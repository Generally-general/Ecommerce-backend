package com.ecommerce.project.controller;

import com.ecommerce.project.dto.*;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRequest request
    ) {
        UserResponse data = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Registration Successful", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse data = authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(new ApiResponse<>(true, "Login Successful", data));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        LoginResponse data = authService.refreshAccessToken(request.getRefreshToken());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Token refreshed successfully", data)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile fetched", authService.toResponse(authenticatedUser))
        );
    }
}
