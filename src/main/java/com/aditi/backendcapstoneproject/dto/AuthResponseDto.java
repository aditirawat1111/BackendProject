package com.aditi.backendcapstoneproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {

    private String token;
    private String email;
    private String name;
    private String role;
    private Long id;

}

