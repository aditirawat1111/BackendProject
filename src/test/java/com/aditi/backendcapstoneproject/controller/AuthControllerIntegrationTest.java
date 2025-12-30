package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.ForgotPasswordRequestDto;
import com.aditi.backendcapstoneproject.dto.LoginRequestDto;
import com.aditi.backendcapstoneproject.dto.PasswordResetRequestDto;
import com.aditi.backendcapstoneproject.dto.RegisterRequestDto;
import com.aditi.backendcapstoneproject.dto.UpdateProfileRequestDto;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Common authenticated user for profile-related tests
        testUser = new User();
        testUser.setEmail("profile@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Profile User");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());
        testUser.setLastModified(new Date());
        testUser.setDeleted(false);
        testUser = userRepository.save(testUser);

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities("ROLE_" + testUser.getRole())
                .build();
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
                .andExpect(status().isConflict());
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

    @Test
    void testForgotPassword_Success() throws Exception {
        // Given
        User user = new User();
        user.setEmail("forgot@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Forgot User");
        user.setRole("USER");
        user.setCreatedAt(new Date());
        user.setLastModified(new Date());
        user.setDeleted(false);
        userRepository.save(user);

        ForgotPasswordRequestDto requestDto = new ForgotPasswordRequestDto();
        requestDto.setEmail("forgot@example.com");

        // When & Then
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testResetPassword_Success() throws Exception {
        // 1. Trigger forgot-password for the user created in setUp()
        ForgotPasswordRequestDto forgotDto = new ForgotPasswordRequestDto();
        forgotDto.setEmail("profile@example.com"); // Using the user from setUp

        String response = mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 2. Extract the REAL token generated by your service
        String realToken = com.jayway.jsonpath.JsonPath.read(response, "$.token");

        // 3. Prepare the Reset DTO using that REAL token
        PasswordResetRequestDto resetDto = new PasswordResetRequestDto();
        resetDto.setToken(realToken); // This is the key fix!
        resetDto.setNewPassword("newPassword123");

        // 4. Act & Assert: Send the resetDto containing the real token
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetDto))) // Use resetDto here
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetProfile_Success() throws Exception {
        // Given - authenticate as testUser
        Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When & Then
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.name").value(testUser.getName()));
    }

    @Test
    void testUpdateProfile_Unauthorized() throws Exception {
        UpdateProfileRequestDto requestDto = new UpdateProfileRequestDto();
        requestDto.setName("Updated Name");

        mockMvc.perform(put("/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
        // Given - authenticate as testUser
        Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UpdateProfileRequestDto requestDto = new UpdateProfileRequestDto();
        requestDto.setName("Updated Profile User");
        requestDto.setPhoneNumber("9876543210");
        requestDto.setAddress("456 Updated St");

        // When & Then
        mockMvc.perform(put("/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Profile User"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.address").value("456 Updated St"));
    }
}

