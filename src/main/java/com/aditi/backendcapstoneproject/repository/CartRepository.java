package com.aditi.backendcapstoneproject.repository;

import com.aditi.backendcapstoneproject.model.Cart;
import com.aditi.backendcapstoneproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
    
    Optional<Cart> findByUser_Id(Long userId);

}

