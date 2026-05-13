package com.example.ProductService.controller;

import com.example.ProductService.dto.ProductDto;
import com.example.ProductService.dto.ProductPatchDto;
import com.example.ProductService.dto.ProductSearchDto;
import com.example.ProductService.exception.NotFoundException;
import com.example.ProductService.model.Product;
import com.example.ProductService.service.ProductService;
import jakarta.validation.Valid;
import jakarta.ws.rs.QueryParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Component
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService=productService;
    }


    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(){
        return ResponseEntity.ok().body(productService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('VENDOR','Admin')")
    public ResponseEntity<Product> addProducts(@Valid @RequestBody ProductDto dto){
        return ResponseEntity.ok().body(productService.addProduct(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") String id){
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok().body(product);
        }
        catch (NotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProduct(@RequestParam String query){
        return ResponseEntity.ok().body(productService.searchByRelevance(query));
    }


    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('VENDOR','Admin')")
    public ResponseEntity<String> saveBulk(@RequestBody List<Product> products) {
        productService.addAllProducts(products);
        return ResponseEntity.ok("Saved " + products.size() + " products");
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable("id") String id, @Valid @RequestBody ProductPatchDto dto){
        try {
            return ResponseEntity.ok().body(productService.patchProduct(id, dto));
        }
        catch (NotFoundException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") String id){
        try{
            productService.deleteProduct(id);
            return ResponseEntity.ok().body("Product Deleted Successfully");
        }
        catch (NotFoundException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

}
