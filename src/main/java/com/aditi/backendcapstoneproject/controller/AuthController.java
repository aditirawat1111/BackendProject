package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.AuthResponseDto;
import com.aditi.backendcapstoneproject.dto.LoginRequestDto;
import com.aditi.backendcapstoneproject.dto.RegisterRequestDto;
import com.aditi.backendcapstoneproject.exception.InvalidCredentialsException;
import com.aditi.backendcapstoneproject.exception.UserAlreadyExistsException;
import com.aditi.backendcapstoneproject.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
}

