package com.example.ProductService.service;

import com.example.ProductService.dto.ProductDto;
import com.example.ProductService.dto.ProductPatchDto;
import com.example.ProductService.exception.ConflictException;
import com.example.ProductService.exception.NotFoundException;
import com.example.ProductService.model.Product;
import com.example.ProductService.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository=productRepository;
    }

    public List<Product> getAll(){
        return productRepository.findAll();
    }

    public Product addProduct(@Valid ProductDto dto) {
        Product product=new Product();
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setDescription(dto.getDescription());
        return productRepository.save(product);
    }

    public List<Product> searchByRelevance(String terms){return productRepository.textSearch(terms);
    }


    public void addAllProducts(List<Product> products) {
        productRepository.saveAll(products);
    }


    public Product patchProduct(String id, @Valid ProductPatchDto dto) throws NotFoundException{
        Product product = productRepository.findById(id).orElse(null);
        if(product==null){
            throw new NotFoundException("Product not found");
        }

        dto.getName().ifPresent(product::setName);
        dto.getBrand().ifPresent(product::setBrand);
        dto.getCategory().ifPresent(product::setCategory);
        dto.getImageUrl().ifPresent(product::setImageUrl);
        dto.getDescription().ifPresent(product::setDescription);

        return productRepository.save(product);
    }

    public void deleteProduct(String id) throws NotFoundException {
        Product product = productRepository.findById(id).orElse(null);
        if(product==null){
            throw new NotFoundException("Product not found");
        }
        productRepository.deleteById(product.getId());
    }


    public Product getProductById(String id) throws NotFoundException {
        Product product=productRepository.findById(id).orElse(null);
        if(product==null){
            throw new NotFoundException("Product with Product Id not found");
        }
        return product;
    }
}
