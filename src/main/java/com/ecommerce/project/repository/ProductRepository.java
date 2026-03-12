package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends
        JpaRepository<Product, Integer>,
        JpaSpecificationExecutor<Product> {
    @EntityGraph(attributePaths = "categories")
    Page<Product> findAll(Pageable pageable);
}
