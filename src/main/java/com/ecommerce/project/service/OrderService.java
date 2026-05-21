package com.ecommerce.project.service;

import com.ecommerce.project.dto.OrderItemResponse;
import com.ecommerce.project.dto.OrderResponse;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.exception.AccessDeniedException;
import com.ecommerce.project.exception.AuthenticationException;
import com.ecommerce.project.exception.BadRequestException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final UserRepository userRepository;

    public OrderResponse checkout(Integer userId) {
        User authenticatedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user Not Found"));

        Cart cart = cartRepository.findByUserWithItems(authenticatedUser)
                .orElseThrow(() -> new ResourceNotFoundException("Cart doesn't exist"));

        if(cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = Order.builder()
                .user(authenticatedUser)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for(CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product disappeared!"));

            int requestedQty = cartItem.getQuantity();
            int availableQty = product.getStockQuantity();

            int fulfilledQty = Math.min(requestedQty, availableQty);

            if(fulfilledQty <= 0) continue;

            product.setStockQuantity(availableQty - fulfilledQty);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .requestedQuantity(requestedQty)
                    .fulfilledQuantity(fulfilledQty)
                    .price(product.getPrice())
                    .build();

            order.addItem(orderItem);

            total = total.add(
                    product.getPrice().multiply(BigDecimal.valueOf(fulfilledQty))
            );
        }

        if(order.getItems().isEmpty()) {
            throw new BadRequestException("No items available for checkout");
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);
        cart.getItems().clear();


        log.info("User {} placed order {} with total {}", authenticatedUser.getId(), order.getId(), total);

        return toResponse(savedOrder);
    }

    public Page<OrderResponse> getMyOrders(Integer userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    public OrderResponse getOrderById(Integer userId, String role, Integer orderId) {
        Order order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        boolean isOwner = order.getUser().getId().equals(userId);
        boolean isAdmin = role.equals(Role.ADMIN.name());

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to access this order");
        }

        return toResponse(order);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        if(!isValidTransition(currentStatus, newStatus)) {
            throw new BadRequestException("Invalid status transition");
        }

        order.setStatus(newStatus);

        return toResponse(order);
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PENDING ->
                next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED ->
                    next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public OrderResponse processPayment(Integer userId, Integer orderId, boolean success) {
        Order order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        boolean isOwner = order.getUser().getId().equals(userId);

        if(!isOwner) {
            throw new AccessDeniedException("You cannot pay for this order");
        }

        if(order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in payable state");
        }

        if(!success) {
            order.setStatus(OrderStatus.CANCELLED);
            return toResponse(order);
        }

        for(OrderItem item: order.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            int available = product.getStockQuantity();
            int needed = item.getFulfilledQuantity();

            if(available < needed) {
                throw new BadRequestException("Stock changed, cannot complete order");
            }

            product.setStockQuantity(available - needed);
        }

        order.setStatus(OrderStatus.CONFIRMED);

        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .productId(oi.getProduct().getId())
                        .productName(oi.getProduct().getName())
                        .requestedQuantity(oi.getRequestedQuantity())
                        .fulfilledQuantity(oi.getFulfilledQuantity())
                        .price(oi.getPrice())
                        .subtotal(oi.getPrice().multiply(BigDecimal.valueOf(oi.getFulfilledQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
