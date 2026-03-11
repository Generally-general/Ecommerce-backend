package com.ecommerce.project.controller;

import com.ecommerce.project.dto.ApiResponse;
import com.ecommerce.project.dto.CategoryRequest;
import com.ecommerce.project.dto.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse response = categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Category created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "id")
            Pageable pageable
    ) {
        Page<CategoryResponse> page = categoryService.getAllCategories(name, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categories fetched", page));
    }
}
