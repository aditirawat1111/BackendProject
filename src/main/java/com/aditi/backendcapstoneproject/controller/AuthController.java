package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.AuthResponseDto;
import com.aditi.backendcapstoneproject.dto.ForgotPasswordRequestDto;
import com.aditi.backendcapstoneproject.dto.LoginRequestDto;
import com.aditi.backendcapstoneproject.dto.PasswordResetRequestDto;
import com.aditi.backendcapstoneproject.dto.PasswordResetTokenResponseDto;
import com.aditi.backendcapstoneproject.dto.ProfileResponseDto;
import com.aditi.backendcapstoneproject.dto.RegisterRequestDto;
import com.aditi.backendcapstoneproject.dto.UpdateProfileRequestDto;
import com.aditi.backendcapstoneproject.exception.InvalidCredentialsException;
import com.aditi.backendcapstoneproject.exception.InvalidPasswordResetTokenException;
import com.aditi.backendcapstoneproject.exception.UserAlreadyExistsException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.service.AuthenticationService;
import com.aditi.backendcapstoneproject.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for user authentication, registration, and profile management")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request)
            throws UserAlreadyExistsException {
        AuthResponseDto response = authenticationService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request)
            throws InvalidCredentialsException {
        AuthResponseDto response = authenticationService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Request password reset", description = "Generates a password reset token for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset token generated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetTokenResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request)
            throws UserNotFoundException {
        PasswordResetTokenResponseDto response = authenticationService.requestPasswordReset(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Reset password", description = "Resets user password using the reset token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody PasswordResetRequestDto request)
            throws InvalidPasswordResetTokenException {
        authenticationService.resetPassword(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get current user profile", description = "Retrieves the authenticated user's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getProfile(Authentication authentication)
            throws UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        ProfileResponseDto response = authenticationService.getProfile(email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Update user profile", description = "Updates the authenticated user's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/profile")
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDto request,
            Authentication authentication)
            throws UserNotFoundException {
        String email = SecurityUtils.getCurrentUserEmail(authentication);
        ProfileResponseDto response = authenticationService.updateProfile(email, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

