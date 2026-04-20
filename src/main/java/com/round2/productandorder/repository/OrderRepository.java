package com.round2.productandorder.repository;

import com.round2.productandorder.entity.Order;
import com.round2.productandorder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByPlacedAtDesc(User user);
}