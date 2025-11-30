package com.aditi.backendcapstoneproject.repository;

import com.aditi.backendcapstoneproject.model.Cart;
import com.aditi.backendcapstoneproject.model.CartItem;
import com.aditi.backendcapstoneproject.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart(Cart cart);
    
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    void deleteByCart(Cart cart);

}

