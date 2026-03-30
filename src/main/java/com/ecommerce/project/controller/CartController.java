package com.ecommerce.project.controller;

import com.ecommerce.project.dto.ApiResponse;
import com.ecommerce.project.dto.CartItemRequest;
import com.ecommerce.project.dto.CartItemResponse;
import com.ecommerce.project.dto.CartResponse;
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

}
