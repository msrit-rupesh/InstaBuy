package com.example.PaymentService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StripeDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Min(value = 0,message = "Price can't be negative")
    private Long price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1,message = "Quantity must be atleast 1")
    private int quantity;
}
