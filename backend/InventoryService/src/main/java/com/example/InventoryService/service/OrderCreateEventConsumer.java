package com.example.InventoryService.service;

import com.example.InventoryService.dto.OrderCreateEvent;
import com.example.InventoryService.dto.OrderCreateEventResponse;
import com.example.InventoryService.dto.OrderItemDTO;
import com.example.InventoryService.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Id;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;


@Component
public class OrderCreateEventConsumer {
    private final ObjectMapper objectMapper;
    private final VendorProductService vendorProductService;
    private final InventoryEventProducer inventoryEventProducer;

    public OrderCreateEventConsumer(VendorProductService vendorProductService, InventoryEventProducer inventoryEventProducer){
        this.vendorProductService=vendorProductService;
        this.inventoryEventProducer=inventoryEventProducer;
        this.objectMapper=new ObjectMapper();
    }

    @JmsListener(destination = "inventory.commands")
    public void consume(String message) {
        try {
            System.out.println("Received JSON: " + message);

            OrderCreateEvent event =
                    objectMapper.readValue(message, OrderCreateEvent.class);
            switch (event.getEvent()){
                case "RESERVE_STOCK":
                    this.reserveStock(event);
                    break;
                case "UPDATE_STOCK":
                    this.updateStockSuccess(event);
                    break;
                case "UPDATE_STOCK_FAILURE":
                    this.updateStockFailure(event);
                    break;

            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }


    public void reserveStock(OrderCreateEvent event) {

        OrderCreateEventResponse response = new OrderCreateEventResponse();
        response.setOrderId(event.getOrderId());

        try {
            for (OrderItemDTO orderItemDTO : event.getItems()) {
                if (!vendorProductService.checkStock(orderItemDTO.getStockId(), orderItemDTO.getQuantity())) {
                    throw new NotFoundException("Stock unavailable");
                }
            }
        } catch (NotFoundException e) {
            inventoryEventProducer.sendStatus(event.getOrderId(),"STOCK_UNAVAILABLE");
            return;
        }

        try {
            for (OrderItemDTO item : event.getItems()) {
                vendorProductService.updateProductByReserveQuantity(
                        item.getStockId(),
                        item.getQuantity()
                );
            }
            inventoryEventProducer.sendStatus(event.getOrderId(),"STOCK_AVAILABLE");

        } catch (NotFoundException e) {
            inventoryEventProducer.sendStatus(event.getOrderId(),"STOCK_UNAVAILABLE");
        }
    }

    private void updateStockSuccess(OrderCreateEvent event){
        try {
            for (OrderItemDTO item : event.getItems()) {
                vendorProductService.updateProductByQuantity(
                        item.getStockId(),
                        item.getQuantity()
                );
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
    private void updateStockFailure(OrderCreateEvent event){
        try {
            for (OrderItemDTO item : event.getItems()) {
                vendorProductService.releaseProductByReserveQuantity(
                        item.getStockId(),
                        item.getQuantity()
                );
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }


}



