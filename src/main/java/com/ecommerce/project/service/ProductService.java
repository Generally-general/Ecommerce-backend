package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryResponse;
import com.ecommerce.project.dto.ProductRequest;
import com.ecommerce.project.dto.ProductResponse;
import com.ecommerce.project.entity.Category;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        List<Category> rawCategories = categoryRepository.findAllById(request.getCategoryIds());

        if(rawCategories.size() != request.getCategoryIds().size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }

        Set<Category> categories = new HashSet<>(rawCategories);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .categories(categories)
                .build();
        return toResponse(productRepository.save(product));
    }

    public Page<ProductResponse> getAllProducts(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer categoryId,
            Pageable pageable
    ) {
        Specification<Product> spec = Specification.allOf();

        if(name != null && !name.isBlank()) spec = spec.and(ProductSpecification.nameContains(name));
        if(minPrice != null) spec = spec.and(ProductSpecification.priceGreaterThanOrEqual(minPrice));
        if(maxPrice != null) spec = spec.and(ProductSpecification.priceLessThanOrEqual(maxPrice));
        if(categoryId != null) spec = spec.and(ProductSpecification.hasCategory(categoryId));

        return productRepository
                .findAll(spec, pageable)
                .map(this::toResponse);
    }

    public ProductResponse getProductById(Integer id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .categories(product.getCategories().stream()
                        .map(c -> new CategoryResponse(c.getId(), c.getName()))
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build();
    }
}
