package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.AddToCartRequestDto;
import com.aditi.backendcapstoneproject.model.Category;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.CategoryRepository;
import com.aditi.backendcapstoneproject.repository.ProductRepository;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import com.aditi.backendcapstoneproject.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Product testProduct;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());
        testUser.setLastModified(new Date());
        testUser.setDeleted(false);
        testUser = userRepository.save(testUser);

        // Create user details for authentication
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities("ROLE_" + testUser.getRole())
                .build();

        // Create test category
        Category category = new Category();
        category.setName("Electronics");
        category.setCreatedAt(new Date());
        category.setLastModified(new Date());
        category.setDeleted(false);
        category = categoryRepository.save(category);

        // Create test product
        testProduct = new Product();
        testProduct.setName("Laptop");
        testProduct.setDescription("High performance laptop");
        testProduct.setPrice(999.99);
        testProduct.setImageUrl("https://example.com/laptop.jpg");
        testProduct.setCategory(category);
        testProduct.setCreatedAt(new Date());
        testProduct.setLastModified(new Date());
        testProduct.setDeleted(false);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testAddItemToCart_Success() throws Exception {
        // Given
        AddToCartRequestDto requestDto = new AddToCartRequestDto();
        requestDto.setProductId(testProduct.getId());
        requestDto.setQuantity(2);

        // When & Then
        mockMvc.perform(post("/cart/items")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").exists())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalAmount").value(1999.98));
    }

    @Test
    void testGetCart_Success() throws Exception {
        // Given - First add an item
        AddToCartRequestDto requestDto = new AddToCartRequestDto();
        requestDto.setProductId(testProduct.getId());
        requestDto.setQuantity(1);

        mockMvc.perform(post("/cart/items")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // When & Then
        mockMvc.perform(get("/cart")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").exists())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void testGetCart_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateCartItem_Success() throws Exception {
        // Given - first add an item
        AddToCartRequestDto requestDto = new AddToCartRequestDto();
        requestDto.setProductId(testProduct.getId());
        requestDto.setQuantity(1);

        String cartResponse = mockMvc.perform(post("/cart/items")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long itemId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(cartResponse)
                .get("items").get(0).get("id").asLong();

        com.aditi.backendcapstoneproject.dto.UpdateCartItemRequestDto updateRequest =
                new com.aditi.backendcapstoneproject.dto.UpdateCartItemRequestDto();
        updateRequest.setQuantity(3);

        // When & Then
        mockMvc.perform(put("/cart/items/{itemId}", itemId)
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(3));
    }

    @Test
    void testRemoveCartItem_Success() throws Exception {
        // Given - first add an item
        AddToCartRequestDto requestDto = new AddToCartRequestDto();
        requestDto.setProductId(testProduct.getId());
        requestDto.setQuantity(1);

        String cartResponse = mockMvc.perform(post("/cart/items")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long itemId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(cartResponse)
                .get("items").get(0).get("id").asLong();

        // When & Then
        mockMvc.perform(delete("/cart/items/{itemId}", itemId)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void testClearCart_Success() throws Exception {
        // Given - first add an item
        AddToCartRequestDto requestDto = new AddToCartRequestDto();
        requestDto.setProductId(testProduct.getId());
        requestDto.setQuantity(2);

        mockMvc.perform(post("/cart/items")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)));

        // When & Then
        mockMvc.perform(delete("/cart")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));
    }
}

