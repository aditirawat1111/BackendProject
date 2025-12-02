package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;

import java.util.List;

public interface ProductService {

    Product getProductsById(Long id) throws ProductNotFoundException;

    List<Product> getAllProducts();

    Product createProduct(String title, String description, String category, double price, String image);

    Product updateProduct(Long id, ProductRequestDto productRequestDto) throws ProductNotFoundException;

    Product partialUpdateProduct(Long id, ProductRequestDto productRequestDto) throws ProductNotFoundException;

    List<Product> searchProducts(String keyword);

    /**
     * Returns all products that belong to the given category name.
     */
    List<Product> getProductsByCategory(String categoryName);
}
