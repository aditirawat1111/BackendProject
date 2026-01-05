package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.PaymentRequestDto;
import com.aditi.backendcapstoneproject.dto.PaymentResponseDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.exception.PaymentNotFoundException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.model.Order;
import com.aditi.backendcapstoneproject.model.Payment;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.OrderRepository;
import com.aditi.backendcapstoneproject.repository.PaymentRepository;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    private User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public PaymentResponseDto createPayment(String email, PaymentRequestDto request) throws OrderNotFoundException, UserNotFoundException {
        logger.info("Creating payment for user: {}, order ID: {}, method: {}", 
                email, request.getOrderId(), request.getMethod());
        
        User user = getUserByEmail(email);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    logger.warn("Payment creation failed: Order with id {} not found", request.getOrderId());
                    return new OrderNotFoundException("Order with id " + request.getOrderId() + " not found");
                });

        if (order.getUser().getId() != user.getId()) {
            logger.warn("Payment creation failed: Order {} does not belong to user {}", 
                    request.getOrderId(), email);
            throw new OrderNotFoundException("Order does not belong to user");
        }

        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
            logger.error("Payment creation failed: Invalid order amount {} for order ID: {}", 
                    order.getTotalAmount(), request.getOrderId());
            throw new IllegalStateException("Order amount is invalid for payment");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.SUCCESS); // Mock success
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentDate(new Date());
        payment.setCreatedAt(new Date());
        payment.setLastModified(new Date());
        payment.setDeleted(false);

        payment = paymentRepository.save(payment);
        logger.info("Payment created successfully with ID: {}, transaction ID: {}, amount: {}", 
                payment.getId(), payment.getTransactionId(), payment.getAmount());

        // Automatically update order status to CONFIRMED when payment succeeds
        if (payment.getStatus() == PaymentStatus.SUCCESS && order.getStatus() == OrderStatus.PENDING) {
            logger.info("Payment successful, updating order status from PENDING to CONFIRMED for order ID: {}", 
                    order.getId());
            order.setStatus(OrderStatus.CONFIRMED);
            order.setLastModified(new Date());
            orderRepository.save(order);
        }

        return buildPaymentResponse(payment);
    }

    public PaymentResponseDto getPayment(String email, Long paymentId) throws PaymentNotFoundException, UserNotFoundException {
        User user = getUserByEmail(email);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with id " + paymentId + " not found"));

        if (payment.getOrder() == null || payment.getOrder().getUser() == null
                || payment.getOrder().getUser().getId() != user.getId()) {
            throw new PaymentNotFoundException("Payment does not belong to user");
        }

        return buildPaymentResponse(payment);
    }

    private PaymentResponseDto buildPaymentResponse(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setPaymentId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentDate(payment.getPaymentDate());
        return dto;
    }
}


