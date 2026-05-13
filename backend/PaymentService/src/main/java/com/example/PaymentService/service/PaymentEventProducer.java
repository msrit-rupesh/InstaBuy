package com.example.PaymentService.service;

import com.example.PaymentService.dto.PaymentEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void sendPaymentInitialized(Long orderId)  {

        PaymentEvent event =
                new PaymentEvent("PAYMENT_INITIALIZED", orderId);
        try {
            String json = objectMapper.writeValueAsString(event);
            jmsTemplate.convertAndSend("payment.events", json);
        }catch (JsonProcessingException e){
            System.out.println(e.getMessage());
        }
    }

    public void sendPaymentConfirmed(Long orderId) {
        PaymentEvent event =
                new PaymentEvent("PAYMENT_CONFIRMED", orderId);

        try {
            String json = objectMapper.writeValueAsString(event);
            jmsTemplate.convertAndSend("payment.events", json);
        }catch (JsonProcessingException e){
            System.out.println(e.getMessage());
        }
    }

    public void sendPaymentFailed(Long orderId) {
        PaymentEvent event =
                new PaymentEvent("PAYMENT_FAILED", orderId);

        try {
            String json = objectMapper.writeValueAsString(event);
            jmsTemplate.convertAndSend("payment.events", json);
        }catch (JsonProcessingException e){
            System.out.println(e.getMessage());
        }
    }
}