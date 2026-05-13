package com.example.OrderService.service;


import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.dto.VendorProductResponse;
import com.example.OrderService.exception.NotFoundException;
import com.example.OrderService.model.Order;
import com.example.OrderService.model.OrderItem;
import com.example.OrderService.model.OrderStatus;
import com.example.OrderService.repository.OrderRepository;
import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.Instant;


@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final OrderEventProducer orderEventProducer;



    @Transactional
    public Order generateOrder(Long userId, @Valid List<OrderItemDTO> orderItems, String authToken) throws NotFoundException {

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);

        double totalAmount = 0;
        for (OrderItemDTO item : orderItems) {
            VendorProductResponse vendorProductResponse;
            try {
                vendorProductResponse=inventoryService.getVendorProduct(item.getStockId());
            } catch (FeignException.NotFound ex) {
                throw new NotFoundException("Stock not found with id: " + item.getStockId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setStockId(item.getStockId());
            orderItem.setProductId(vendorProductResponse.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(vendorProductResponse.getPrice());
            orderItem.setDiscount(vendorProductResponse.getDiscount());

            double discountedUnitPrice = vendorProductResponse.getPrice() - (vendorProductResponse.getPrice() * vendorProductResponse.getDiscount() / 100);

            double finalPrice = discountedUnitPrice * item.getQuantity();

            orderItem.setFinalPrice(finalPrice);

            order.addOrderItem(orderItem);

            totalAmount += finalPrice;
        }

        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setUpdatedAt(Instant.now());

        Order savedOrder= orderRepository.save(order);

        orderEventProducer.sendOrderCreate(savedOrder);
        return savedOrder;
    }

    @Transactional
    public Order getOrderById(Long id) throws NotFoundException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Order not found with id: " + id
                ));
    }


    public void deleteOrder(Long id) throws NotFoundException {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }

    public List<Order> getOrderByUser(Long userId) throws NotFoundException {
        List<Order> orders=orderRepository.findByUserIdAndStatus(userId, OrderStatus.CONFIRMED);
        if(orders.isEmpty()){
            throw new NotFoundException("Orders not found with user Id");
        }
        return orders;
    }

    public void setOrderStatus(@NotNull(message = "Order id is required") Long orderId, OrderStatus orderStatus) throws NotFoundException {
        Order order=orderRepository.findById(orderId).orElse(null);
        if(order==null){
            throw new NotFoundException("Order not found");
        }
        order.setStatus(orderStatus);
        orderRepository.save(order);
    }
}
