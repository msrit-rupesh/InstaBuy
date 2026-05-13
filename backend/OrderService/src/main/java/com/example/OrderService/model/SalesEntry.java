package com.example.OrderService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Entity
@Table(
        name = "sales_entities",
        indexes = {
                @Index(name = "idx_sales_entry_order_id", columnList = "order_id"),
                @Index(name = "idx_sales_entry_vendor_id", columnList = "vendor_id"),
                @Index(name = "idx_sales_entry_product_id", columnList = "product_id"),
                @Index(name = "idx_sales_entry_user_id", columnList = "user_id"),

        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    @NotNull(message = "Order id is required")
    private Long orderId;

    @Column(name = "vendor_id", nullable = false)
    @NotNull(message = "Vendor id is required")
    private Long vendorId;

    @Column(name = "product_id", nullable = false)
    @NotBlank(message = "Product id is required")
    private String productId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "Product id is required")
    private Long userId;


    @Column(nullable = false)
    @Min(value = 1, message = "Quantity can't be negative")
    private int quantity;

    @Column(nullable = false)
    @Min(value = 1, message = "Price can't be negative")
    private double unitPrice;

    @Column(nullable = false)
    @NotNull(message = "Discount amount is required")
    private double discountAmount;

    @Column(name = "final_price", nullable = false)
    @Min(value = 0, message = "Final price can't be negative")
    private double finalPrice;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}