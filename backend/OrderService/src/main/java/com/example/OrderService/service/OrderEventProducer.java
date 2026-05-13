package com.example.OrderService.service;

import com.example.OrderService.dto.OrderCreateEvent;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.model.Order;
import com.example.OrderService.model.OrderItem;
import com.example.OrderService.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderEventProducer {

    private final JmsTemplate jmsTemplate;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(JmsTemplate jmsTemplate,OrderRepository orderRepository){
        this.jmsTemplate=jmsTemplate;
        this.orderRepository=orderRepository;
        this.objectMapper=new ObjectMapper();
    }

    public void sendOrderCreate(Order order){
        OrderCreateEvent orderCreateEvent=new OrderCreateEvent();
        orderCreateEvent.setOrderId(order.getOrderId());
        List<OrderItemDTO> orderItemDTOList=new ArrayList<>();
        for(OrderItem orderItem:order.getOrderItems()){
            OrderItemDTO orderItemDTO=new OrderItemDTO(orderItem.getStockId(),orderItem.getQuantity());
            orderItemDTOList.add(orderItemDTO);
        }
        orderCreateEvent.setItems(orderItemDTOList);
        orderCreateEvent.setEvent("RESERVE_STOCK");
        try {
            String json = objectMapper.writeValueAsString(orderCreateEvent);
            jmsTemplate.convertAndSend("inventory.commands", json);
        }catch (JsonProcessingException e){
            System.out.println(e.getMessage());
        }
    }

    public void sendConfirmOrder(Long orderId){

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            OrderCreateEvent orderCreateEvent = new OrderCreateEvent();
            orderCreateEvent.setOrderId(order.getOrderId());
            List<OrderItemDTO> orderItemDTOList = new ArrayList<>();
            for (OrderItem orderItem : order.getOrderItems()) {
                OrderItemDTO orderItemDTO = new OrderItemDTO(orderItem.getStockId(), orderItem.getQuantity());
                orderItemDTOList.add(orderItemDTO);
            }
            orderCreateEvent.setItems(orderItemDTOList);
            orderCreateEvent.setEvent("UPDATE_STOCK");
            try {
                String json = objectMapper.writeValueAsString(orderCreateEvent);
                jmsTemplate.convertAndSend("inventory.commands", json);
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void sendFailureOrder(OrderCreateEvent orderCreateEvent){

        try {

            orderCreateEvent.setEvent("UPDATE_STOCK_FAILURE");
            try {
                String json = objectMapper.writeValueAsString(orderCreateEvent);
                jmsTemplate.convertAndSend("inventory.commands", json);
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
