package com.aditi.backendcapstoneproject.scheduler;

import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import com.aditi.backendcapstoneproject.exception.PaymentNotFoundException;
import com.aditi.backendcapstoneproject.exception.StripePaymentException;
import com.aditi.backendcapstoneproject.model.Payment;
import com.aditi.backendcapstoneproject.repository.PaymentRepository;
import com.aditi.backendcapstoneproject.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class PaymentStatusSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStatusSyncScheduler.class);

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Value("${stripe.sync.enabled:true}")
    private boolean syncEnabled;

    public PaymentStatusSyncScheduler(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    /**
     * Synchronize pending payments with Stripe every 5 minutes
     * Cron expression: every 5 minutes
     */
    @Scheduled(cron = "${stripe.sync.cron:0 0/5 * * * ?}")
    public void syncPendingPayments() {
        if (!syncEnabled) {
            logger.debug("Payment status synchronization is disabled");
            return;
        }

        logger.info("Starting payment status synchronization with Stripe");

        try {
            // First, expire stale pending payments older than 24 hours
            paymentService.expireStalePendingPayments();

            // Then, only sync a limited batch of oldest pending payments
            Instant lastModifiedCutoffInstant = Instant.now().minus(1, ChronoUnit.HOURS);
            Date lastModifiedCutoff = Date.from(lastModifiedCutoffInstant);

            List<Payment> pendingPayments = paymentService.findOldestPendingPaymentsNotUpdatedSince(
                    lastModifiedCutoff,
                    50
            );

            if (pendingPayments.isEmpty()) {
                logger.debug("No eligible pending payments to synchronize");
                return;
            }

            logger.info("Found {} eligible pending payments to synchronize", pendingPayments.size());

            int successCount = 0;
            int errorCount = 0;

            for (Payment payment : pendingPayments) {
                if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
                    logger.warn("Payment {} has no transaction ID, skipping", payment.getId());
                    continue;
                }

                try {
                    paymentService.synchronizePaymentStatus(payment.getTransactionId());
                    successCount++;
                    logger.debug("Successfully synchronized payment: {}", payment.getId());
                } catch (PaymentNotFoundException e) {
                    logger.warn("Payment not found during sync: {}", e.getMessage());
                    errorCount++;
                } catch (StripePaymentException e) {
                    logger.error("Stripe error during payment sync for payment {}: {}", 
                            payment.getId(), e.getMessage());
                    errorCount++;
                } catch (Exception e) {
                    logger.error("Unexpected error during payment sync for payment {}: {}", 
                            payment.getId(), e.getMessage(), e);
                    errorCount++;
                }
            }

            logger.info("Payment synchronization completed: {} succeeded, {} failed", 
                    successCount, errorCount);
        } catch (Exception e) {
            logger.error("Error during payment status synchronization: {}", e.getMessage(), e);
        }
    }
}
