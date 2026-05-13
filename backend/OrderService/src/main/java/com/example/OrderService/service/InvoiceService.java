package com.example.OrderService.service;

import com.example.OrderService.dto.VendorProductResponse;
import com.example.OrderService.dto.VendorProfileResponseDTO;
import com.example.OrderService.model.Order;
import com.example.OrderService.model.OrderItem;
import com.example.OrderService.security.JwtUtil;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class InvoiceService {

    private final UserService vendorProfileService;
    private final InvoicePdfGenerator invoicePdfGenerator;
    private final MailService mailService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final JwtUtil jwt;
    private final UserService userService;

    public InvoiceService(UserService vendorProfileService, InvoicePdfGenerator invoicePdfGenerator, MailService mailService,OrderService orderService,InventoryService inventoryService,JwtUtil jwt,UserService userService){
        this.vendorProfileService=vendorProfileService;
        this.invoicePdfGenerator=invoicePdfGenerator;
        this.mailService=mailService;
        this.orderService=orderService;
        this.inventoryService=inventoryService;
        this.jwt=jwt;
        this.userService=userService;
    }

    public void sendInvoice(Long orderId) {

        try {
            Order order = orderService.getOrderById(orderId);
            Long userId = order.getUserId();
            String userEmail = userService.getUserEmail(userId);

            String authToken = "Bearer " + jwt.generateToken(
                    "ORDER-SERVICE",
                    "ORDER-SERVICE",
                    0
            );

            // ✅ Group order items by Vendor ID
            Map<Long, List<OrderItem>> vendorItemsMap = new HashMap<>();

            for (OrderItem item : order.getOrderItems()) {

                VendorProductResponse vendorProduct =
                        inventoryService.getVendorProduct(item.getStockId());

                Long vendorId = vendorProduct.getVendorId();

                vendorItemsMap
                        .computeIfAbsent(vendorId, k -> new ArrayList<>())
                        .add(item);
            }

            // ✅ Generate & send ONE invoice per vendor
            for (Map.Entry<Long, List<OrderItem>> entry : vendorItemsMap.entrySet()) {

                Long vendorId = entry.getKey();
                List<OrderItem> vendorItems = entry.getValue();

                try {
                    VendorProfileResponseDTO vendorDetails =
                            vendorProfileService.getVendorProfile(vendorId);

                    String filePath =
                            invoicePdfGenerator.generateInvoicePdf(
                                    userId,
                                    vendorDetails,
                                    vendorItems   // ✅ LIST of items
                            );

                    mailService.sendInvoicePath(userEmail, filePath);

                } catch (Exception e) {
                    mailService.sendErrorMessage(
                            userEmail,
                            "Failed to generate invoice for vendorId "
                                    + vendorId + ": " + e.getMessage()
                    );
                }
            }

        } catch (Exception e) {
            mailService.sendErrorMessage("admin@gmail.com", e.getMessage());
        }
    }
}
