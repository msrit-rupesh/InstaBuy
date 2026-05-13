package com.example.OrderService.service;


import com.example.OrderService.dto.OrderCreateEvent;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.model.Order;
import com.example.OrderService.model.OrderItem;
import com.example.OrderService.model.OrderStatus;
import com.example.OrderService.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCleanupService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    // 🔥 10 minutes TTL
    private static final Duration ORDER_TTL = Duration.ofMinutes(10);
    private Instant lastUpdate;

    // Run every 5 minutes (recommended instead of 1 hour)
    @Scheduled(fixedRate = 300_000) // 5 min
    @Transactional
    public void scheduledCleanup() {
        cleanupExpiredOrders();
    }

    @Transactional
    public void cleanupExpiredOrders() {
        Instant threshold = Instant.now().minus(ORDER_TTL);

        if(lastUpdate!=null){
            if(lastUpdate.isAfter(threshold)){
                return;
            }
        }

        List<Order> orders = orderRepository.findExpiredOrdersWithItems(
                List.of(OrderStatus.PAYMENT_INITIATED),
                threshold
        );

        for (Order order : orders) {

            OrderCreateEvent event = new OrderCreateEvent();
            event.setOrderId(order.getOrderId());

            List<OrderItemDTO> items = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                items.add(new OrderItemDTO(
                        item.getStockId(),
                        item.getQuantity()
                ));
            }

            event.setItems(items);

            // 🔥 send AFTER delete (see below)
            orderEventProducer.sendFailureOrder(event);
        }
        lastUpdate=Instant.now();
        // ✅ Use JPA delete (NOT bulk)
        orderRepository.deleteAll(orders);

        log.info("Deleted {} expired orders", orders.size());
    }
}