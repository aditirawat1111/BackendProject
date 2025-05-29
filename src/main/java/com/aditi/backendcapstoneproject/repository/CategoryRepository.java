package com.aditi.backendcapstoneproject.repository;

import com.aditi.backendcapstoneproject.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
    Category save(Category category);
}
