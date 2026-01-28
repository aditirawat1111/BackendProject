package com.aditi.backendcapstoneproject.repository;

import com.aditi.backendcapstoneproject.enums.PaymentStatus;
import com.aditi.backendcapstoneproject.model.Order;
import com.aditi.backendcapstoneproject.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrder(Order order);

    Optional<Payment> findByOrder_Id(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, Date createdAt);

    List<Payment> findByStatusAndLastModifiedBeforeOrderByCreatedAtAsc(
            PaymentStatus status,
            Date lastModified,
            Pageable pageable
    );
}


