package com.example.InventoryService.controller;

import com.example.InventoryService.dto.AuthUser;
import com.example.InventoryService.dto.UpdateQuantityRequestDTO;
import com.example.InventoryService.dto.VendorProductDTO;
import com.example.InventoryService.dto.InventoryResponseDTO;
import com.example.InventoryService.exception.NotFoundException;
import com.example.InventoryService.model.VendorProduct;
import com.example.InventoryService.security.JwtUtil;
import com.example.InventoryService.service.VendorProductService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final VendorProductService vendorProductService;
    private final JwtUtil jwt;

    public InventoryController(VendorProductService vendorProductService,JwtUtil jwt){

        this.vendorProductService = vendorProductService;
        this.jwt=jwt;
    }

    @GetMapping("/")
    public String greet(){
        return "Hello World";
    }

    @PostMapping()
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> createVendorProduct(@Valid @RequestBody VendorProductDTO dto, Authentication authentication){

        try {

            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            Long vendorId=authUser.getId();

            String token=jwt.generateToken("INVENTORY-SERVICE","INVENTORY-SERVICE",0);
            String authHeader="Bearer "+token;
            VendorProduct product=vendorProductService.addVendorProduct(vendorId,dto,authHeader);
            return ResponseEntity.ok().body(product);
        }
        catch(NotFoundException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVendorProduct(@PathVariable Long id){

        try {

            VendorProduct product=vendorProductService.getVendorProductById(id);
            return ResponseEntity.ok().body(product);
        }
        catch(NotFoundException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getProductsByVendorId(@PathVariable Long vendorId){

        try{
            List<VendorProduct> products=vendorProductService.getVendorInventory(vendorId);
            return ResponseEntity.ok().body(products);
        }
        catch (NotFoundException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('Vendor')")
    public ResponseEntity<?> getProductsByVendorId( Authentication authentication){
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        Long vendorId=authUser.getId();
        try{
            List<VendorProduct> products=vendorProductService.getVendorInventory(vendorId);
            return ResponseEntity.ok().body(products);
        }
        catch (NotFoundException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductsByProductId(@PathVariable String productId){
        try {
            List<InventoryResponseDTO> products = vendorProductService.getProductInventory(productId);
            return ResponseEntity.ok().body(products);
        }
        catch (NotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PatchMapping("/product/{id}")
    @PreAuthorize("hasRole('ORDER-SERVICE')")
    public ResponseEntity<?> updateQuantityById(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuantityRequestDTO request,
            Authentication authentication
    ){
        try {
            VendorProduct product=vendorProductService.updateProductByQuantity(id,request.getQuantity());
            return ResponseEntity.ok().body(product);
        }
        catch (NotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }



    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateInventory(@PathVariable Long id,@Valid @RequestBody VendorProductDTO dto,Authentication authentication){
        try {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            Long vendorId=authUser.getId();
            VendorProduct product = vendorProductService.updateInventory(vendorId,id, dto);
            return ResponseEntity.ok().body(product);
        }catch (NotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> deleteInventory(@PathVariable Long id){
        try {
            vendorProductService.deleteInventory(id);
            return ResponseEntity.ok().body("Stock deleted successfully");
        }catch (NotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}