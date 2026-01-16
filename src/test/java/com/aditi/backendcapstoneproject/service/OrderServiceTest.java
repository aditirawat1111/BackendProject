package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.OrderResponseDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.exception.EmptyCartException;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.model.*;
import com.aditi.backendcapstoneproject.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(999.99);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(new Date());
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setDeliveryAddress("123 Test St");
        testOrder.setTotalAmount(1999.98);

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(999.99);
        
        // Mock userRepository.findByEmail to return testUser
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testCreateOrder_Success() throws EmptyCartException, UserNotFoundException {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.singletonList(testCartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);
        doNothing().when(cartItemRepository).deleteByCart(testCart);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(orderItemRepository.findByOrder(testOrder)).thenReturn(Collections.singletonList(testOrderItem));

        // When
        OrderResponseDto result = orderService.createOrder(testUser.getEmail(), "123 Test St");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getDeliveryAddress()).isEqualTo("123 Test St");
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(cartItemRepository, times(1)).findByCart(testCart);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(cartItemRepository, times(1)).deleteByCart(testCart);
    }

    @Test
    void testCreateOrder_EmptyCart_NoCart() {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testUser.getEmail(), "123 Test St"))
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("Cart is empty");
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_EmptyCart_NoItems() {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testUser.getEmail(), "123 Test St"))
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("Cart is empty");
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(cartItemRepository, times(1)).findByCart(testCart);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetOrderById_Success() throws OrderNotFoundException, UserNotFoundException {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrder(testOrder)).thenReturn(Collections.singletonList(testOrderItem));

        // When
        OrderResponseDto result = orderService.getOrderById(testUser.getEmail(), 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository, times(1)).findById(1L);
        verify(orderItemRepository, times(1)).findByOrder(testOrder);
    }

    @Test
    void testGetOrderById_NotFound() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(testUser.getEmail(), 999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order with id 999 not found");
        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    void testGetOrderById_WrongUser() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testOrder.setUser(otherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(testUser.getEmail(), 1L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order does not belong to user");
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrders() throws UserNotFoundException {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByUser(testUser)).thenReturn(orders);
        when(orderItemRepository.findByOrder(testOrder)).thenReturn(Collections.singletonList(testOrderItem));

        // When
        List<OrderResponseDto> result = orderService.getOrders(testUser.getEmail());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(1L);
        verify(orderRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetOrders_WithPagination_NoFilter() throws UserNotFoundException {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder), pageable, 1);
        when(orderRepository.findByUser(testUser, pageable)).thenReturn(orderPage);
        when(orderItemRepository.findByOrder(testOrder)).thenReturn(Collections.singletonList(testOrderItem));

        // When
        Page<OrderResponseDto> result = orderService.getOrders(testUser.getEmail(), pageable, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        verify(orderRepository, times(1)).findByUser(testUser, pageable);
        verify(orderRepository, never()).findByUserAndStatus(any(), any(), any());
    }

    @Test
    void testGetOrders_WithPagination_WithStatusFilter() throws UserNotFoundException {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder), pageable, 1);
        when(orderRepository.findByUserAndStatus(testUser, OrderStatus.PENDING, pageable)).thenReturn(orderPage);
        when(orderItemRepository.findByOrder(testOrder)).thenReturn(Collections.singletonList(testOrderItem));

        // When
        Page<OrderResponseDto> result = orderService.getOrders(testUser.getEmail(), pageable, OrderStatus.PENDING);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        verify(orderRepository, times(1)).findByUserAndStatus(testUser, OrderStatus.PENDING, pageable);
    }

    @Test
    void testUpdateOrderStatus_Success() throws OrderNotFoundException {
        // Given
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setUser(testUser);
        updatedOrder.setStatus(OrderStatus.CONFIRMED);
        updatedOrder.setTotalAmount(1999.98);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderItemRepository.findByOrder(any(Order.class))).thenReturn(Collections.singletonList(testOrderItem));

        // When
        OrderResponseDto result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_NotFound() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order with id 999 not found");
        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }
}

