package com.round2.productandorder.controller;

import com.round2.productandorder.dto.ProductRequestDTO;
import com.round2.productandorder.dto.ProductResponseDTO;
import com.round2.productandorder.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String productList(Model model) {
        model.addAttribute("products", productService.getAllProductsForAdmin());
        model.addAttribute("productRequest", new ProductRequestDTO());
        return "admin/products";
    }

    @PostMapping("/products")
    public String addProduct(@Valid @ModelAttribute("productRequest") ProductRequestDTO request,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("products", productService.getAllProductsForAdmin());
            return "admin/products";
        }
        productService.addProduct(request);
        return "redirect:/admin/products?added=true";
    }

    @GetMapping("/products/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        ProductResponseDTO product = productService.getAllProductsForAdmin().stream()
                .filter(p -> p.getId().equals(id)).findFirst()
                .orElseThrow();
        ProductRequestDTO req = new ProductRequestDTO();
        req.setName(product.getName());
        req.setDescription(product.getDescription());
        req.setPrice(product.getPrice());
        req.setQuantity(product.getQuantity());
        model.addAttribute("product", product);
        model.addAttribute("productRequest", req);
        return "admin/edit-product";
    }

    @PostMapping("/products/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productRequest") ProductRequestDTO request,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", productService.getAllProductsForAdmin().stream()
                    .filter(p -> p.getId().equals(id)).findFirst().orElseThrow());
            return "admin/edit-product";
        }
        productService.updateProduct(id, request);
        return "redirect:/admin/products?updated=true";
    }

    @PostMapping("/products/{id}/toggle")
    public String toggleProduct(@PathVariable Long id) {
        productService.toggleEnabled(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<ProductResponseDTO>> apiGetAll() {
        return ResponseEntity.ok(productService.getAllProductsForAdmin());
    }

    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<ProductResponseDTO> apiAdd(@Valid @RequestBody ProductRequestDTO request) {
        return ResponseEntity.ok(productService.addProduct(request));
    }

    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<ProductResponseDTO> apiUpdate(@PathVariable Long id,
                                                     @Valid @RequestBody ProductRequestDTO request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/api/products/{id}/toggle")
    @ResponseBody
    public ResponseEntity<ProductResponseDTO> apiToggle(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleEnabled(id));
    }
}