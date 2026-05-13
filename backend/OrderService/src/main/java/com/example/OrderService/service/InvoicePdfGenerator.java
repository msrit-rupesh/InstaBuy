package com.example.OrderService.service;

import com.example.OrderService.dto.AddressResponseDTO;
import com.example.OrderService.dto.ProductResponseDTO;
import com.example.OrderService.model.OrderItem;
import com.example.OrderService.dto.VendorProfileResponseDTO;
import com.example.OrderService.security.JwtUtil;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
@Service
public class InvoicePdfGenerator {


    private static final String BASE_DIR = "invoices/";
    private final ProductService productService;
    private final UserService userService;
    private final JwtUtil jwt;

    public InvoicePdfGenerator(ProductService productService,JwtUtil jwt,UserService userService){
        this.productService=productService;
        this.userService=userService;
        this.jwt=jwt;
    }

    public String generateInvoicePdf(
            Long userId,
            VendorProfileResponseDTO vendor,
            List<OrderItem> items) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AddressResponseDTO userAddress =
                userService.getBillingAddressById(userId);

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.setMargins(20, 20, 20, 20);

        /* ================= HEADER ================= */
        Table headerTable = new Table(new float[]{3, 2});
        headerTable.setWidth(UnitValue.createPercentValue(100));

        Cell companyCell = new Cell()
                .add(new Paragraph(vendor.getCompanyName()).setBold().setFontSize(16))
                .add(new Paragraph(vendor.getStreetAddress()))
                .add(new Paragraph(vendor.getCity() + ", " + vendor.getState()))
                .add(new Paragraph(vendor.getCountry() + " - " + vendor.getPostalCode()))
                .add(new Paragraph("Phone: " + vendor.getPhone()))
                .setBorder(Border.NO_BORDER);

        Cell invoiceCell = new Cell()
                .add(new Paragraph("INVOICE").setBold().setFontSize(18))
                .add(new Paragraph("Invoice No: INV-" + System.currentTimeMillis()))
                .add(new Paragraph("Date: " + LocalDate.now()))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER);

        headerTable.addCell(companyCell);
        headerTable.addCell(invoiceCell);
        document.add(headerTable);

        document.add(new Paragraph("\n"));

        /* ================= BILL TO ================= */
        document.add(new Paragraph("BILL TO").setBold());

        document.add(new Paragraph(
                userAddress.getFullName() + "\n" +
                        userAddress.getStreetAddress() + "\n" +
                        userAddress.getCity() + ", " + userAddress.getState() + "\n" +
                        userAddress.getCountry() + " - " + userAddress.getPostalCode() + "\n" +
                        "Phone: " + userAddress.getPhone()
        ));

        document.add(new Paragraph("\n"));

        /* ================= ITEM TABLE ================= */
        Table table = new Table(new float[]{4, 2, 2, 2, 2, 2});
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell("Description");
        table.addHeaderCell("Unit Price");
        table.addHeaderCell("Qty");
        table.addHeaderCell("Discount %");
        table.addHeaderCell("Discount Amount");
        table.addHeaderCell("Final Amount");

        String authToken = "Bearer " + jwt.generateToken(
                "ORDER-SERVICE", "ORDER-SERVICE", 0);

        double subtotal = 0.0;
        double totalDiscount = 0.0;
        double totalAmount = 0.0;

        for (OrderItem item : items) {

            ProductResponseDTO product =
                    productService.getProductById(item.getProductId(), authToken);

            double itemSubtotal = item.getPrice() * item.getQuantity();
            double itemDiscountAmount = itemSubtotal - item.getFinalPrice();

            subtotal += itemSubtotal;
            totalDiscount += itemDiscountAmount;
            totalAmount += item.getFinalPrice();

            table.addCell(product.getName());
            table.addCell("₹" + item.getPrice());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(item.getDiscount() + "%");
            table.addCell("₹" + String.format("%.2f", itemDiscountAmount));
            table.addCell("₹" + String.format("%.2f", item.getFinalPrice()));
        }

        document.add(table);

        /* ================= TOTALS ================= */
        document.add(new Paragraph("\n"));

        Table totalTable = new Table(new float[]{6, 2});
        totalTable.setWidth(UnitValue.createPercentValue(40));
        totalTable.setHorizontalAlignment(HorizontalAlignment.RIGHT);

        totalTable.addCell(new Cell()
                .add(new Paragraph("Subtotal"))
                .setBorder(Border.NO_BORDER));
        totalTable.addCell(new Cell()
                .add(new Paragraph("₹" + String.format("%.2f", subtotal)))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER));

        totalTable.addCell(new Cell()
                .add(new Paragraph("Total Discount"))
                .setBorder(Border.NO_BORDER));
        totalTable.addCell(new Cell()
                .add(new Paragraph("- ₹" + String.format("%.2f", totalDiscount)))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER));

        totalTable.addCell(new Cell()
                .add(new Paragraph("Tax"))
                .setBorder(Border.NO_BORDER));
        totalTable.addCell(new Cell()
                .add(new Paragraph("₹0.00"))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER));

        totalTable.addCell(new Cell()
                .add(new Paragraph("Total").setBold())
                .setBorder(Border.NO_BORDER));
        totalTable.addCell(new Cell()
                .add(new Paragraph("₹" + String.format("%.2f", totalAmount)).setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER));

        document.add(totalTable);

        /* ================= FOOTER ================= */
        document.add(new Paragraph("\nThank you for your business!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.close();

        /* ================= FILE SAVE ================= */
        String userDIR = BASE_DIR + userId;
        Files.createDirectories(Paths.get(userDIR));

        String filePath =
                userDIR + "/invoice_" + System.currentTimeMillis() + ".pdf";

        Files.write(Paths.get(filePath), out.toByteArray());

        return filePath;
    }
}