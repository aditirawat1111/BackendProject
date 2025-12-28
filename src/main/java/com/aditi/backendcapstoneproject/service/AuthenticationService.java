package com.aditi.backendcapstoneproject.service;

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
import com.aditi.backendcapstoneproject.model.PasswordResetToken;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.PasswordResetTokenRepository;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager,
                                 PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
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

    public ProfileResponseDto getProfile(String email) throws UserNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return ProfileResponseDto.from(user);
    }

    @Transactional
    public ProfileResponseDto updateProfile(String email, UpdateProfileRequestDto request) throws UserNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Update only provided fields
        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        user.setLastModified(new Date());

        User updatedUser = userRepository.save(user);
        return ProfileResponseDto.from(updatedUser);
    }

    @Transactional
    public PasswordResetTokenResponseDto requestPasswordReset(ForgotPasswordRequestDto request)
            throws UserNotFoundException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(java.util.UUID.randomUUID().toString());
        Date now = new Date();
        token.setCreatedAt(now);
        token.setLastModified(now);
        token.setDeleted(false);
        // Token valid for 1 hour
        token.setExpiryDate(new Date(now.getTime() + 60 * 60 * 1000));
        token.setUsed(false);

        passwordResetTokenRepository.save(token);

        PasswordResetTokenResponseDto responseDto = new PasswordResetTokenResponseDto();
        responseDto.setMessage("Password reset token generated successfully. (In production, this would be emailed.)");
        responseDto.setToken(token.getToken());
        return responseDto;
    }

    @Transactional
    public void resetPassword(PasswordResetRequestDto request)
            throws InvalidPasswordResetTokenException {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid password reset token"));

        Date now = new Date();
        if (token.isUsed() || token.getExpiryDate() == null || token.getExpiryDate().before(now)) {
            throw new InvalidPasswordResetTokenException("Password reset token is expired or has already been used");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastModified(now);
        userRepository.save(user);

        token.setUsed(true);
        token.setLastModified(now);
        passwordResetTokenRepository.save(token);
    }
}

