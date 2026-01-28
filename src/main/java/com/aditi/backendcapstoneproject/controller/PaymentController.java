package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.PaymentRequestDto;
import com.aditi.backendcapstoneproject.dto.PaymentResponseDto;
import com.aditi.backendcapstoneproject.dto.StripePaymentRequestDto;
import com.aditi.backendcapstoneproject.dto.StripePaymentResponseDto;
import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import com.aditi.backendcapstoneproject.exception.OrderNotFoundException;
import com.aditi.backendcapstoneproject.exception.PaymentNotFoundException;
import com.aditi.backendcapstoneproject.exception.StripePaymentException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.service.PaymentService;
import com.aditi.backendcapstoneproject.util.SecurityUtils;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "APIs for payment processing - create and view payments")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Create a payment", description = "Processes payment for an order (User only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - User role required"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @Valid @RequestBody PaymentRequestDto request,
            Authentication authentication) throws OrderNotFoundException, UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        PaymentResponseDto responseDto = paymentService.createPayment(email, request);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get payment by ID", description = "Retrieves a specific payment by its ID (User only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "403", description = "Access denied - User role required"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId,
            Authentication authentication) throws PaymentNotFoundException, UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        PaymentResponseDto responseDto = paymentService.getPayment(email, paymentId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @Operation(summary = "Create Stripe payment", description = "Creates a payment intent using Stripe Payment Gateway (User only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment intent created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied - User role required"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Stripe payment processing failed")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/stripe/make-payment")
    public ResponseEntity<StripePaymentResponseDto> makePayment(
            @Valid @RequestBody StripePaymentRequestDto request,
            Authentication authentication) throws OrderNotFoundException, UserNotFoundException, StripePaymentException {
        logger.info("Received Stripe payment request for order: {}", request.getOrderId());
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        StripePaymentResponseDto responseDto = paymentService.makePayment(email, request);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Stripe webhook handler", description = "Receives webhook events from Stripe to update payment status")
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        logger.info("Received Stripe webhook event");

        if (stripeWebhookSecret == null || stripeWebhookSecret.isEmpty()) {
            logger.error("Stripe webhook secret is not configured");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook secret not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            logger.info("Webhook event verified: type={}, id={}", event.getType(), event.getId());
        } catch (SignatureVerificationException e) {
            logger.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing webhook");
        }

        // Handle the event - delegate business logic to service layer
        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null);
            paymentService.handleSuccessfulStripePaymentIntent(paymentIntent);
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null);
            if (paymentIntent != null) {
                logger.info("Payment failed: paymentIntentId={}", paymentIntent.getId());
                try {
                    paymentService.updatePaymentStatusFromStripe(paymentIntent.getId(), PaymentStatus.FAILED);
                } catch (PaymentNotFoundException e) {
                    logger.warn("Payment not found for webhook: {}", e.getMessage());
                }
            }
        } else if ("payment_intent.canceled".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null);
            if (paymentIntent != null) {
                logger.info("Payment canceled: paymentIntentId={}", paymentIntent.getId());
                try {
                    paymentService.updatePaymentStatusFromStripe(paymentIntent.getId(), PaymentStatus.FAILED);
                } catch (PaymentNotFoundException e) {
                    logger.warn("Payment not found for webhook: {}", e.getMessage());
                }
            }
        } else {
            logger.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook processed successfully");
    }

    @Operation(summary = "Payment callback handler", description = "Handles payment success/cancel callbacks from Stripe")
    @GetMapping("/stripe/callback")
    public ResponseEntity<Map<String, String>> handlePaymentCallback(
            @RequestParam(required = false) String payment_intent,
            @RequestParam(required = false) String payment_intent_client_secret,
            @RequestParam(required = false) String redirect_status) {
        logger.info("Received payment callback: payment_intent={}, redirect_status={}", 
                payment_intent, redirect_status);

        Map<String, String> response = Map.of(
                "status", redirect_status != null ? redirect_status : "unknown",
                "payment_intent", payment_intent != null ? payment_intent : "",
                "message", "Payment callback received"
        );

        return ResponseEntity.ok(response);
    }
}