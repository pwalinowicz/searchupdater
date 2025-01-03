package com.ingestionsystem.searchupdater.providers;

import com.ingestionsystem.searchupdater.model.Offer;
import com.ingestionsystem.searchupdater.operation.BaseSearchEngineOperation;
import com.ingestionsystem.searchupdater.repository.OfferRepository;
import com.ingestionsystem.searchupdater.repository.ProductRepository;
import com.ingestionsystem.searchupdater.service.IngestionRequest;
import com.ingestionsystem.searchupdater.validation.FieldValidator;

import java.util.ArrayList;
import java.util.List;

public class DeleteOfferOperationProvider extends SearchEngineOperationProvider {

    public DeleteOfferOperationProvider(ProductRepository productRepository, OfferRepository offerRepository, IngestionRequest request) {
        super(productRepository, offerRepository, request);
    }

    public List<BaseSearchEngineOperation> getSearchOperations(IngestionRequest request) {
        var operations = new ArrayList<BaseSearchEngineOperation>();
        var offerId = request.offerId();
        var offerOptional = offerRepository.findById(offerId);

        offerOptional.ifPresent(offer -> {
            offerRepository.deleteById(offerId);
            if (offer.getProduct() == null) {
                return;
            }
            if (offer.getProduct().isValid()) {
                var product = offer.getProduct();
                var offers = offerRepository
                        .findByProductId(product.getId())
                        .stream()
                        .map(Offer::getName)
                        .toList();
                if (!offers.isEmpty()) {
                    operations.add(SearchEngineOperationProvider.getUpsertSearchEngineOperation(product, offers));
                } else {
                    operations.add(SearchEngineOperationProvider.getDeleteSearchEngineOperation(product));
                }
            }
        });
        return operations;
    }

    @Override
    protected void validateRequestForProvider(IngestionRequest request) {
        FieldValidator.validateField("offerId", request.offerId(), request.operation());
    }
}

