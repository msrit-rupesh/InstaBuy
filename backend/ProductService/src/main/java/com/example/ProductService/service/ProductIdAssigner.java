package com.example.ProductService.service;

import com.example.ProductService.model.Product;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class ProductIdAssigner implements BeforeConvertCallback<Product> {

    @Override
    public Product onBeforeConvert(Product product, String collection) {
        product.setId(IdGenerator.nextId());
        return product;
    }
}