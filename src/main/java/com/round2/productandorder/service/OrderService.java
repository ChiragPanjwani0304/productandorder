package com.round2.productandorder.service;

import com.round2.productandorder.dto.OrderResponseDTO;
import com.round2.productandorder.entity.*;
import com.round2.productandorder.exception.BadRequestException;
import com.round2.productandorder.exception.InsufficientInventoryException;
import com.round2.productandorder.exception.ResourceNotFoundException;
import com.round2.productandorder.repository.OrderRepository;
import com.round2.productandorder.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CartService cartService, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponseDTO placeOrder(User user) {
        Cart cart = cartService.getCartForUser(user);

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PLACED);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (!product.isEnabled()) {
                throw new BadRequestException("Product '" + product.getName() + "' is no longer available");
            }
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new InsufficientInventoryException(
                        "Insufficient stock for '" + product.getName() +
                                "'. Available: " + product.getQuantity() +
                                ", Requested: " + cartItem.getQuantity());
            }

            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtOrder(product.getPrice());
            orderItems.add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        order.setOrderItems(orderItems);
        Order saved = orderRepository.save(order);
        cartService.clearCart(cart);

        return toResponse(saved);
    }

    public List<OrderResponseDTO> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByPlacedAtDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Order does not belong to this user");
        }
        return toResponse(order);
    }

    private OrderResponseDTO toResponse(Order order) {
        List<OrderResponseDTO.OrderItemResponse> items = order.getOrderItems().stream().map(oi -> {
            OrderResponseDTO.OrderItemResponse r = new OrderResponseDTO.OrderItemResponse();
            r.setProductId(oi.getProduct().getId());
            r.setProductName(oi.getProduct().getName());
            r.setQuantity(oi.getQuantity());
            r.setPriceAtOrder(oi.getPriceAtOrder());
            r.setSubtotal(oi.getPriceAtOrder().multiply(BigDecimal.valueOf(oi.getQuantity())));
            return r;
        }).collect(Collectors.toList());

        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getId());
        response.setPlacedAt(order.getPlacedAt());
        response.setStatus(order.getStatus().name());
        response.setTotalAmount(order.getTotalAmount());
        response.setItems(items);
        return response;
    }
}