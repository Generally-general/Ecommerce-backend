package com.ecommerce.project.controller;

import com.ecommerce.project.dto.ApiResponse;
import com.ecommerce.project.dto.ProductRequest;
import com.ecommerce.project.dto.ProductResponse;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Product created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        Page<ProductResponse> page = productService.getAllProducts(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Products fetched", page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Integer id
    ) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product fetched", response));
    }
}
