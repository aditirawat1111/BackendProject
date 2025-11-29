package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.AuthResponseDto;
import com.aditi.backendcapstoneproject.dto.LoginRequestDto;
import com.aditi.backendcapstoneproject.dto.RegisterRequestDto;
import com.aditi.backendcapstoneproject.exception.InvalidCredentialsException;
import com.aditi.backendcapstoneproject.exception.UserAlreadyExistsException;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDto register(RegisterRequestDto request) throws UserAlreadyExistsException {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setRole("USER");
        user.setCreatedAt(new Date());
        user.setLastModified(new Date());
        user.setDeleted(false);

        // Save user to database
        User savedUser = userRepository.save(user);

        // Generate JWT token
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(savedUser.getEmail())
                .password(savedUser.getPassword())
                .authorities("ROLE_" + savedUser.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        // Build response
        AuthResponseDto response = new AuthResponseDto();
        response.setToken(token);
        response.setEmail(savedUser.getEmail());
        response.setName(savedUser.getName());
        response.setRole(savedUser.getRole());
        response.setId(savedUser.getId());

        return response;
    }

    public AuthResponseDto login(LoginRequestDto request) throws InvalidCredentialsException {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // User authenticated successfully, load user details
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            // Generate JWT token
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getRole())
                    .build();

            String token = jwtService.generateToken(userDetails);

            // Build response
            AuthResponseDto response = new AuthResponseDto();
            response.setToken(token);
            response.setEmail(user.getEmail());
            response.setName(user.getName());
            response.setRole(user.getRole());
            response.setId(user.getId());

            return response;
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}

