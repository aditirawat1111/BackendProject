package com.aditi.backendcapstoneproject.exception;

import com.aditi.backendcapstoneproject.dto.ErrorResponseDto;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ErrorResponseDto HandlingProductNotFoundException(ProductNotFoundException productNotFoundException){
        ErrorResponseDto errorResponseDto=new ErrorResponseDto();
        errorResponseDto.setStatus("Product Not Found");
        errorResponseDto.setMessage(productNotFoundException.getMessage());

        return errorResponseDto;
    }
}
