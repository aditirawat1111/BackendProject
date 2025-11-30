package com.aditi.backendcapstoneproject.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartResponseDto {

    private Long cartId;
    private List<CartItemResponseDto> items;
    private Integer totalItems;
    private Double totalAmount;

}

