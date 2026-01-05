package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.AddToCartRequestDto;
import com.aditi.backendcapstoneproject.dto.CartResponseDto;
import com.aditi.backendcapstoneproject.dto.UpdateCartItemRequestDto;
import com.aditi.backendcapstoneproject.exception.CartItemNotFoundException;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.service.CartService;
import com.aditi.backendcapstoneproject.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart", description = "APIs for shopping cart management - add, update, remove items")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Add item to cart", description = "Adds a product to the user's shopping cart (User only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - User role required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @Valid @RequestBody AddToCartRequestDto request,
            Authentication authentication) throws ProductNotFoundException, UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        CartResponseDto cart = cartService.addItemToCart(email, request.getProductId(), request.getQuantity());
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @Operation(summary = "Get cart", description = "Retrieves the current user's shopping cart with all items (User only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - User role required")
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(Authentication authentication) throws UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        CartResponseDto cart = cartService.getCart(email);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequestDto request,
            Authentication authentication) throws CartItemNotFoundException, UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        CartResponseDto cart = cartService.updateCartItem(email, itemId, request.getQuantity());
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> removeCartItem(
            @PathVariable Long itemId,
            Authentication authentication) throws CartItemNotFoundException, UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        CartResponseDto cart = cartService.removeCartItem(email, itemId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping
    public ResponseEntity<CartResponseDto> clearCart(Authentication authentication) throws UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        CartResponseDto cart = cartService.clearCart(email);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }
}