package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.OrderItemResponseDto;
import com.aditi.backendcapstoneproject.dto.OrderResponseDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.exception.EmptyCartException;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.model.*;
import com.aditi.backendcapstoneproject.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    private User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional
    @CacheEvict(cacheNames = {"orders", "orderById"}, allEntries = true)
    public OrderResponseDto createOrder(String email, String deliveryAddress) throws EmptyCartException, UserNotFoundException {
        logger.info("Creating order for user: {}", email);
        
        User user = getUserByEmail(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> {
                    logger.warn("Order creation failed: Cart is empty for user: {}", email);
                    return new EmptyCartException("Cart is empty");
                });

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            logger.warn("Order creation failed: Cart has no items for user: {}", email);
            throw new EmptyCartException("Cart is empty");
        }

        logger.debug("Creating order with {} items from cart for user: {}", cartItems.size(), email);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(deliveryAddress);
        order.setCreatedAt(new Date());
        order.setLastModified(new Date());
        order.setDeleted(false);

        order = orderRepository.save(order);
        logger.debug("Order created with ID: {}", order.getId());

        double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setCreatedAt(new Date());
            orderItem.setLastModified(new Date());
            orderItem.setDeleted(false);

            totalAmount += orderItem.getPrice() * orderItem.getQuantity();
            orderItemRepository.save(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setLastModified(new Date());
        orderRepository.save(order);

        cartItemRepository.deleteByCart(cart);
        cart.setLastModified(new Date());
        cartRepository.save(cart);

        logger.info("Order created successfully with ID: {} and total amount: {} for user: {}", 
                order.getId(), totalAmount, email);

        return buildOrderResponse(order);
    }

    @Cacheable(cacheNames = "orderById", key = "#email + ':' + #orderId")
    public OrderResponseDto getOrderById(String email, Long orderId) throws OrderNotFoundException, UserNotFoundException {
        User user = getUserByEmail(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found"));

        if (order.getUser().getId() != user.getId()) {
            throw new OrderNotFoundException("Order does not belong to user");
        }

        return buildOrderResponse(order);
    }

    @Cacheable(cacheNames = "orders", key = "#email")
    public List<OrderResponseDto> getOrders(String email) throws UserNotFoundException {
        User user = getUserByEmail(email);
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    public Page<OrderResponseDto> getOrders(String email, Pageable pageable, OrderStatus status) throws UserNotFoundException {
        User user = getUserByEmail(email);
        Page<Order> page;
        if (status != null) {
            page = orderRepository.findByUserAndStatus(user, status, pageable);
        } else {
            page = orderRepository.findByUser(user, pageable);
        }

        return page.map(this::buildOrderResponse);
    }

    @Transactional
    @CacheEvict(cacheNames = {"orders", "orderById"}, allEntries = true)
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status) throws OrderNotFoundException {
        logger.info("Updating order status: Order ID: {}, New Status: {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order status update failed: Order with id {} not found", orderId);
                    return new OrderNotFoundException("Order with id " + orderId + " not found");
                });

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        order.setLastModified(new Date());
        orderRepository.save(order);

        logger.info("Order status updated successfully: Order ID: {}, Status changed from {} to {}", 
                orderId, oldStatus, status);

        return buildOrderResponse(order);
    }

    private OrderResponseDto buildOrderResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        List<OrderItemResponseDto> itemDtos = orderItems.stream()
                .map(OrderItemResponseDto::from)
                .collect(Collectors.toList());

        int totalItems = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setOrderId(order.getId());
        responseDto.setOrderDate(order.getOrderDate());
        responseDto.setStatus(order.getStatus());
        responseDto.setTotalAmount(order.getTotalAmount());
        responseDto.setDeliveryAddress(order.getDeliveryAddress());
        responseDto.setItems(itemDtos);
        responseDto.setTotalItems(totalItems);

        return responseDto;
    }
}


