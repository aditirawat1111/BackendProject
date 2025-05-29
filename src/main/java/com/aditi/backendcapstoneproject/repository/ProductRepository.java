package com.aditi.backendcapstoneproject.repository;

import com.aditi.backendcapstoneproject.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//JPA repository:
//1st argument=table/model name;
//2nd argument=type of the primary key for the table;
public interface ProductRepository extends JpaRepository<Product, Long> {

    Product save(Product product);

    List<Product> findAll();

    Optional<Product> findById(long id);
}
