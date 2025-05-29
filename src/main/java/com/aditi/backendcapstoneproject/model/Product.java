package com.aditi.backendcapstoneproject.model;

import com.aditi.backendcapstoneproject.model.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Product extends BaseModel{

    private String description;
    private String imageUrl;
    private double price;

    @ManyToOne
    private Category category;

}
