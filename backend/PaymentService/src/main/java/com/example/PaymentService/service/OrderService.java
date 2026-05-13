package com.example.PaymentService.service;

import com.example.PaymentService.client.OrderServiceClient;
import com.example.PaymentService.dto.OrderRequestDTO;
import com.example.PaymentService.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Service
public class OrderService {

    private final OrderServiceClient orderServiceClient;

    public OrderService(OrderServiceClient orderServiceClient){
        this.orderServiceClient=orderServiceClient;
    }

    public OrderResponse getOrderById(Long id, Long userId, String authToken){
        return orderServiceClient.getOrderById(id,new OrderRequestDTO(userId),authToken);
    }
}
