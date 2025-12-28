package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Product getProductsById(Long id) throws ProductNotFoundException;

    List<Product> getAllProducts();

    /**
     * Paginated list of all products with sorting support.
     */
    Page<Product> getAllProducts(Pageable pageable);

    Product createProduct(String title, String description, String category, Double price, String image);

    Product updateProduct(Long id, ProductRequestDto productRequestDto) throws ProductNotFoundException;

    Product partialUpdateProduct(Long id, ProductRequestDto productRequestDto) throws ProductNotFoundException;

    List<Product> searchProducts(String keyword);

    /**
     * Paginated search by keyword (name / description, case-insensitive).
     */
    Page<Product> searchProducts(String keyword, Pageable pageable);

    /**
     * Returns all products that belong to the given category name.
     */
    List<Product> getProductsByCategory(String categoryName);

    /**
     * Paginated list of products filtered by category name.
     */
    Page<Product> getProductsByCategory(String categoryName, Pageable pageable);
}
