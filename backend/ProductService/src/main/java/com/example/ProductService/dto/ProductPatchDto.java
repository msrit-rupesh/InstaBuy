package com.example.ProductService.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPatchDto {

    private Optional<String> name = Optional.empty();
    private Optional<String> brand = Optional.empty();
    private Optional<String> category = Optional.empty();
    private Optional<String> imageUrl = Optional.empty();
    private Optional<String> description = Optional.empty();
}


