package com.example.InventoryService.client;


import com.example.InventoryService.dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "PRODUCT-SERVICE",
        url = "http://127.0.0.1:8080"
)
public interface ProductServiceClient {

    @GetMapping("api/products/{id}")

    ProductResponseDTO getProductById(
            @PathVariable("id") String id,
            @RequestHeader("Authorization") String authorization
    );

}

