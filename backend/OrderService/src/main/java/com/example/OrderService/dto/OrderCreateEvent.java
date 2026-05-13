package com.example.OrderService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateEvent {

    @NotNull(message = "Stock id is required")
    private Long orderId;

    private String event;
    private List<OrderItemDTO> items;
}
