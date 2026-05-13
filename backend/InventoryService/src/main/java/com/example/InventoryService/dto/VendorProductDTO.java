package com.example.InventoryService.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.Instant;

@Data
public class VendorProductDTO {

    @NotBlank(message = "Product Id must not be null")
    private String productId;

    @NotNull(message = "Price must not be null")
    @Min(value = 1,message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Discount must not be null")
    @Min(value = 0,message = "Discount must be greater than equal to 0")
    @Max(value = 100,message = "Discount must be less than equal to 100")
    private Double discount;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1,message = "Quantity must be greater than 0")
    private Integer quantity;

}