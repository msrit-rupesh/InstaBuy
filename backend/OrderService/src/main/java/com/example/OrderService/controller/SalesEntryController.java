package com.example.OrderService.controller;

import com.example.OrderService.dto.AuthUser;
import com.example.OrderService.dto.SalesEntryRequestDTO;
import com.example.OrderService.dto.SalesReportDTO;
import com.example.OrderService.exception.NotFoundException;
import com.example.OrderService.model.SalesEntry;
import com.example.OrderService.service.SalesEntryService;
import jakarta.validation.Valid;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/sales")
@PreAuthorize("hasAnyRole('VENDOR','ADMIN)")
public class SalesEntryController {

    private final SalesEntryService salesEntryService;

    public SalesEntryController(SalesEntryService salesEntryService){
        this.salesEntryService=salesEntryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSalesEntryById(@PathVariable Long id, Authentication authentication){
        try{
            AuthUser authUser=(AuthUser)authentication.getPrincipal();
            SalesEntry salesEntry=salesEntryService.getSalesEntryById(id);
            if(!Objects.equals(salesEntry.getVendorId(), authUser.getId())){
                return ResponseEntity.badRequest().body("Sales Entry is not assigned to the vendor");
            }
            return ResponseEntity.ok(salesEntry);
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getSalesReport(
            @Param("vendorId") Long vendorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Authentication authentication
    ){

        try {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            SalesReportDTO salesReport=salesEntryService.getSalesReportBetweenDays(authUser.getId(),startDate.atStartOfDay(ZoneId.systemDefault())
                    .toInstant(),endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            return ResponseEntity.ok(salesReport);
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    @GetMapping("/total-report")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getTotalSalesReport(Authentication authentication){

        try {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            SalesReportDTO salesReport=salesEntryService.getTotalSales(authUser.getId());
            return ResponseEntity.ok(salesReport);
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    @GetMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getSales(
            @Param("vendorId") Long vendorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Authentication authentication
    ){

        try{
            AuthUser authUser=(AuthUser)authentication.getPrincipal();
            List<SalesEntry> salesEntries=salesEntryService.getSalesBetweenDates(authUser.getId(),startDate.atStartOfDay(ZoneId.systemDefault())
                    .toInstant(),endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());;
            return ResponseEntity.ok(salesEntries);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/vendor/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReportByVendor(@PathVariable Long id){

        try {
            SalesReportDTO salesReport=salesEntryService.getTotalSales(id);
            return ResponseEntity.ok(salesReport);
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/vendor/total/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReportByVendor(
            @PathVariable Long id,
            @Param("vendorId") Long vendorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    ){

        try {
            SalesReportDTO salesReport=salesEntryService.getSalesReportBetweenDays(id,startDate.atStartOfDay(ZoneId.systemDefault())
                    .toInstant(),endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());;
            return ResponseEntity.ok(salesReport);
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }






}
