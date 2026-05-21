package com.ecommerce.project.controller;

import com.ecommerce.project.dto.ApiResponse;
import com.ecommerce.project.dto.AuthenticatedUser;
import com.ecommerce.project.dto.OrderResponse;
import com.ecommerce.project.entity.OrderStatus;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        OrderResponse response = orderService.checkout(user.getId());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order placed successfully", response)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<OrderResponse> response = orderService.getMyOrders(user.getId(), pageable);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orders fetched successfully", response)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer id
    ) {
        OrderResponse response = orderService.getOrderById(user.getId(), user.getRole(), id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order fetched", response)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "All orders fetched",
                        orderService.getAllOrders(pageable))
        );
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Integer orderId,
            @RequestParam OrderStatus status
    ) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order status updated", response)
        );
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> processPayment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer orderId,
            @RequestParam boolean success
    ) {
        OrderResponse response = orderService.processPayment(user.getId(), orderId, success);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Payment processed", response)
        );
    }
}
