package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}
