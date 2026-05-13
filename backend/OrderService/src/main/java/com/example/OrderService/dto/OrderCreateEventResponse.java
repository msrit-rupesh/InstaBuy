package com.example.OrderService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateEventResponse {

    @NotNull(message = "Order id is required")
    private Long orderId;

    @NotNull(message = "Status is required")
    private String status;
}
