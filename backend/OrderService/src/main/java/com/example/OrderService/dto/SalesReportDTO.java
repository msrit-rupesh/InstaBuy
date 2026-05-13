package com.example.OrderService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDTO {

    private long totalOrders;
    private long totalQuantity;
    private double totalDiscount;
    private double totalRevenue;

}