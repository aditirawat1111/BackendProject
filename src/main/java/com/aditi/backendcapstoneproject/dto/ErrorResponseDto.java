package com.aditi.backendcapstoneproject.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ErrorResponseDto {
    private String status;
    private String message;
    private Instant timestamp;

}
