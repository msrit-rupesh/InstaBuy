package com.example.InventoryService.repository;

import com.example.InventoryService.model.VendorProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository
        extends JpaRepository<VendorProduct,Long>{

    List<VendorProduct> findByProductId(String productId);

    List<VendorProduct> findByVendorId(Long vendorId);

}