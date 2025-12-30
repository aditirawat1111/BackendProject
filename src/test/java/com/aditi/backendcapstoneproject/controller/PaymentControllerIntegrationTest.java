package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.PaymentRequestDto;
import com.aditi.backendcapstoneproject.enums.OrderStatus;
import com.aditi.backendcapstoneproject.enums.PaymentMethod;
import com.aditi.backendcapstoneproject.model.*;
import com.aditi.backendcapstoneproject.repository.*;
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
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Order testOrder;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        orderRepository.deleteAll();
        orderItemRepository.deleteAll();
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
        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("High performance laptop");
        product.setPrice(999.99);
        product.setImageUrl("https://example.com/laptop.jpg");
        product.setCategory(category);
        product.setCreatedAt(new Date());
        product.setLastModified(new Date());
        product.setDeleted(false);
        product = productRepository.save(product);

        // Create test order
        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setOrderDate(new Date());
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setDeliveryAddress("123 Test St");
        testOrder.setTotalAmount(1999.98);
        testOrder.setCreatedAt(new Date());
        testOrder.setLastModified(new Date());
        testOrder.setDeleted(false);
        testOrder = orderRepository.save(testOrder);

        // Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(testOrder);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(999.99);
        orderItem.setCreatedAt(new Date());
        orderItem.setLastModified(new Date());
        orderItem.setDeleted(false);
        orderItemRepository.save(orderItem);
    }

    @Test
    void testCreatePayment_Success() throws Exception {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setOrderId(testOrder.getId());
        requestDto.setMethod(PaymentMethod.CREDIT_CARD);

        // When & Then
        mockMvc.perform(post("/payments")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.amount").value(1999.98))
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testGetPayment_Success() throws Exception {
        // Given - First create a payment
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setOrderId(testOrder.getId());
        requestDto.setMethod(PaymentMethod.CREDIT_CARD);

        String response = mockMvc.perform(post("/payments")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long paymentId = objectMapper.readTree(response).get("paymentId").asLong();

        // When & Then
        mockMvc.perform(get("/payments/{paymentId}", paymentId)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.amount").value(1999.98));
    }

    @Test
    void testGetPayment_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/payments/{paymentId}", 999L)
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }
}


