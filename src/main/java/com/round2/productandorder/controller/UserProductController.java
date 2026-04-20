package com.round2.productandorder.controller;

import com.round2.productandorder.dto.ProductResponseDTO;
import com.round2.productandorder.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class UserProductController {

    private final ProductService productService;

    public UserProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String productList(Model model) {
        model.addAttribute("products", productService.getActiveProducts());
        return "user/products";
    }

    // REST endpoint
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<ProductResponseDTO>> apiList() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }
}