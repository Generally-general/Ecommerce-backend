package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryResponse;
import com.ecommerce.project.dto.ProductRequest;
import com.ecommerce.project.dto.ProductResponse;
import com.ecommerce.project.entity.Category;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.exception.ConflictException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCacheService productCacheService;

    @CacheEvict(value = "products_list", allEntries = true)
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

    @Cacheable(value = "products_list", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
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

    public ProductResponse getProductFromDB(Integer id) {
        System.out.println("Fetching from DB (fallback)");
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public ProductResponse getProductByIdSafe(Integer id) {
        try {
            return productCacheService.getProductById(id);
        } catch (Exception ex) {
            System.out.println("Redis failed, falling back to DB...");
            return getProductFromDB(id);
        }
    }

    @CacheEvict(value = {"products_list"}, allEntries = true)
    @CachePut(value = "products", key = "#id")
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        validateVersion(product, request);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        Product saved = productRepository.save(product);

        return toResponse(saved);
    }

    @CacheEvict(value = {"products", "products_list"}, allEntries = true)
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        productRepository.delete(product);
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

    private void validateVersion(Product product, ProductRequest request) {
        if(request.getVersion() == null ||
                !product.getVersion().equals(request.getVersion())) {
            throw new ConflictException("Product was updated by another admin. Refresh and try again.");
        }
    }
}
