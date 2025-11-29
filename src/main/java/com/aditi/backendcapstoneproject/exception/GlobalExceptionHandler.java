package com.aditi.backendcapstoneproject.exception;

import com.aditi.backendcapstoneproject.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NullPointerException.class)
    public ErrorResponseDto ExceptionHandler(){
        ErrorResponseDto errorResponseDto=new ErrorResponseDto();
        errorResponseDto.setMessage("Response Failure");
        errorResponseDto.setStatus("Null Point Exception Found");

        return errorResponseDto;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> HandlingProductNotFoundException(ProductNotFoundException productNotFoundException){
        ErrorResponseDto errorResponseDto=new ErrorResponseDto();
        errorResponseDto.setStatus("Product Not Found");
        errorResponseDto.setMessage(productNotFoundException.getMessage());

        ResponseEntity<ErrorResponseDto> responseEntity=
                new ResponseEntity(errorResponseDto, HttpStatus.NOT_FOUND);

        return responseEntity;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExistsException(UserAlreadyExistsException exception){
        ErrorResponseDto errorResponseDto = new ErrorResponseDto();
        errorResponseDto.setStatus("User Already Exists");
        errorResponseDto.setMessage(exception.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentialsException(InvalidCredentialsException exception){
        ErrorResponseDto errorResponseDto = new ErrorResponseDto();
        errorResponseDto.setStatus("Invalid Credentials");
        errorResponseDto.setMessage(exception.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException exception){
        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDto errorResponseDto = new ErrorResponseDto();
        errorResponseDto.setStatus("Validation Failed");
        errorResponseDto.setMessage(errorMessage);

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }
}
