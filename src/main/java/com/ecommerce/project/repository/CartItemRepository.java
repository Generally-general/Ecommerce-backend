package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.CartItem;
import com.ecommerce.project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
