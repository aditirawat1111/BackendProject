package com.aditi.backendcapstoneproject.model;

import com.aditi.backendcapstoneproject.enums.PaymentMethod;
import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
public class Payment extends BaseModel {

    @ManyToOne
    private Order order;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;
}


