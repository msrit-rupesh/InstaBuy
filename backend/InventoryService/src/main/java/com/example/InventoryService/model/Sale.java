package com.example.InventoryService.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "sales",
        indexes={
                @Index(name = "idx_sales_order_id",columnList = "order_id"),
                @Index(name = "idx_sales_product_id",columnList = "product_id")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesId;

    @Column(name = "Order_id", nullable = false)
    @NotBlank(message = "Order Id must not be null")
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    @NotBlank(message = "Product Id must not be null")
    private Long productId;

    @Column(nullable = false)
    @Min(value = 1,message = "Quantity must be greater than 0")
    private Integer quantity;

    @Column(name = "unit_price",nullable = false)
    @Min(value = 1,message = "Unit Price must be greater than 0")
    private double unitPrice;

    @Column(name = "unit_discount",nullable = false)
    @Min(value = 0,message = "Discount must be greater than or equal to 0")
    private double unitDiscount;

    @Column(name = "final_price",nullable = false)
    @Min(value = 1,message = "Quantity must be greater than 0")
    private double finalPrice;

    @Enumerated(EnumType.STRING)
    private SalesStatus status;

    private Instant createdAt;
    private Instant updatedAt;
}