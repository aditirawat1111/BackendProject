package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.AddToCartRequestDto;
import com.aditi.backendcapstoneproject.dto.CartResponseDto;
import com.aditi.backendcapstoneproject.dto.UpdateCartItemRequestDto;
import com.aditi.backendcapstoneproject.exception.CartItemNotFoundException;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import com.aditi.backendcapstoneproject.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(Authentication authentication) {
        User user = getCurrentUser(authentication);
        CartResponseDto cart = cartService.getCart(user);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @Valid @RequestBody AddToCartRequestDto request,
            Authentication authentication) throws ProductNotFoundException {
        User user = getCurrentUser(authentication);
        CartResponseDto cart = cartService.addItemToCart(user, request.getProductId(), request.getQuantity());
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequestDto request,
            Authentication authentication) throws CartItemNotFoundException {
        User user = getCurrentUser(authentication);
        CartResponseDto cart = cartService.updateCartItem(user, itemId, request.getQuantity());
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> removeCartItem(
            @PathVariable Long itemId,
            Authentication authentication) throws CartItemNotFoundException {
        User user = getCurrentUser(authentication);
        CartResponseDto cart = cartService.removeCartItem(user, itemId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<CartResponseDto> clearCart(Authentication authentication) {
        User user = getCurrentUser(authentication);
        CartResponseDto cart = cartService.clearCart(user);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

