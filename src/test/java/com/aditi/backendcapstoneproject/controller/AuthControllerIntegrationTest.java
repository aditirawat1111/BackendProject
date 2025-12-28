package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.LoginRequestDto;
import com.aditi.backendcapstoneproject.dto.RegisterRequestDto;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        // Given
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail("newuser@example.com");
        registerDto.setPassword("password123");
        registerDto.setName("New User");
        registerDto.setPhoneNumber("1234567890");
        registerDto.setAddress("123 Test St");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.name").value("New User"));
    }

    @Test
    void testRegister_UserAlreadyExists() throws Exception {
        // Given
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setName("Existing User");
        existingUser.setRole("USER");
        existingUser.setCreatedAt(new Date());
        existingUser.setLastModified(new Date());
        existingUser.setDeleted(false);
        userRepository.save(existingUser);

        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail("existing@example.com");
        registerDto.setPassword("password123");
        registerDto.setName("New User");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Test User");
        user.setRole("USER");
        user.setCreatedAt(new Date());
        user.setLastModified(new Date());
        user.setDeleted(false);
        userRepository.save(user);

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }
}

