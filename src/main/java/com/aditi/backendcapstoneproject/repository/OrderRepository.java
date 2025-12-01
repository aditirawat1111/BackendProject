package com.aditi.backendcapstoneproject.repository;

import com.aditi.backendcapstoneproject.model.Order;
import com.aditi.backendcapstoneproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    List<Order> findByUser_Id(Long userId);
}


