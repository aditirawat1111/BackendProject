package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.PaymentRequestDto;
import com.aditi.backendcapstoneproject.dto.PaymentResponseDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.enums.PaymentMethod;
import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.exception.PaymentNotFoundException;
import com.aditi.backendcapstoneproject.model.Order;
import com.aditi.backendcapstoneproject.model.Payment;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.OrderRepository;
import com.aditi.backendcapstoneproject.repository.PaymentRepository;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.service.StripePaymentService;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StripePaymentService stripePaymentService;

    @InjectMocks
    private PaymentService paymentService;

    private User testUser;
    private Order testOrder;
    private Payment testPayment;
    private PaymentRequestDto paymentRequestDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setTotalAmount(1999.98);
        testOrder.setOrderDate(new Date());

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrder(testOrder);
        testPayment.setAmount(1999.98);
        testPayment.setMethod(PaymentMethod.CREDIT_CARD);
        testPayment.setStatus(PaymentStatus.SUCCESS);
        testPayment.setTransactionId("txn-12345");
        testPayment.setPaymentDate(new Date());

        paymentRequestDto = new PaymentRequestDto();
        paymentRequestDto.setOrderId(1L);
        paymentRequestDto.setMethod(PaymentMethod.CREDIT_CARD);
        
        // Mock userRepository.findByEmail to return testUser (lenient because not all tests need it)
        lenient().when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testCreatePayment_Success_DelegatesToStripe() throws Exception {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Mock Stripe customer and payment intent
        when(stripePaymentService.createOrRetrieveCustomer(anyString(), anyString()))
                .thenReturn("cus_123");
        PaymentIntent paymentIntent = mock(PaymentIntent.class);
        when(paymentIntent.getId()).thenReturn("pi_123");
        when(paymentIntent.getClientSecret()).thenReturn("secret_123");
        when(stripePaymentService.createPaymentIntent(anyLong(), anyDouble(), anyString(), anyString(), anyString()))
                .thenReturn(paymentIntent);

        // Mock repository save
        testPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentResponseDto result = paymentService.createPayment(testUser.getEmail(), paymentRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(1999.98);
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        // Initial status should be PENDING until Stripe confirms via webhook
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);

        // createPayment calls orderRepository.findById once, and makePayment calls it again
        verify(orderRepository, times(2)).findById(1L);
        verify(stripePaymentService, times(1))
                .createOrRetrieveCustomer(eq(testUser.getEmail()), anyString());
        verify(stripePaymentService, times(1))
                .createPaymentIntent(eq(testOrder.getId()), eq(1999.98), anyString(), anyString(), anyString());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_OrderNotFound() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        paymentRequestDto.setOrderId(999L);

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(testUser.getEmail(), paymentRequestDto))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order with id 999 not found");
        verify(orderRepository, times(1)).findById(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_OrderDoesNotBelongToUser() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testOrder.setUser(otherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(testUser.getEmail(), paymentRequestDto))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order does not belong to user");
        verify(orderRepository, times(1)).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_InvalidOrderAmount() {
        // Given
        testOrder.setTotalAmount(null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(testUser.getEmail(), paymentRequestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order amount is invalid for payment");
        verify(orderRepository, times(1)).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testHandleSuccessfulStripePaymentIntent_UpdatesStatusAndOrder() throws Exception {
        // Given
        testOrder.setStatus(OrderStatus.PENDING);
        testPayment.setStatus(PaymentStatus.PENDING);
        testPayment.setTransactionId("pi_123");

        when(paymentRepository.findByTransactionId("pi_123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentIntent paymentIntent = mock(PaymentIntent.class);
        when(paymentIntent.getId()).thenReturn("pi_123");

        // When
        paymentService.handleSuccessfulStripePaymentIntent(paymentIntent);

        // Then
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(paymentRepository, times(1)).findByTransactionId("pi_123");
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testGetPayment_Success() throws PaymentNotFoundException, UserNotFoundException {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When
        PaymentResponseDto result = paymentService.getPayment(testUser.getEmail(), 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(1999.98);
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetPayment_NotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.getPayment(testUser.getEmail(), 999L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment with id 999 not found");
        verify(paymentRepository, times(1)).findById(999L);
    }

    @Test
    void testGetPayment_DoesNotBelongToUser() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testOrder.setUser(otherUser);
        testPayment.setOrder(testOrder);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When & Then
        assertThatThrownBy(() -> paymentService.getPayment(testUser.getEmail(), 1L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment does not belong to user");
        verify(paymentRepository, times(1)).findById(1L);
    }
}

