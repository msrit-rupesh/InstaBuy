package com.example.InventoryService.service;

import com.example.InventoryService.client.ProductServiceClient;
import com.example.InventoryService.dto.ProductResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class ProductService{

    private final ProductServiceClient productServiceClient;

    public ProductService(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    public ProductResponseDTO getProductById(String productId,String authHeader) {
        return productServiceClient.getProductById(productId,authHeader);
    }
}