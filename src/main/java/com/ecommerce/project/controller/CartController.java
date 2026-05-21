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
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CartItemRequest request
    ) {
        CartItemResponse response = cartService.addToCart(user.getId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Item Added Successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(@AuthenticationPrincipal AuthenticatedUser user) {
        CartResponse response = cartService.getCart(user.getId());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cart fetched successfully", response)
        );
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateQuantity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer productId,
            @RequestBody UpdateCartItemRequest request
    ) {
        CartItemResponse response = cartService.updateQuantity(user.getId(), productId, request.getQuantity());

        String message = (response.getQuantity() == 0) ? "Item removed from cart" : "Quantity updated";

        return ResponseEntity.ok(
                new ApiResponse<>(true, message, response)
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer productId
    ) {
        cartService.removeItem(user.getId(), productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item removed", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal AuthenticatedUser user) {
        cartService.clearCartByUserId(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart cleared successfully", null));
    }
}
