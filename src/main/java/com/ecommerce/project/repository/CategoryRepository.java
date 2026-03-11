package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);

    Page<Category> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}
