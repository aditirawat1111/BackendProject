package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.PaymentRequestDto;
import com.aditi.backendcapstoneproject.dto.PaymentResponseDto;
import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.exception.PaymentNotFoundException;
import com.aditi.backendcapstoneproject.model.Order;
import com.aditi.backendcapstoneproject.model.Payment;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.OrderRepository;
import com.aditi.backendcapstoneproject.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponseDto createPayment(User user, PaymentRequestDto request) throws OrderNotFoundException {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + request.getOrderId() + " not found"));

        if (order.getUser().getId() != user.getId()) {
            throw new OrderNotFoundException("Order does not belong to user");
        }

        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
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

        // Optionally update order status to CONFIRMED if payment succeeds
        order.setLastModified(new Date());
        orderRepository.save(order);

        return buildPaymentResponse(payment);
    }

    public PaymentResponseDto getPayment(User user, Long paymentId) throws PaymentNotFoundException {
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


