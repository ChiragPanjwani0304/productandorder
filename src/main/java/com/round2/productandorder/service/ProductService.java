package com.round2.productandorder.service;

import com.round2.productandorder.dto.ProductRequest;
import com.round2.productandorder.dto.ProductRequestDTO;
import com.round2.productandorder.dto.ProductResponse;
import com.round2.productandorder.dto.ProductResponseDTO;
import com.round2.productandorder.entity.Product;
import com.round2.productandorder.exception.ResourceNotFoundException;
import com.round2.productandorder.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponseDTO addProduct(ProductRequestDTO request) {
        Product product = new Product();
        mapRequestToProduct(request, product);
        product.setEnabled(true);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
        Product product = findById(id);
        mapRequestToProduct(request, product);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDTO toggleEnabled(Long id) {
        Product product = findById(id);
        product.setEnabled(!product.isEnabled());
        return toResponse(productRepository.save(product));
    }

    public List<ProductResponseDTO> getAllProductsForAdmin() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    public List<ProductResponseDTO> getActiveProducts() {
        return productRepository.findByEnabledTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO getActiveProductById(Long id) {
        Product product = findById(id);
        if (!product.isEnabled()) {
            throw new ResourceNotFoundException("Product not found");
        }
        return toResponse(product);
    }


    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void mapRequestToProduct(ProductRequest request, Product product) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
    }

    private ProductResponseDTO toResponse(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.isEnabled()
        );
    }
}