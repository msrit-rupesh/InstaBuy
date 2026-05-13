package com.example.OrderService.service;

import com.example.OrderService.dto.SalesReportDTO;
import com.example.OrderService.dto.VendorProductResponse;
import com.example.OrderService.exception.NotFoundException;
import com.example.OrderService.model.Order;
import com.example.OrderService.model.OrderItem;
import com.example.OrderService.model.SalesEntry;
import com.example.OrderService.repository.SalesEntryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SalesEntryService {

    private final SalesEntryRepository salesEntryRepository;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    public SalesEntryService(SalesEntryRepository salesEntryRepository,OrderService orderService,InventoryService inventoryService){
        this.salesEntryRepository=salesEntryRepository;
        this.orderService=orderService;
        this.inventoryService=inventoryService;
    }

    public void create(Long orderId) throws NotFoundException {

        Order order = orderService.getOrderById(orderId);
        for(OrderItem item: order.getOrderItems()) {
            SalesEntry salesEntry = new SalesEntry();
            salesEntry.setOrderId(order.getOrderId());
            VendorProductResponse vendorProduct =
                        inventoryService.getVendorProduct(item.getStockId());
            salesEntry.setVendorId(vendorProduct.getVendorId());
            salesEntry.setProductId(item.getProductId());
            salesEntry.setUserId(order.getUserId());
            salesEntry.setQuantity(item.getQuantity());
            salesEntry.setUnitPrice(item.getPrice());
            salesEntry.setDiscountAmount((item.getDiscount()* item.getPrice())/100);
            salesEntry.setFinalPrice(salesEntry.getUnitPrice()-salesEntry.getDiscountAmount());
            salesEntryRepository.save(salesEntry);
        }
    }

    public SalesEntry getSalesEntryById(Long id) throws NotFoundException {
        return salesEntryRepository.findById(id).orElseThrow(()->new NotFoundException("Sales Entry not found"));
    }

    public List<SalesEntry> getSalesByVendor(Long vendorId) {
        return salesEntryRepository.findByVendorId(vendorId);
    }


    public List<SalesEntry> getSalesBetweenDates(Long vendorId,Instant startDate, Instant endDate) {

        return salesEntryRepository.findByVendorIdAndCreatedAtBetween(vendorId,startDate, endDate);
    }


    public SalesReportDTO getSalesReportBetweenDays(Long vendorId,Instant startDate, Instant endDate) {

        return salesEntryRepository.getVendorSalesReportByDays(vendorId,startDate, endDate);
    }

    public SalesReportDTO getTotalSales(Long vendorId){
        return salesEntryRepository.getVendorSalesReport(vendorId);
    }



}
