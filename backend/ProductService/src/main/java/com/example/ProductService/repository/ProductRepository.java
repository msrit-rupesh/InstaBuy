package com.example.ProductService.repository;

import java.util.List;
import java.util.Optional;

import com.example.ProductService.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product,String> {

    @Query(value = "{ $text: { $search: ?0 } }",
            fields = "{ score:{ $meta:'textScore' } }",
            sort   = "{ score: { $meta: 'textScore' } }"
    )
    List<Product> textSearch(String search);
    Optional<Product> findByIdAndDeletedFalse(String id);
    Page<Product> findAllByDeletedFalse(Pageable pageable);
}
