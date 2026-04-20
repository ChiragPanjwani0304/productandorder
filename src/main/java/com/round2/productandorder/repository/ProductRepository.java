package com.round2.productandorder.repository;

import com.round2.productandorder.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByEnabledTrue();
}