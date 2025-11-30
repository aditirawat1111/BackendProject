package com.aditi.backendcapstoneproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDto {

    private String name;
    private String phoneNumber;
    private String address;

}

