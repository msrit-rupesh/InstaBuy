package com.example.InventoryService.service;

import com.example.InventoryService.dto.OrderCreateEventResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public InventoryEventProducer(JmsTemplate jmsTemplate){
        this.jmsTemplate=jmsTemplate;
        this.objectMapper=new ObjectMapper();
    }

    public void sendStatus(Long orderId,String status){

        OrderCreateEventResponse response=new OrderCreateEventResponse(orderId,status);
        try {
            String json = objectMapper.writeValueAsString(response);
            jmsTemplate.convertAndSend("inventory.status", json);
        }catch (JsonProcessingException e){
            System.out.println(e.getMessage());
        }
    }
}
