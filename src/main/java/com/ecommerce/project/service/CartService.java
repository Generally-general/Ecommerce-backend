package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartItemResponse;
import com.ecommerce.project.dto.CartResponse;
import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.CartItem;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.exception.AuthenticationException;
import com.ecommerce.project.exception.BadRequestException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
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

    public CartItemResponse addToCart(User authenticatedUser, Integer productId, Integer quantity) {
        if (authenticatedUser == null) {
            throw new AuthenticationException("Unauthorized");
        }
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        Cart cart = cartRepository.findByUser(authenticatedUser)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(authenticatedUser).build()
                ));

        Product product = getProductOrThrow(productId);

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

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

    public CartResponse getCart(User authenticatedUser) {
        if (authenticatedUser == null) {
            throw new AuthenticationException("Unauthorized");
        }

        Cart cart = cartRepository.findByUserWithItems(authenticatedUser)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(authenticatedUser).build()));

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

    public CartItemResponse updateQuantity(User authenticatedUser, Integer productId, Integer newQuantity) {

        Cart cart = getCartOrThrow(authenticatedUser);

        Product product = getProductOrThrow(productId);

        CartItem item = getCartItemOrThrow(cart, product);

        if (product.getStockQuantity() < newQuantity) {
            throw new BadRequestException("Not enough stock available");
        }

        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        item.setQuantity(newQuantity);

        return toResponse(item);
    }

    public void removeItem(User authenticatedUser, Integer productId) {

        Cart cart = getCartOrThrow(authenticatedUser);

        Product product = getProductOrThrow(productId);

        CartItem item = getCartItemOrThrow(cart, product);

        cartItemRepository.delete(item);
    }

    public void clearCart(User authenticatedUser) {

        Cart cart = getCartOrThrow(authenticatedUser);

        List<CartItem> item = cartItemRepository.findByCart(cart);

        cartItemRepository.deleteAll(item);
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

    private Cart getCartOrThrow(User user) {
        if (user == null) {
            throw new AuthenticationException("Unauthorized");
        }

        return cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart doesn't exist"));
    }

    private Product getProductOrThrow(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private CartItem getCartItemOrThrow(Cart cart, Product product) {
        return cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));
    }
}
