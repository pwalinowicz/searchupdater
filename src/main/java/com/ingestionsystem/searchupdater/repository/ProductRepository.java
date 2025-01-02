package com.ingestionsystem.searchupdater.repository;

import com.ingestionsystem.searchupdater.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    @Transactional
    default void updateOrInsert(Product product) {
        save(product);
    }
}
