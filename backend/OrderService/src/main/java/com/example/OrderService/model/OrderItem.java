package com.example.OrderService.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id", columnList = "order_id"),
                @Index(name = "idx_order_items_order_item_id", columnList = "order_item_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    // 🔥 FIX IS HERE
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference   // ✅ THIS LINE FIXES YOUR ERROR
    private Order order;

    @Column(name = "stock_id", nullable = false)
    @NotNull(message = "Stock id is required")
    private Long stockId;

    @Column(name = "product_id", nullable = false)
    @NotBlank(message = "Product id is required")
    private String productId;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity can't be negative")
    private int quantity;

    @Column(nullable = false)
    @Min(value = 1, message = "Price can't be negative")
    private double price;

    @Column(nullable = false)
    @Min(value = 0, message = "Discount can't be negative")
    @Max(value = 100, message = "Discount can't be greater than 100")
    private double discount;

    @Column(name = "final_price", nullable = false)
    @Min(value = 0, message = "Final price can't be negative")
    private double finalPrice;
}