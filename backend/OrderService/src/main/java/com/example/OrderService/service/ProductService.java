package com.example.OrderService.service;

import com.example.OrderService.client.ProductServiceClient;
import com.example.OrderService.dto.ProductResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class ProductService{

    private final ProductServiceClient productServiceClient;

    public ProductService(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    public ProductResponseDTO getProductById(String productId, String authHeader) {
        return productServiceClient.getProductById(productId,authHeader);
    }
}