package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.AuthResponseDto;
import com.aditi.backendcapstoneproject.dto.LoginRequestDto;
import com.aditi.backendcapstoneproject.dto.ProfileResponseDto;
import com.aditi.backendcapstoneproject.dto.RegisterRequestDto;
import com.aditi.backendcapstoneproject.dto.UpdateProfileRequestDto;
import com.aditi.backendcapstoneproject.exception.InvalidCredentialsException;
import com.aditi.backendcapstoneproject.exception.UserAlreadyExistsException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request)
            throws UserAlreadyExistsException {
        AuthResponseDto response = authenticationService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request)
            throws InvalidCredentialsException {
        AuthResponseDto response = authenticationService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getProfile(Authentication authentication)
            throws UserNotFoundException {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        ProfileResponseDto response = authenticationService.getProfile(email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDto request,
            Authentication authentication)
            throws UserNotFoundException {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        ProfileResponseDto response = authenticationService.updateProfile(email, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

