package com.ecommerce.project.controller;

import com.ecommerce.project.dto.*;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest request
    ) {
        CartItemResponse response = cartService.addToCart(user, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Item Added Successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(@AuthenticationPrincipal User user) {
        CartResponse response = cartService.getCart(user);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cart fetched successfully", response)
        );
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Integer productId,
            @RequestBody UpdateCartItemRequest request
    ) {
        CartItemResponse response = cartService.updateQuantity(user, productId, request.getQuantity());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Quantity updated", response)
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Integer productId
    ) {
        cartService.removeItem(user, productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item removed", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart cleared successfully", null));
    }
}
