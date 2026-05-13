package com.example.OrderService.service;

import com.example.OrderService.dto.*;
import com.example.OrderService.exception.NotFoundException;
import com.example.OrderService.model.Order;
import com.example.OrderService.model.OrderItem;
import com.example.OrderService.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderService orderService;
    private final OrderEventProducer orderEventProducer;
    private final InvoiceService invoiceService;
    private final SalesEntryService salesEntryService;

    public PaymentEventConsumer(OrderService orderService, OrderEventProducer orderEventProducer, InvoiceService invoiceService, SalesEntryService salesEntryService) {
        this.orderService = orderService;
        this.orderEventProducer = orderEventProducer;
        this.invoiceService = invoiceService;
        this.salesEntryService = salesEntryService;
    }

    @JmsListener(destination = "payment.events")
    public void consume(String message) {

        try {
            System.out.println("Received JSON: " + message);

            PaymentEvent event =
                    objectMapper.readValue(message, PaymentEvent.class);

            handleEvent(event);

        } catch (Exception e) {
            System.err.println("Failed to process message");
            e.printStackTrace();
        }
    }

    private void handleEvent(PaymentEvent event) {
        try {
            switch (event.getEventType()) {

                case "PAYMENT_INITIALIZED":
                    orderService.setOrderStatus(event.getOrderId(), OrderStatus.PAYMENT_INITIATED);
                    break;

                case "PAYMENT_CONFIRMED":
                    orderService.setOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
                    orderEventProducer.sendConfirmOrder(event.getOrderId());
                    invoiceService.sendInvoice(event.getOrderId());
                    salesEntryService.create(event.getOrderId());
                    break;

                case "PAYMENT_FAILED":
                    orderService.setOrderStatus(event.getOrderId(), OrderStatus.FAILED);
                    Order order = orderService.getOrderById(event.getOrderId());
                    OrderCreateEvent orderCreateEvent = new OrderCreateEvent();
                    orderCreateEvent.setOrderId(order.getOrderId());
                    List<OrderItemDTO> orderItemDTOList = new ArrayList<>();
                    for (OrderItem orderItem : order.getOrderItems()) {
                        OrderItemDTO orderItemDTO = new OrderItemDTO(orderItem.getStockId(), orderItem.getQuantity());
                        orderItemDTOList.add(orderItemDTO);
                    }
                    orderCreateEvent.setItems(orderItemDTOList);
                    orderEventProducer.sendFailureOrder(orderCreateEvent);
                    break;

                default:
                    System.out.println("Unknown event type");
            }
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

}