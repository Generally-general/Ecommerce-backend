package com.ecommerce.project.service;

import com.ecommerce.project.dto.OrderItemResponse;
import com.ecommerce.project.dto.OrderResponse;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.exception.AuthenticationException;
import com.ecommerce.project.exception.BadRequestException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.OrderRepository;
import com.ecommerce.project.repository.ProductRepository;
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

    public OrderResponse checkout(User authenticatedUser) {
        if(authenticatedUser == null) {
            throw new AuthenticationException("Unauthorized");
        }

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
        order.setStatus(OrderStatus.CONFIRMED);

        Order savedOrder = orderRepository.save(order);
        cart.getItems().clear();


        log.info("User {} placed order {} with total {}", authenticatedUser.getId(), order.getId(), total);

        return toResponse(savedOrder);
    }

    public Page<OrderResponse> getMyOrders(User authenticatedUser, Pageable pageable) {
        if(authenticatedUser == null) {
            throw new AuthenticationException("Unauthorized");
        }

        return orderRepository.findByUser(authenticatedUser, pageable)
                .map(this::toResponse);
    }

    public OrderResponse getOrderById(User authenticatedUser, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if(!order.getUser().getId().equals(authenticatedUser.getId())) {
            throw new AuthenticationException("You cannot access this order");
        }

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
