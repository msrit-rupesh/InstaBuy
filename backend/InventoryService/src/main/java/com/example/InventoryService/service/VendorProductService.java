package com.example.InventoryService.service;

import com.example.InventoryService.dto.*;
import com.example.InventoryService.exception.NotFoundException;
import com.example.InventoryService.model.VendorProduct;
import com.example.InventoryService.repository.InventoryRepository;
import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class VendorProductService {

    private final InventoryRepository inventoryRepository;
    private final ProductService productService;

    public VendorProductService(InventoryRepository inventoryRepository,ProductService productService){
        this.inventoryRepository = inventoryRepository;
        this.productService=productService;
    }

    public VendorProduct addVendorProduct(Long id,@Valid VendorProductDTO dto,String authHeader) throws NotFoundException{

        try{
            productService.getProductById(dto.getProductId(),authHeader);
        }catch(FeignException.NotFound ex){
            throw new NotFoundException(ex.getMessage());
        }

        VendorProduct item = new VendorProduct();
        item.setProductId(dto.getProductId());
        item.setVendorId(id);
        item.setPrice(dto.getPrice());
        item.setDiscount(dto.getDiscount());
        item.setQuantity(dto.getQuantity());
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());

        return inventoryRepository.save(item);
    }

    public boolean checkStock(Long stockId,int quantity) throws NotFoundException{
        VendorProduct product=inventoryRepository.findById(stockId).orElse(null);
        if(product==null){
            throw new NotFoundException("Product not found");
        }
        return (product.getQuantity()-product.getReserveQuantity())>=quantity;

    }
    public List<VendorProduct> getVendorInventory(Long vendorId) throws NotFoundException{
        List<VendorProduct> vendorProducts= inventoryRepository.findByVendorId(vendorId);
        if(vendorProducts.isEmpty()){
            throw new NotFoundException("Products with vendor Id not found");
        }
        return vendorProducts;
    }

    public List<InventoryResponseDTO> getProductInventory(String productId) throws NotFoundException{

        List<VendorProduct> items =
                inventoryRepository.findByProductId(productId);

        if(items.isEmpty()){
            throw new NotFoundException("Products Out of Stock");
        }

        return items.stream().map(i -> {
            InventoryResponseDTO dto = new InventoryResponseDTO();
            dto.setId(i.getId());
            dto.setProductId(i.getProductId());
            dto.setPrice(i.getPrice());
            dto.setDiscount(i.getDiscount());
            dto.setAvailability((i.getQuantity()-i.getReserveQuantity())>0);
            return dto;
        }).toList();
    }

    public VendorProduct updateInventory(Long vendorId,Long id,VendorProductDTO dto) throws NotFoundException{

        VendorProduct item = inventoryRepository.findById(id).orElseThrow(null);
        if(item==null){
            throw new NotFoundException("Product with Id not found");
        }
        if(!item.getVendorId().equals(vendorId)){
            throw new NotFoundException("Vendor not mapped with Product");
        }
        item.setPrice(dto.getPrice());
        item.setDiscount(dto.getDiscount());
        item.setQuantity(dto.getQuantity());
        item.setUpdatedAt(Instant.now());

        return inventoryRepository.save(item);
    }

    public void deleteInventory(Long id) throws NotFoundException{
        VendorProduct item = inventoryRepository.findById(id).orElseThrow(null);
        if(item==null){
            throw new NotFoundException("Product with Id not found");
        }
        inventoryRepository.deleteById(id);
    }


    public VendorProduct updateProductByQuantity(Long id,int quantity) throws NotFoundException{
        VendorProduct product=inventoryRepository.findById(id).orElse(null);
        if(product==null){
            throw new NotFoundException("Product with Id not found");
        }
        if(product.getReserveQuantity()<quantity){
            throw new NotFoundException("Product has Insufficient stock");
        }
        product.setQuantity(product.getQuantity()-quantity);
        product.setReserveQuantity(product.getReserveQuantity()-quantity);
        product.setUpdatedAt(Instant.now());
        inventoryRepository.save(product);
        return product;
    }
    public VendorProduct updateProductByReserveQuantity(Long id,int quantity) throws NotFoundException{
        VendorProduct product=inventoryRepository.findById(id).orElse(null);
        if(product==null){
            throw new NotFoundException("Product with Id not found");
        }
        if(product.getQuantity()-product.getReserveQuantity()<quantity){
            throw new NotFoundException("Product has Insufficient stock");
        }
        product.setReserveQuantity(product.getReserveQuantity()+quantity);
        product.setUpdatedAt(Instant.now());
        inventoryRepository.save(product);
        return product;
    }
    public VendorProduct releaseProductByReserveQuantity(Long id,int quantity) throws NotFoundException{
        VendorProduct product=inventoryRepository.findById(id).orElse(null);
        if(product==null){
            throw new NotFoundException("Product with Id not found");
        }
        product.setReserveQuantity(product.getReserveQuantity()-quantity);
        product.setUpdatedAt(Instant.now());
        inventoryRepository.save(product);
        return product;
    }

    public VendorProduct getVendorProductById(Long id) throws NotFoundException {

        VendorProduct product=inventoryRepository.findById(id).orElse(null);
        return product;
    }
}