package com.round2.productandorder.repository;

import com.round2.productandorder.entity.Cart;
import com.round2.productandorder.entity.CartItem;
import com.round2.productandorder.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}