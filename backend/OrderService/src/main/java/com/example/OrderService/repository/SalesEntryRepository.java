package com.example.OrderService.repository;

import com.example.OrderService.dto.SalesReportDTO;
import com.example.OrderService.model.SalesEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SalesEntryRepository extends JpaRepository<SalesEntry,Long> {
    List<SalesEntry> findByVendorId(Long vendorId);


    List<SalesEntry> findByVendorIdAndCreatedAtBetween(
            Long vendorId,
            Instant start,
            Instant end
    );


    @Query("""
        SELECT new com.example.OrderService.dto.SalesReportDTO(
            COUNT(DISTINCT s.orderId),
            COALESCE(SUM(s.quantity), 0),
            COALESCE(SUM(s.discountAmount), 0),
            COALESCE(SUM(s.finalPrice*s.quantity), 0)
        )
        FROM SalesEntry s
        WHERE s.vendorId = :vendorId
        AND s.createdAt >= :startDate AND s.createdAt <= :endDate
    """)
    SalesReportDTO getVendorSalesReportByDays(
            @Param("vendorId") Long vendorId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
        SELECT new com.example.OrderService.dto.SalesReportDTO(
             COUNT(DISTINCT s.orderId),
             COALESCE(SUM(s.quantity), 0),
             COALESCE(SUM(s.discountAmount), 0),
             COALESCE(SUM(s.finalPrice*s.quantity), 0)
         )
        FROM SalesEntry s
        WHERE s.vendorId = :vendorId
    """)
    SalesReportDTO getVendorSalesReport(@Param("vendorId") Long vendorId);

}

