package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.*;
import com.aditi.backendcapstoneproject.exception.InvalidCredentialsException;
import com.aditi.backendcapstoneproject.exception.InvalidPasswordResetTokenException;
import com.aditi.backendcapstoneproject.exception.UserAlreadyExistsException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.model.PasswordResetToken;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.PasswordResetTokenRepository;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequestDto registerRequestDto;
    private LoginRequestDto loginRequestDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Test St");

        registerRequestDto = new RegisterRequestDto();
        registerRequestDto.setEmail("test@example.com");
        registerRequestDto.setPassword("password123");
        registerRequestDto.setName("Test User");
        registerRequestDto.setPhoneNumber("1234567890");
        registerRequestDto.setAddress("123 Test St");

        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password123");
    }

    @Test
    void testRegister_Success() throws UserAlreadyExistsException {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // When
        AuthResponseDto result = authenticationService.register(registerRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("Test User");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authenticationService.register(registerRequestDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email test@example.com already exists");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() throws InvalidCredentialsException {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // When
        AuthResponseDto result = authenticationService.login(loginRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtService, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.login(loginRequestDto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void testGetProfile_Success() throws UserNotFoundException {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        ProfileResponseDto result = authenticationService.getProfile("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testGetProfile_NotFound() {
        // Given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.getProfile("notfound@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with email: notfound@example.com");
        verify(userRepository, times(1)).findByEmail("notfound@example.com");
    }

    @Test
    void testUpdateProfile_Success() throws UserNotFoundException {
        // Given
        UpdateProfileRequestDto updateDto = new UpdateProfileRequestDto();
        updateDto.setName("Updated Name");
        updateDto.setPhoneNumber("9876543210");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("test@example.com");
        updatedUser.setName("Updated Name");
        updatedUser.setPhoneNumber("9876543210");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        ProfileResponseDto result = authenticationService.updateProfile("test@example.com", updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRequestPasswordReset_Success() throws UserNotFoundException {
        // Given
        ForgotPasswordRequestDto requestDto = new ForgotPasswordRequestDto();
        requestDto.setEmail("test@example.com");

        PasswordResetToken token = new PasswordResetToken();
        token.setId(1L);
        token.setUser(testUser);
        token.setToken("reset-token");
        token.setExpiryDate(new Date(System.currentTimeMillis() + 3600000));
        token.setUsed(false);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(token);

        // When
        PasswordResetTokenResponseDto result = authenticationService.requestPasswordReset(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    void testRequestPasswordReset_UserNotFound() {
        // Given
        ForgotPasswordRequestDto requestDto = new ForgotPasswordRequestDto();
        requestDto.setEmail("notfound@example.com");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.requestPasswordReset(requestDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with email: notfound@example.com");
        verify(userRepository, times(1)).findByEmail("notfound@example.com");
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void testResetPassword_Success() throws InvalidPasswordResetTokenException {
        // Given
        PasswordResetRequestDto requestDto = new PasswordResetRequestDto();
        requestDto.setToken("valid-token");
        requestDto.setNewPassword("newPassword123");

        PasswordResetToken token = new PasswordResetToken();
        token.setId(1L);
        token.setUser(testUser);
        token.setToken("valid-token");
        token.setExpiryDate(new Date(System.currentTimeMillis() + 3600000));
        token.setUsed(false);

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(token);

        // When
        authenticationService.resetPassword(requestDto);

        // Then
        verify(passwordResetTokenRepository, times(1)).findByToken("valid-token");
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    void testResetPassword_InvalidToken() {
        // Given
        PasswordResetRequestDto requestDto = new PasswordResetRequestDto();
        requestDto.setToken("invalid-token");
        requestDto.setNewPassword("newPassword123");

        when(passwordResetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.resetPassword(requestDto))
                .isInstanceOf(InvalidPasswordResetTokenException.class)
                .hasMessageContaining("Invalid password reset token");
        verify(passwordResetTokenRepository, times(1)).findByToken("invalid-token");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testResetPassword_ExpiredToken() {
        // Given
        PasswordResetRequestDto requestDto = new PasswordResetRequestDto();
        requestDto.setToken("expired-token");
        requestDto.setNewPassword("newPassword123");

        PasswordResetToken token = new PasswordResetToken();
        token.setId(1L);
        token.setUser(testUser);
        token.setToken("expired-token");
        token.setExpiryDate(new Date(System.currentTimeMillis() - 3600000)); // Expired
        token.setUsed(false);

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        // When & Then
        assertThatThrownBy(() -> authenticationService.resetPassword(requestDto))
                .isInstanceOf(InvalidPasswordResetTokenException.class)
                .hasMessageContaining("Password reset token is expired or has already been used");
        verify(passwordResetTokenRepository, times(1)).findByToken("expired-token");
        verify(userRepository, never()).save(any(User.class));
    }
}

