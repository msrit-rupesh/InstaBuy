package com.example.PaymentService.client;

import com.example.PaymentService.config.OrderFeignConfig;
import com.example.PaymentService.dto.OrderRequestDTO;
import com.example.PaymentService.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(
        name = "ORDER-SERVICE",
        configuration = OrderFeignConfig.class,
        url = "http://127.0.0.1:8080"
)
public interface OrderServiceClient {

    @GetMapping("/api/orders/{id}")
    public OrderResponse getOrderById(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequestDTO orderDTO,
            @RequestHeader("Authorization") String authToken
    );



}
