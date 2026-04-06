package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUser(User user, Pageable pageable);
}
