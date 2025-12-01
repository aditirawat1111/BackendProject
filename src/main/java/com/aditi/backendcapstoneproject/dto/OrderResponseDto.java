package com.aditi.backendcapstoneproject.dto;

import com.aditi.backendcapstoneproject.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto {

    private Long orderId;
    private Date orderDate;
    private OrderStatus status;
    private Double totalAmount;
    private String deliveryAddress;
    private List<OrderItemResponseDto> items;
    private Integer totalItems;
}


