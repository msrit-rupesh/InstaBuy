package com.example.ProductService.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.TextScore;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Products")
public class Product {

    @Id
    @NotNull(message = "Product id is required")
    private String id;
    @NotBlank(message = "Product name is required")
    @TextIndexed(weight = 5)
    private String name;
    @NotBlank(message = "Brand name is required")
    @TextIndexed(weight = 3)
    private String brand;
    @NotBlank(message = "Category name is required")
    @TextIndexed(weight = 4)
    private String category;
    @NotBlank(message = "Image url is required")
    private String imageUrl;
    @TextIndexed(weight = 1)
    @NotBlank(message = "Description is required")
    private String description;
    @TextScore
    private Double score;
    private Boolean deleted = false;
}
