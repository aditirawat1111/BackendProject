package com.aditi.backendcapstoneproject.dto;

import com.aditi.backendcapstoneproject.enums.PaymentMethod;
import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PaymentResponseDto {

    private Long paymentId;
    private Long orderId;
    private Double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private Date paymentDate;
}


