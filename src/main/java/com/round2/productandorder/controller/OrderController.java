package com.round2.productandorder.controller;

import com.round2.productandorder.dto.OrderResponseDTO;
import com.round2.productandorder.entity.User;
import com.round2.productandorder.service.OrderService;
import com.round2.productandorder.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }


    @GetMapping
    public String myOrders(Authentication authentication, Model model) {
        User user = userService.getByUsername(authentication.getName());
        model.addAttribute("orders", orderService.getOrdersForUser(user));
        return "user/orders";
    }

    @PostMapping("/place")
    public String placeOrder(Authentication authentication, Model model) {
        User user = userService.getByUsername(authentication.getName());
        try {
            OrderResponseDTO order = orderService.placeOrder(user);
            return "redirect:/orders?placed=" + order.getOrderId();
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("cart", null);
            return "user/cart";
        }
    }


    @PostMapping("/api/place")
    @ResponseBody
    public ResponseEntity<OrderResponseDTO> apiPlace(Authentication authentication) {
        User user = userService.getByUsername(authentication.getName());
        return ResponseEntity.ok(orderService.placeOrder(user));
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<OrderResponseDTO>> apiList(Authentication authentication) {
        User user = userService.getByUsername(authentication.getName());
        return ResponseEntity.ok(orderService.getOrdersForUser(user));
    }

    @GetMapping("/api/{orderId}")
    @ResponseBody
    public ResponseEntity<OrderResponseDTO> apiGet(Authentication authentication,
                                                @PathVariable Long orderId) {
        User user = userService.getByUsername(authentication.getName());
        return ResponseEntity.ok(orderService.getOrderById(user, orderId));
    }
}