package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryRequest;
import com.ecommerce.project.dto.CategoryResponse;
import com.ecommerce.project.entity.Category;
import com.ecommerce.project.exception.ConflictException;
import com.ecommerce.project.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Transactional
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryResponse createCategory(CategoryRequest request) {
        if(categoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Category already exists");
        }
        Category category = new Category();
        category.setName(request.getName());
        return toResponse(categoryRepository.save(category));
    }

    public Page<CategoryResponse> getAllCategories(
            String name,
            Pageable pageable
    ) {
        Page<Category> page;
        if(name != null) {
            page = categoryRepository
                    .findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = categoryRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    public CategoryResponse toResponse(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
