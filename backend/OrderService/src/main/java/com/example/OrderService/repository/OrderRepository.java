package com.example.OrderService.repository;

import com.example.OrderService.model.Order;
import com.example.OrderService.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        DELETE FROM Order o
        WHERE o.status IN :statuses
        AND o.createdAt < :time
        """)
    int deleteExpiredOrders(List<OrderStatus> statuses, Instant time);
    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.orderItems
        WHERE o.status IN :statuses
        AND o.createdAt < :time
        """)
    List<Order> findExpiredOrdersWithItems(List<OrderStatus> statuses, Instant time);
    @Query("""
SELECT o FROM Order o
LEFT JOIN FETCH o.orderItems
WHERE o.orderId = :orderId
""")
    Optional<Order> findByIdWithItems(Long orderId);
}
