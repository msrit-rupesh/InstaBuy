package com.example.PaymentService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    @NotNull(message = "Stock id is required")
    private Long stockId;

    @Min(value = 1,message = "Quantity can't be negative")
    private int quantity;

}
