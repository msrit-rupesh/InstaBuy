package com.example.InventoryService.dto;

import lombok.Data;

@Data
public class InventoryResponseDTO {

    private Long id;
    private String productId;
    private Double price;
    private Double discount;
    private boolean availability;

}