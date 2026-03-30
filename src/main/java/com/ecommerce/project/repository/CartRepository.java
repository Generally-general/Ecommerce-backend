package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUser(User user);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user = :user")
    Optional<Cart> findByUserWithItems(User user);
}
