package com.example.OrderService.service;

import com.example.OrderService.dto.OrderCreateEventResponse;
import com.example.OrderService.exception.NotFoundException;
import com.example.OrderService.model.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.weaver.ast.Or;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InventoryEventConsumer {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public InventoryEventConsumer(OrderService orderService){
        this.orderService=orderService;
        this.objectMapper=new ObjectMapper();
    }

    @JmsListener(destination = "inventory.status")
    public void consume(String message)  {
        System.out.println("Received JSON: " + message);

        try {
            OrderCreateEventResponse response = objectMapper.readValue(message, OrderCreateEventResponse.class);
            if (Objects.equals(response.getStatus(), "STOCK_AVAILABLE")) {
                orderService.setOrderStatus(response.getOrderId(), OrderStatus.PAYMENT_INITIATED);
            } else {
                orderService.setOrderStatus(response.getOrderId(), OrderStatus.CANCELLED);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
