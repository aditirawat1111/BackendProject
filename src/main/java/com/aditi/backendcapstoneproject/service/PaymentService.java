package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.PaymentRequestDto;
import com.aditi.backendcapstoneproject.dto.PaymentResponseDto;
import com.aditi.backendcapstoneproject.dto.StripePaymentRequestDto;
import com.aditi.backendcapstoneproject.dto.StripePaymentResponseDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.enums.PaymentMethod;
import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.exception.PaymentNotFoundException;
import com.aditi.backendcapstoneproject.exception.StripePaymentException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.model.Order;
import com.aditi.backendcapstoneproject.model.Payment;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.OrderRepository;
import com.aditi.backendcapstoneproject.repository.PaymentRepository;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StripePaymentService stripePaymentService;

    @Value("${stripe.webhook.success-url:}")
    private String successUrl;

    @Value("${stripe.webhook.cancel-url:}")
    private String cancelUrl;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          UserRepository userRepository,
                          StripePaymentService stripePaymentService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.stripePaymentService = stripePaymentService;
    }

    private User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public PaymentResponseDto createPayment(String email, PaymentRequestDto request) throws OrderNotFoundException, UserNotFoundException {
        logger.info("Creating payment via Stripe for user: {}, order ID: {}, method: {}",
                email, request.getOrderId(), request.getMethod());

        // Delegate to Stripe-based payment creation to ensure consistent flow
        StripePaymentRequestDto stripeRequest = new StripePaymentRequestDto();
        stripeRequest.setOrderId(request.getOrderId());

        // Load order to derive amount safely
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

        stripeRequest.setAmount(order.getTotalAmount());
        stripeRequest.setCustomerId(user.getEmail());

        // Reuse Stripe payment flow
        StripePaymentResponseDto stripeResponse;
        try {
            stripeResponse = makePayment(email, stripeRequest);
        } catch (StripePaymentException e) {
            logger.error("Stripe payment creation failed in createPayment: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to create payment via Stripe", e);
        }

        // Build a simplified PaymentResponseDto view from the Stripe response
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setPaymentId(stripeResponse.getPaymentId());
        responseDto.setOrderId(stripeResponse.getOrderId());
        responseDto.setAmount(stripeResponse.getAmount());
        responseDto.setMethod(PaymentMethod.CREDIT_CARD);
        responseDto.setStatus(stripeResponse.getStatus());
        responseDto.setTransactionId(stripeResponse.getTransactionId());
        responseDto.setPaymentDate(stripeResponse.getPaymentDate());

        return responseDto;
    }

    @Cacheable(cacheNames = "payments", key = "#email + ':' + #paymentId")
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

    /**
     * Create payment using Stripe Payment Gateway
     */
    @Transactional
    public StripePaymentResponseDto makePayment(String email, StripePaymentRequestDto request) 
            throws OrderNotFoundException, UserNotFoundException, StripePaymentException {
        logger.info("Processing Stripe payment for user: {}, order ID: {}, amount: {}", 
                email, request.getOrderId(), request.getAmount());
        
        // Validate user
        User user = getUserByEmail(email);
        
        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    logger.warn("Stripe payment failed: Order with id {} not found", request.getOrderId());
                    return new OrderNotFoundException("Order with id " + request.getOrderId() + " not found");
                });

        // Verify order belongs to user
        if (order.getUser().getId() != user.getId()) {
            logger.warn("Stripe payment failed: Order {} does not belong to user {}", 
                    request.getOrderId(), email);
            throw new OrderNotFoundException("Order does not belong to user");
        }

        // Validate amount
        if (request.getAmount() == null || request.getAmount() <= 0) {
            logger.error("Stripe payment failed: Invalid amount {} for order ID: {}", 
                    request.getAmount(), request.getOrderId());
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // Validate amount matches order total (with small tolerance for rounding)
        double amountDifference = Math.abs(order.getTotalAmount() - request.getAmount());
        if (amountDifference > 0.01) {
            logger.warn("Amount mismatch: Order total={}, Request amount={}", 
                    order.getTotalAmount(), request.getAmount());
            throw new IllegalArgumentException("Payment amount does not match order total");
        }

        // Check if payment already exists for this order
        Payment existingPayment = paymentRepository.findByOrder_Id(order.getId())
                .orElse(null);
        
        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.SUCCESS) {
            logger.warn("Payment already exists and is successful for order: {}", order.getId());
            throw new IllegalStateException("Payment already processed for this order");
        }

        try {
            // Create or retrieve Stripe customer
            String customerName = user.getEmail(); // Using email as name since User doesn't have firstName/lastName
            String stripeCustomerId = stripePaymentService.createOrRetrieveCustomer(
                    user.getEmail(), customerName);

            // Create payment intent in Stripe
            String description = request.getDescription() != null 
                    ? request.getDescription() 
                    : "Payment for order #" + order.getId();
            
            PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(
                    order.getId(),
                    request.getAmount(),
                    stripeCustomerId,
                    request.getCurrency(),
                    description
            );

            // Create payment record in database with PENDING status
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(request.getAmount());
            payment.setMethod(PaymentMethod.CREDIT_CARD); // Stripe typically handles card payments
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(paymentIntent.getId()); // Store Stripe payment intent ID
            payment.setPaymentDate(new Date());
            payment.setCreatedAt(new Date());
            payment.setLastModified(new Date());
            payment.setDeleted(false);

            payment = paymentRepository.save(payment);
            logger.info("Payment record created with ID: {}, Stripe payment intent: {}", 
                    payment.getId(), paymentIntent.getId());

            // Build response
            StripePaymentResponseDto response = new StripePaymentResponseDto();
            response.setPaymentId(payment.getId());
            response.setOrderId(order.getId());
            response.setAmount(request.getAmount());
            response.setStripePaymentIntentId(paymentIntent.getId());
            response.setStripeCustomerId(stripeCustomerId);
            response.setStatus(PaymentStatus.PENDING);
            response.setTransactionId(paymentIntent.getId());
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setPaymentDate(new Date());
            response.setMessage("Payment intent created. Use client_secret to complete payment on frontend.");

            return response;
        } catch (StripePaymentException e) {
            logger.error("Stripe payment processing failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update payment status from Stripe webhook
     */
    @Transactional
    @CacheEvict(cacheNames = "payments", allEntries = true)
    public void updatePaymentStatusFromStripe(String stripePaymentIntentId, PaymentStatus status) 
            throws PaymentNotFoundException {
        logger.info("Updating payment status from Stripe webhook: paymentIntent={}, status={}", 
                stripePaymentIntentId, status);

        Payment payment = paymentRepository.findByTransactionId(stripePaymentIntentId)
                .orElseThrow(() -> {
                    logger.warn("Payment not found for Stripe payment intent: {}", stripePaymentIntentId);
                    return new PaymentNotFoundException("Payment not found for transaction: " + stripePaymentIntentId);
                });

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(status);
        payment.setLastModified(new Date());

        if (status == PaymentStatus.SUCCESS) {
            payment.setPaymentDate(new Date());
        }

        payment = paymentRepository.save(payment);
        logger.info("Payment status updated: paymentId={}, oldStatus={}, newStatus={}", 
                payment.getId(), oldStatus, status);

        // Update order status if payment succeeded
        if (status == PaymentStatus.SUCCESS && payment.getOrder().getStatus() == OrderStatus.PENDING) {
            logger.info("Payment successful, updating order status from PENDING to CONFIRMED for order ID: {}", 
                    payment.getOrder().getId());
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CONFIRMED);
            order.setLastModified(new Date());
            orderRepository.save(order);
        }
    }

    /**
     * Handle successful Stripe webhook event (payment_intent.succeeded)
     */
    @Transactional
    public void handleSuccessfulStripePaymentIntent(PaymentIntent paymentIntent) {
        if (paymentIntent == null) {
            logger.warn("Received null PaymentIntent in handleSuccessfulStripePaymentIntent");
            return;
        }

        logger.info("Handling successful Stripe payment intent: {}", paymentIntent.getId());
        try {
            updatePaymentStatusFromStripe(paymentIntent.getId(), PaymentStatus.SUCCESS);
        } catch (PaymentNotFoundException e) {
            logger.warn("Payment not found for successful Stripe payment intent {}: {}", paymentIntent.getId(), e.getMessage());
        }
    }

    /**
     * Synchronize payment status with Stripe
     */
    @Transactional
    public void synchronizePaymentStatus(String stripePaymentIntentId) throws PaymentNotFoundException, StripePaymentException {
        logger.info("Synchronizing payment status with Stripe: paymentIntent={}", stripePaymentIntentId);

        Payment payment = paymentRepository.findByTransactionId(stripePaymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for transaction: " + stripePaymentIntentId));

        try {
            PaymentIntent paymentIntent = stripePaymentService.retrievePaymentIntent(stripePaymentIntentId);
            
            PaymentStatus newStatus = mapStripeStatusToPaymentStatus(paymentIntent.getStatus());
            
            if (payment.getStatus() != newStatus) {
                logger.info("Payment status changed: paymentId={}, oldStatus={}, newStatus={}", 
                        payment.getId(), payment.getStatus(), newStatus);
                updatePaymentStatusFromStripe(stripePaymentIntentId, newStatus);
            }
        } catch (StripePaymentException e) {
            logger.error("Failed to synchronize payment status: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Map Stripe payment intent status to our PaymentStatus enum
     */
    private PaymentStatus mapStripeStatusToPaymentStatus(String stripeStatus) {
        String status = stripeStatus.toLowerCase();
        if ("succeeded".equals(status)) {
            return PaymentStatus.SUCCESS;
        } else if ("processing".equals(status) || "requires_payment_method".equals(status) 
                || "requires_confirmation".equals(status) || "requires_action".equals(status) 
                || "requires_capture".equals(status)) {
            return PaymentStatus.PENDING;
        } else if ("canceled".equals(status) || "payment_failed".equals(status)) {
            return PaymentStatus.FAILED;
        } else {
            logger.warn("Unknown Stripe status: {}, defaulting to PENDING", stripeStatus);
            return PaymentStatus.PENDING;
        }
    }

    /**
     * Expire pending payments older than 24 hours and stop syncing them.
     */
    @Transactional
    public void expireStalePendingPayments() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        Date cutoffDate = Date.from(cutoff);

        logger.info("Expiring pending payments created before {}", cutoffDate);

        var stalePayments = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, cutoffDate);
        if (stalePayments.isEmpty()) {
            logger.debug("No stale pending payments to expire");
            return;
        }

        for (Payment payment : stalePayments) {
            logger.info("Marking payment {} as FAILED due to staleness (createdAt={})",
                    payment.getId(), payment.getCreatedAt());
            payment.setStatus(PaymentStatus.FAILED);
            payment.setLastModified(new Date());
        }

        paymentRepository.saveAll(stalePayments);
        logger.info("Expired {} stale pending payments", stalePayments.size());
    }

    /**
     * Find up to 'limit' oldest pending payments that haven't been updated since given cutoff.
     */
    @Transactional(readOnly = true)
    public java.util.List<Payment> findOldestPendingPaymentsNotUpdatedSince(Date lastModifiedBefore, int limit) {
        logger.info("Finding up to {} pending payments with lastModified before {}", limit, lastModifiedBefore);
        return paymentRepository.findByStatusAndLastModifiedBeforeOrderByCreatedAtAsc(
                PaymentStatus.PENDING,
                lastModifiedBefore,
                PageRequest.of(0, limit)
        );
    }
}


