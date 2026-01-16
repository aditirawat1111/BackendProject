package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.model.Category;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.repository.CategoryRepository;
import com.aditi.backendcapstoneproject.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category
        Category category = new Category();
        category.setName("Electronics");
        category.setCreatedAt(new Date());
        category.setLastModified(new Date());
        category.setDeleted(false);
        category = categoryRepository.save(category);

        // Create test products
        Product product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("High performance laptop");
        product1.setPrice(999.99);
        product1.setImageUrl("https://example.com/laptop.jpg");
        product1.setCategory(category);
        product1.setCreatedAt(new Date());
        product1.setLastModified(new Date());
        product1.setDeleted(false);
        productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("Smartphone");
        product2.setDescription("Latest smartphone");
        product2.setPrice(699.99);
        product2.setImageUrl("https://example.com/phone.jpg");
        product2.setCategory(category);
        product2.setCreatedAt(new Date());
        product2.setLastModified(new Date());
        product2.setDeleted(false);
        productRepository.save(product2);
    }

    @Test
    void testGetProductById_Success() throws Exception {
        // Given
        Product product = productRepository.findAll().get(0);
        Long productId = product.getId();

        // When & Then
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllProducts_WithPagination() throws Exception {
        // When & Then
        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testSearchProducts_WithKeyword() throws Exception {
        // When & Then
        mockMvc.perform(get("/products/search")
                        .param("q", "Laptop")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));
    }

    @Test
    void testGetProductsByCategory() throws Exception {
        // When & Then
        mockMvc.perform(get("/products/by-category")
                        .param("category", "Electronics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateProduct_Success() throws Exception {
        // Given
        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setName("Headphones");
        requestDto.setDescription("Wireless headphones");
        requestDto.setPrice(199.99);
        requestDto.setImageUrl("https://example.com/headphones.jpg");
        requestDto.setCategory("Electronics");

        // When & Then
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Headphones"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateProduct_Success() throws Exception {
        // Given
        Product existingProduct = productRepository.findAll().get(0);

        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setName("Updated Laptop");
        requestDto.setDescription("Updated description");
        requestDto.setPrice(1099.99);
        requestDto.setImageUrl("https://example.com/updated-laptop.jpg");
        requestDto.setCategory(existingProduct.getCategory().getName());

        // When & Then
        mockMvc.perform(put("/products/{id}", existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingProduct.getId()))
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.price").value(1099.99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPartialUpdateProduct_Success() throws Exception {
        // Given
        Product existingProduct = productRepository.findAll().get(0);

        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setPrice(899.99);

        // When & Then
        mockMvc.perform(patch("/products/{id}", existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingProduct.getId()))
                .andExpect(jsonPath("$.price").value(899.99));
    }

}

