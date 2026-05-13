package com.example.InventoryService.model;

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
        name = "vendor_products",
        indexes = {
                @Index(name = "idx_vendor_products_product_id",columnList = "product_id"),
                @Index(name = "idx_vendor_products_vendor_id",columnList = "vendor_id")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    @NotBlank(message = "Product Id must not be null")
    private String productId;

    @Column(name = "vendor_id", nullable = false)
    @NotNull(message = "Vendor Id must not be null")
    private Long vendorId;

    @Column(nullable = false)
    @NotNull(message = "Price must not be null")
    @Min(value = 1,message = "Price must be greater than 0")
    private Double price;

    @Column(nullable = false)
    @NotNull(message = "Discount must not be null")
    @Min(value = 0,message = "Discount must be greater than equal to 0")
    @Max(value = 100,message = "Discount must be less than equal to 100")
    private Double discount;

    @Column(nullable = false)
    @NotNull(message = "Quantity must not be null")
    @Min(value = 1,message = "Quantity must be greater than 0")
    private Integer quantity;

    @Column(name = "reserve_quantity")
    @NotNull(message = "Reserve Quantity is required")
    @Min(value = 0,message = "Reserve Quantity can't be negative")
    private Integer ReserveQuantity=0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}