package com.aditi.backendcapstoneproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequestDto {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
}


