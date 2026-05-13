package com.example.OrderService.client;

import com.example.OrderService.config.InventoryFeignConfig;
import com.example.OrderService.dto.VendorProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(
        name = "INVENTORY-SERVICE",
        configuration = InventoryFeignConfig.class,
        url = "http://127.0.0.1:8080"
)
public interface InventoryServiceClient {

    @GetMapping("/api/inventory/{id}")
    VendorProductResponse getVendorProduct(@PathVariable Long id);

}
