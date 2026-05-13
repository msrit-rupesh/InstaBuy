package com.example.InventoryService.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {

    private String id;
    private String name;
    private String brand;
    private String category;
    private String imageUrl;
    private String description;
    private Double score;
    private Boolean deleted = false;
}

