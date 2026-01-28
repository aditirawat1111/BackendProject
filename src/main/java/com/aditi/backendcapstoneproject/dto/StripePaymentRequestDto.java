package com.aditi.backendcapstoneproject.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StripePaymentRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Customer ID is required")
    private String customerId;

    private String paymentMethodId;
    
    private String currency = "usd";
    
    private String description;
}
