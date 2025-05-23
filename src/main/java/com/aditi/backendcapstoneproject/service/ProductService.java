package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.FakeStoreProductDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;
import java.util.List;

public interface ProductService {

    public Product getProductsById(long id) throws ProductNotFoundException;
    public List<Product> getAllProducts();
    public Product createProduct(String title, String description, String category, double price, String image);
}
