package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.OrderItemResponseDto;
import com.aditi.backendcapstoneproject.dto.OrderResponseDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.exception.EmptyCartException;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.model.*;
import com.aditi.backendcapstoneproject.repository.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public OrderResponseDto createOrder(User user, String deliveryAddress) throws EmptyCartException {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new EmptyCartException("Cart is empty"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(deliveryAddress);
        order.setCreatedAt(new Date());
        order.setLastModified(new Date());
        order.setDeleted(false);

        order = orderRepository.save(order);

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

        return buildOrderResponse(order);
    }

    public OrderResponseDto getOrderById(User user, Long orderId) throws OrderNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found"));

        if (order.getUser().getId() != user.getId()) {
            throw new OrderNotFoundException("Order does not belong to user");
        }

        return buildOrderResponse(order);
    }

    public List<OrderResponseDto> getOrders(User user) {
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
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


