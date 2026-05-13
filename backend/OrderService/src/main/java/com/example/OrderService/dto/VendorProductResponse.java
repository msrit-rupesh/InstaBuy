package com.example.OrderService.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorProductResponse {

    private Long id;
    private String productId;
    private Long vendorId;
    private Double price;
    private Double discount;
    private Integer quantity;
    private Integer ReserveQuantity;
    private Instant createdAt;
    private Instant updatedAt;

}
