package com.round2.productandorder.repository;

import com.round2.productandorder.entity.Cart;
import com.round2.productandorder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}