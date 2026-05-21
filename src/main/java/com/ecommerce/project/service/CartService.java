package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartItemResponse;
import com.ecommerce.project.dto.CartResponse;
import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.CartItem;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.exception.BadRequestException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public CartItemResponse addToCart(Integer userId, Integer productId, Integer quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User userReference = userRepository.getReferenceById(userId);
                    return cartRepository.save(
                        Cart.builder().user(userReference).build()
                    );
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        int newQuantity = existingItem
                .map(item -> item.getQuantity() + quantity)
                .orElse(quantity);

        if (product.getStockQuantity() < newQuantity) {
            throw new BadRequestException("Not enough stock available");
        }

        CartItem item;
        if (existingItem.isPresent()) {
            item = existingItem.get();
            item.setQuantity(newQuantity);
        } else {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(item);
        }
        return toResponse(item);
    }

    public CartResponse getCart(Integer userId) {
        Cart cart = cartRepository.findByUserIdWithItemsAndProducts(userId)
                .orElseGet(() -> {
                    User userReference = userRepository.getReferenceById(userId);
                    return cartRepository.save(Cart.builder().user(userReference).build());
                });

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toResponse).toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(totalAmount)
                .build();
    }

    public CartItemResponse updateQuantity(Integer userId, Integer productId, Integer newQuantity) {

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        Product product = item.getProduct();

        if (product.getStockQuantity() < newQuantity) {
            throw new BadRequestException("Not enough stock available");
        }

        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
            return CartItemResponse.builder()
                    .productId(productId)
                    .quantity(0)
                    .subtotal(BigDecimal.ZERO).build();
        }

        item.setQuantity(newQuantity);

        return toResponse(item);
    }

    public void removeItem(Integer userId, Integer productId) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        cartItemRepository.delete(item);
    }

    public void clearCartByUserId(Integer userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public CartItemResponse toResponse(CartItem item) {
        BigDecimal price = item.getProduct().getPrice();
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .price(price)
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
