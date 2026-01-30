package com.aditi.backendcapstoneproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Category extends BaseModel{

    private String description;

    /**
     * Avoid serializing the back-reference to products.
     * This prevents lazy-loading errors and infinite recursion
     * when caching Product entities in Redis.
     */
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Product> productList;

}