package com.aditi.backendcapstoneproject.dto;

import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StripePaymentResponseDto {

    private Long paymentId;
    private Long orderId;
    private Double amount;
    private String stripePaymentIntentId;
    private String stripeCustomerId;
    private PaymentStatus status;
    private String transactionId;
    private String clientSecret;
    private String paymentUrl;
    private Date paymentDate;
    private String message;
}
