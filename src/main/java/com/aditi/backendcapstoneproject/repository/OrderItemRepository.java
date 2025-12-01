package com.aditi.backendcapstoneproject.repository;

import com.aditi.backendcapstoneproject.model.Order;
import com.aditi.backendcapstoneproject.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);
}


