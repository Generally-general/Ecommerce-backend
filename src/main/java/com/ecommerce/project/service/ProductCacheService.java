package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryResponse;
import com.ecommerce.project.dto.ProductResponse;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCacheService {
    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Integer id) {
        System.out.println("Fetching from DB (via cache service)");

        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .version(product.getVersion())
                .categories(product.getCategories().stream()
                        .map(c -> new CategoryResponse(c.getId(), c.getName()))
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build();
    }
}
