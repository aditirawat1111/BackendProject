package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.CreateOrderRequestDto;
import com.aditi.backendcapstoneproject.dto.OrderResponseDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.exception.EmptyCartException;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.service.OrderService;
import com.aditi.backendcapstoneproject.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "APIs for order management - create, view, and manage orders")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create an order", description = "Creates a new order from the user's cart items (User only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Cart is empty"),
            @ApiResponse(responseCode = "403", description = "Access denied - User role required")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto requestDto,
            Authentication authentication) throws EmptyCartException, UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        OrderResponseDto responseDto = orderService.createOrder(email, requestDto.getDeliveryAddress());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its ID (User only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "403", description = "Access denied - User role required"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId,
            Authentication authentication) throws OrderNotFoundException, UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        OrderResponseDto responseDto = orderService.getOrderById(email, orderId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @Operation(summary = "Get all orders", description = "Retrieves a paginated list of orders for the authenticated user with optional status filter (User only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - User role required")
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getOrders(
            Authentication authentication,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'orderDate,desc')") @RequestParam(defaultValue = "orderDate,desc") String sort,
            @Parameter(description = "Filter by order status") @RequestParam(required = false) OrderStatus status) throws UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);

        Pageable pageable = buildPageable(page, size, sort);
        Page<OrderResponseDto> responsePage = orderService.getOrders(email, pageable, status);
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    @Operation(summary = "Update order status", description = "Updates the status of an order (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId,
            @Parameter(description = "New order status", required = true) @RequestParam("status") OrderStatus status) throws OrderNotFoundException {
        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, status);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortParts.length > 1) {
            direction = Sort.Direction.fromString(sortParts[1]);
        }
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}


