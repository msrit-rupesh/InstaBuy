package com.example.ProductService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    @NotBlank(message = "Product name is required")
    private String name;
    @NotBlank(message = "Brand name is required")
    private String brand;
    @NotBlank(message = "Category name is required")
    private String category;
    @NotBlank(message = "Image url is required")
    private String imageUrl;
    @NotBlank(message = "Description is required")
    private String description;
}
