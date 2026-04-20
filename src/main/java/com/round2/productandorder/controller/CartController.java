package com.round2.productandorder.controller;

import com.round2.productandorder.dto.CartItemRequestDTO;
import com.round2.productandorder.dto.CartItemRequestDTO;
import com.round2.productandorder.dto.CartResponseDTO;
import com.round2.productandorder.dto.CartResponseDTO;
import com.round2.productandorder.entity.User;
import com.round2.productandorder.service.CartService;
import com.round2.productandorder.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    // ── Views ──────────────────────────────────────────────────────────────────

    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        User user = userService.getByUsername(authentication.getName());
        model.addAttribute("cart", cartService.getCart(user));
        return "user/cart";
    }

    @PostMapping("/add")
    public String addItem(Authentication authentication,
                          @RequestParam Long productId,
                          @RequestParam int quantity) {
        User user = userService.getByUsername(authentication.getName());
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(productId);
        request.setQuantity(quantity);
        cartService.addItem(user, request);
        return "redirect:/cart";
    }

    @PostMapping("/update/{cartItemId}")
    public String updateItem(Authentication authentication,
                             @PathVariable Long cartItemId,
                             @RequestParam int quantity) {
        User user = userService.getByUsername(authentication.getName());
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(0L); // not needed for update
        request.setQuantity(quantity);
        cartService.updateItem(user, cartItemId, request);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeItem(Authentication authentication,
                             @PathVariable Long cartItemId) {
        User user = userService.getByUsername(authentication.getName());
        cartService.removeItem(user, cartItemId);
        return "redirect:/cart";
    }

    // ── REST API ───────────────────────────────────────────────────────────────

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<CartResponseDTO> apiGet(Authentication authentication) {
        User user = userService.getByUsername(authentication.getName());
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<CartResponseDTO> apiAdd(Authentication authentication,
                                                  @Valid @RequestBody CartItemRequestDTO request) {
        User user = userService.getByUsername(authentication.getName());
        return ResponseEntity.ok(cartService.addItem(user, request));
    }

    @PutMapping("/api/update/{cartItemId}")
    @ResponseBody
    public ResponseEntity<CartResponseDTO> apiUpdate(Authentication authentication,
                                                  @PathVariable Long cartItemId,
                                                  @Valid @RequestBody CartItemRequestDTO request) {
        User user = userService.getByUsername(authentication.getName());
        return ResponseEntity.ok(cartService.updateItem(user, cartItemId, request));
    }

    @DeleteMapping("/api/remove/{cartItemId}")
    @ResponseBody
    public ResponseEntity<CartResponseDTO> apiRemove(Authentication authentication,
                                                  @PathVariable Long cartItemId) {
        User user = userService.getByUsername(authentication.getName());
        return ResponseEntity.ok(cartService.removeItem(user, cartItemId));
    }
}