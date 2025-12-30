package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.CreateOrderRequestDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.model.*;
import com.aditi.backendcapstoneproject.repository.*;
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
class OrderControllerIntegrationTest {

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
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        cartRepository.deleteAll();
        cartItemRepository.deleteAll();

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

        // Create test cart with items
        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.setCreatedAt(new Date());
        testCart.setLastModified(new Date());
        testCart.setDeleted(false);
        testCart = cartRepository.save(testCart);

        testCartItem = new CartItem();
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setCreatedAt(new Date());
        testCartItem.setLastModified(new Date());
        testCartItem.setDeleted(false);
        cartItemRepository.save(testCartItem);
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        // Given
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        requestDto.setDeliveryAddress("123 Test Street, City, State 12345");

        // When & Then
        mockMvc.perform(post("/orders")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.deliveryAddress").value("123 Test Street, City, State 12345"))
                .andExpect(jsonPath("$.totalAmount").value(1999.98));
    }

    @Test
    void testGetOrders_WithPagination() throws Exception {
        // Given - First create an order
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        requestDto.setDeliveryAddress("123 Test Street");

        mockMvc.perform(post("/orders")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // When & Then
        mockMvc.perform(get("/orders")
                        .with(user(userDetails))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void testGetOrderById_Success() throws Exception {
        // Given - First create an order
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        requestDto.setDeliveryAddress("123 Test Street");

        String response = mockMvc.perform(post("/orders")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(response).get("orderId").asLong();

        // When & Then
        mockMvc.perform(get("/orders/{orderId}", orderId)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testUpdateOrderStatus_AsAdmin_Success() throws Exception {
        // Given - first create an order as regular user
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        requestDto.setDeliveryAddress("123 Test Street");

        String response = mockMvc.perform(post("/orders")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(response).get("orderId").asLong();

        // And authenticate as admin for status update
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("adminpass"));
        adminUser.setName("Admin User");
        adminUser.setRole("ADMIN");
        adminUser.setCreatedAt(new Date());
        adminUser.setLastModified(new Date());
        adminUser.setDeleted(false);
        userRepository.save(adminUser);

        UserDetails adminDetails = org.springframework.security.core.userdetails.User.builder()
                .username(adminUser.getEmail())
                .password(adminUser.getPassword())
                .authorities("ROLE_ADMIN")
                .build();

        // When & Then
        mockMvc.perform(patch("/orders/{orderId}/status", orderId)
                        .with(user(adminDetails))
                        .param("status", OrderStatus.SHIPPED.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void testUpdateOrderStatus_AsNonAdmin_Forbidden() throws Exception {
        // Given - create an order as regular user
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        requestDto.setDeliveryAddress("123 Test Street");

        String response = mockMvc.perform(post("/orders")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(response).get("orderId").asLong();

        // When & Then - same non-admin user tries to update status
        mockMvc.perform(patch("/orders/{orderId}/status", orderId)
                        .with(user(userDetails))
                        .param("status", OrderStatus.CANCELLED.name()))
                .andExpect(status().isForbidden());
    }
}

