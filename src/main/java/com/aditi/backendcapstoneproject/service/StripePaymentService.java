package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.exception.StripePaymentException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Customer;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.CustomerCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class StripePaymentService {

    private static final Logger logger = LoggerFactory.getLogger(StripePaymentService.class);

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.api.publishable-key}")
    private String stripePublishableKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        logger.info("Stripe API initialized with secret key");
    }

    /**
     * Create or retrieve a Stripe customer
     */
    public String createOrRetrieveCustomer(String email, String name) throws StripePaymentException {
        try {
            logger.info("Creating or retrieving Stripe customer for email: {}", email);
            
            // Search for existing customer by email
            var customers = Customer.list(
                com.stripe.param.CustomerListParams.builder()
                    .setEmail(email)
                    .build()
            );

            if (!customers.getData().isEmpty()) {
                String customerId = customers.getData().get(0).getId();
                logger.info("Found existing Stripe customer: {}", customerId);
                return customerId;
            }

            // Create new customer
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .build();

            Customer customer = Customer.create(params);
            logger.info("Created new Stripe customer: {}", customer.getId());
            return customer.getId();
        } catch (StripeException e) {
            logger.error("Error creating/retrieving Stripe customer: {}", e.getMessage(), e);
            throw new StripePaymentException("Failed to create/retrieve customer: " + e.getMessage(), e);
        }
    }

    /**
     * Create a payment intent for processing payment
     */
    public PaymentIntent createPaymentIntent(Long orderId, Double amount, String customerId, String currency, String description) 
            throws StripePaymentException {
        try {
            logger.info("Creating payment intent for order: {}, amount: {}, customer: {}", orderId, amount, customerId);
            
            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = (long) (amount * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency != null ? currency : "usd")
                    .setCustomer(customerId)
                    .setDescription(description != null ? description : "Payment for order #" + orderId)
                    .putMetadata("orderId", String.valueOf(orderId))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            logger.info("Payment intent created: {}, client secret: {}", 
                    paymentIntent.getId(), paymentIntent.getClientSecret());
            
            return paymentIntent;
        } catch (StripeException e) {
            logger.error("Error creating payment intent: {}", e.getMessage(), e);
            throw new StripePaymentException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve payment intent by ID
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripePaymentException {
        try {
            logger.info("Retrieving payment intent: {}", paymentIntentId);
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            logger.error("Error retrieving payment intent: {}", e.getMessage(), e);
            throw new StripePaymentException("Failed to retrieve payment intent: " + e.getMessage(), e);
        }
    }

    /**
     * Confirm a payment intent
     */
    public PaymentIntent confirmPaymentIntent(String paymentIntentId, String paymentMethodId) 
            throws StripePaymentException {
        try {
            logger.info("Confirming payment intent: {} with payment method: {}", paymentIntentId, paymentMethodId);
            
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            PaymentIntent confirmationParams = paymentIntent.confirm(
                    com.stripe.param.PaymentIntentConfirmParams.builder()
                            .setPaymentMethod(paymentMethodId)
                            .build()
            );
            
            logger.info("Payment intent confirmed: {}, status: {}", 
                    confirmationParams.getId(), confirmationParams.getStatus());
            
            return confirmationParams;
        } catch (StripeException e) {
            logger.error("Error confirming payment intent: {}", e.getMessage(), e);
            throw new StripePaymentException("Failed to confirm payment intent: " + e.getMessage(), e);
        }
    }

    /**
     * Get publishable key for frontend
     */
    public String getPublishableKey() {
        return stripePublishableKey;
    }
}
