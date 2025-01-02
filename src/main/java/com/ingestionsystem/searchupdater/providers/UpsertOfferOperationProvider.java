package com.ingestionsystem.searchupdater.providers;

import com.ingestionsystem.searchupdater.model.Offer;
import com.ingestionsystem.searchupdater.model.Product;
import com.ingestionsystem.searchupdater.operation.BaseSearchEngineOperation;
import com.ingestionsystem.searchupdater.repository.OfferRepository;
import com.ingestionsystem.searchupdater.repository.ProductRepository;
import com.ingestionsystem.searchupdater.service.IngestionRequest;
import com.ingestionsystem.searchupdater.validation.FieldValidator;

import java.util.ArrayList;
import java.util.List;

public class UpsertOfferOperationProvider extends SearchEngineOperationProvider {

    public UpsertOfferOperationProvider(ProductRepository productRepository, OfferRepository offerRepository,
                                        IngestionRequest request) {
        super(productRepository, offerRepository, request);
    }

    @Override
    public List<BaseSearchEngineOperation> getSearchOperations(IngestionRequest request) {
        var operations = new ArrayList<BaseSearchEngineOperation>();
        var offerId = request.offerId();
        var existingOffer = offerRepository.findById(offerId);
        if (existingOffer.isPresent()) {
            if (existingOffer.get().getProduct().getId().equals(request.relatedProductId())) {
                return List.of();
            } else {
                var existingProduct = existingOffer.get().getProduct();
                operations.add(SearchEngineOperationProvider.getDeleteSearchEngineOperation(existingProduct));
            }
        }
//        if (existingOffer.isPresent()
//                && !existingOffer.get().getProduct().getId().equals(request.relatedProductId())) {
//            var existingProduct = existingOffer.get().getProduct();
//            operations.add(SearchEngineOperationProvider.getDeleteSearchEngineOperation(existingProduct));
//        }
        assignOfferToProductFromRequest(request, operations);
        return operations;
    }

    private void assignOfferToProductFromRequest(IngestionRequest request, List<BaseSearchEngineOperation> operations) {
        var offer = new Offer();
        offer.setId(request.offerId());
        offer.setName(request.offerName());
        var productOptional = productRepository.findById(request.relatedProductId());
        if (productOptional.isPresent()) {
            var existingProduct = productOptional.get();
            offer.setProduct(existingProduct);
            offerRepository.updateOrInsert(offer);
            if (productOptional.get().isValid()) {
                var offers = offerRepository.findByProductId(existingProduct.getId()).stream().map(Offer::getName).toList();
                operations.add(SearchEngineOperationProvider.getUpsertSearchEngineOperation(existingProduct, offers));
            }
        } else {
            var product = new Product();
            product.setId(request.relatedProductId());
            productRepository.updateOrInsert(product);
            offer.setProduct(product);
            offerRepository.updateOrInsert(offer);
        }
    }

    @Override
    protected void validateRequestForProvider(IngestionRequest request) {
        var operation = request.operation();
        FieldValidator.validateField("offerId", request.offerId(), operation);
        FieldValidator.validateField("offerName", request.offerName(), operation);
        FieldValidator.validateField("relatedProductId", request.relatedProductId(), operation);
    }
}