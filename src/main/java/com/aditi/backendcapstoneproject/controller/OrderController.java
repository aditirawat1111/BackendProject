package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.CreateOrderRequestDto;
import com.aditi.backendcapstoneproject.dto.OrderResponseDto;
import com.aditi.backendcapstoneproject.exception.EmptyCartException;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import com.aditi.backendcapstoneproject.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService,
                           UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto requestDto,
            Authentication authentication) throws EmptyCartException {
        User user = getCurrentUser(authentication);
        OrderResponseDto responseDto = orderService.createOrder(user, requestDto.getDeliveryAddress());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) throws OrderNotFoundException {
        User user = getCurrentUser(authentication);
        OrderResponseDto responseDto = orderService.getOrderById(user, orderId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<OrderResponseDto> responseDtos = orderService.getOrders(user);
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}


