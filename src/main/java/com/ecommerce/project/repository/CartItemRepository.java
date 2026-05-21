package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.CartItem;
import com.ecommerce.project.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartIdAndProductId(Integer cartId, Integer productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product")
    void deleteByCartAndProduct(Cart cart, Product product);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    void deleteByCart(Cart cart);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart")
    List<CartItem> findByCart(Cart cart);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.product p " +
            "WHERE ci.cart.user.id = :userId AND ci.product.id = :productId")
    Optional<CartItem> findByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
}
