package com.aditi.backendcapstoneproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetTokenResponseDto {

    private String message;
    private String token;
}


