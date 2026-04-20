package com.round2.productandorder.service;

import com.round2.productandorder.dto.CartItemRequestDTO;
import com.round2.productandorder.dto.CartResponseDTO;
import com.round2.productandorder.entity.*;
import com.round2.productandorder.exception.BadRequestException;
import com.round2.productandorder.exception.InsufficientInventoryException;
import com.round2.productandorder.exception.ResourceNotFoundException;
import com.round2.productandorder.repository.CartItemRepository;
import com.round2.productandorder.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public CartResponseDTO getCart(User user) {
        Cart cart = getCartForUser(user);
        return toResponse(cart);
    }

    @Transactional
    public CartResponseDTO addItem(User user, CartItemRequestDTO request) {
        Cart cart = getCartForUser(user);
        Product product = productService.findById(request.getProductId());

        if (!product.isEnabled()) {
            throw new BadRequestException("Product is not available");
        }
        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientInventoryException(
                    "Only " + product.getQuantity() + " unit(s) available for '" + product.getName() + "'");
        }

        Optional<CartItem> existing = cartItemRepository.findByCartAndProduct(cart, product);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (product.getQuantity() < newQty) {
                throw new InsufficientInventoryException(
                        "Only " + product.getQuantity() + " unit(s) available for '" + product.getName() + "'");
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        return toResponse(getCartForUser(user));
    }

    @Transactional
    public CartResponseDTO updateItem(User user, Long cartItemId, CartItemRequestDTO request) {
        Cart cart = getCartForUser(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        Product product = item.getProduct();
        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientInventoryException(
                    "Only " + product.getQuantity() + " unit(s) available for '" + product.getName() + "'");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toResponse(getCartForUser(user));
    }

    @Transactional
    public CartResponseDTO removeItem(User user, Long cartItemId) {
        Cart cart = getCartForUser(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cartItemRepository.delete(item);
        return toResponse(getCartForUser(user));
    }

    @Transactional
    public void clearCart(Cart cart) {
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    public Cart getCartForUser(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
    }

    private CartResponseDTO toResponse(Cart cart) {
        List<CartResponseDTO.CartItemResponse> itemResponses = cart.getCartItems().stream().map(ci -> {
            CartResponseDTO.CartItemResponse r = new CartResponseDTO.CartItemResponse();
            r.setCartItemId(ci.getId());
            r.setProductId(ci.getProduct().getId());
            r.setProductName(ci.getProduct().getName());
            r.setPrice(ci.getProduct().getPrice());
            r.setQuantity(ci.getQuantity());
            r.setSubtotal(ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            return r;
        }).collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartResponseDTO.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponseDTO response = new CartResponseDTO();
        response.setCartId(cart.getId());
        response.setItems(itemResponses);
        response.setTotalAmount(total);
        return response;
    }
}