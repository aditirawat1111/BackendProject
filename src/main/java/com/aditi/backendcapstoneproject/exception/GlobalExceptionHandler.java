package com.aditi.backendcapstoneproject.exception;

import com.aditi.backendcapstoneproject.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponseDto buildErrorResponse(String status, String message) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto();
        errorResponseDto.setStatus(status);
        errorResponseDto.setMessage(message);
        errorResponseDto.setTimestamp(Instant.now());
        return errorResponseDto;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Internal Server Error",
                "An unexpected error occurred. Please contact support."
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException exception) {
        logger.warn("Access denied: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Access Denied",
                "You do not have permission to access this resource."
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthenticatedException(UnauthenticatedException exception) {
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Unauthenticated",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        logger.warn("Invalid request body: {}", exception.getMessage());
        String message = "Invalid request body. Please check your JSON format.";
        if (exception.getMessage() != null && exception.getMessage().contains("JSON")) {
            message = "Malformed JSON request. Please check your request body format.";
        }
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Bad Request",
                message
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> HandlingProductNotFoundException(ProductNotFoundException productNotFoundException){
        logger.warn("Product not found: {}", productNotFoundException.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Product Not Found",
                productNotFoundException.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExistsException(UserAlreadyExistsException exception){
        logger.warn("User already exists: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "User Already Exists",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentialsException(InvalidCredentialsException exception){
        logger.warn("Invalid credentials attempt: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Invalid Credentials",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException exception){
        logger.warn("User not found: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "User Not Found",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCartItemNotFoundException(CartItemNotFoundException exception){
        logger.warn("Cart item not found: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Cart Item Not Found",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderNotFoundException(OrderNotFoundException exception){
        logger.warn("Order not found: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Order Not Found",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ErrorResponseDto> handleEmptyCartException(EmptyCartException exception){
        logger.warn("Empty cart: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Cart Empty",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentNotFoundException(PaymentNotFoundException exception){
        logger.warn("Payment not found: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Payment Not Found",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException exception){
        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        logger.warn("Validation failed: {}", errorMessage);
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Validation Failed",
                errorMessage
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPasswordResetTokenException(
            InvalidPasswordResetTokenException exception) {
        logger.warn("Invalid password reset token: {}", exception.getMessage());
        ErrorResponseDto errorResponseDto = buildErrorResponse(
                "Invalid Password Reset Token",
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }
}
