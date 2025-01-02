package com.ingestionsystem.searchupdater.repository;

import com.ingestionsystem.searchupdater.model.Offer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, String> {
    @Transactional
    default void updateOrInsert(Offer offer) {
        save(offer);
    }

    List<Offer> findByProductId(String productId);
}
