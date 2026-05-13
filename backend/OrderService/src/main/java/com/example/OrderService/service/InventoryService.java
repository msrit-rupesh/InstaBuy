package com.example.OrderService.service;

import com.example.OrderService.client.InventoryServiceClient;

import com.example.OrderService.dto.UpdateQuantityRequestDTO;
import com.example.OrderService.dto.VendorProductResponse;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final InventoryServiceClient inventoryServiceClient;

    public InventoryService(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public VendorProductResponse getVendorProduct(Long id) {
        return inventoryServiceClient.getVendorProduct(id);
    }

}
