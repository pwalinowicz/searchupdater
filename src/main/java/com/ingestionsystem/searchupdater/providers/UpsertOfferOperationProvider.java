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
        var offerFromRequest = new Offer(offerId, request.offerName());
        var existingOffer = offerRepository.findById(offerId);

        if (existingOffer.isPresent()) {
            if (existingOffer.get().equals(offerFromRequest)
                    && existingOffer.get().getProduct().getId().equals(request.relatedProductId())) {
                // no changes in the offer
                return List.of();
            } else {
                operations.addAll(getDeleteProductOperation(existingOffer.get().getProduct(),
                        offerFromRequest, request.relatedProductId()));
            }
        }

        if (request.relatedProductId() == null) {
            offerFromRequest.setProduct(null);
            offerRepository.updateOrInsert(offerFromRequest);
        } else {
            assignOfferToNewProductFromRequest(offerFromRequest, operations, request.relatedProductId());
        }

        return operations;
    }

    private List<BaseSearchEngineOperation> getDeleteProductOperation(
            Product existingProduct, Offer offerFromRequest, String relatedProductId) {
        if (existingProduct != null && existingProduct.isValid()) {
            var existingOffers = offerRepository.findByProductId(existingProduct.getId());
//                if (request.relatedProductId() == null && existingOffers.size() == 1) {
            if (existingOffers.size() == 1) {
                // there is currently 1 offer related to the product
                // and request wants to delete it therefore, we need to delete a searchable product as well
                return List.of(SearchEngineOperationProvider.getDeleteSearchEngineOperation(existingProduct));
            } else if (relatedProductId == null && existingOffers.size() > 1) {
                // there are more than 1 offer related to the product
                // and request wants to delete 1 of them therefore, we need to update a searchable product
                // with updated list of offers
                offerFromRequest.setProduct(null);
                offerRepository.updateOrInsert(offerFromRequest);
                var offers = offerRepository.findByProductId(existingProduct.getId()).stream().map(Offer::getName).toList();
                return List.of(SearchEngineOperationProvider.getUpsertSearchEngineOperation(existingProduct, offers));
            }
        }
        return List.of();
    }

    private void assignOfferToNewProductFromRequest(Offer offer, List<BaseSearchEngineOperation> operations,
                                                    String relatedProductId) {
        var newProductOptional = productRepository.findById(relatedProductId);
        if (newProductOptional.isPresent()) {
            var existingProduct = newProductOptional.get();
            offer.setProduct(existingProduct);
            offerRepository.updateOrInsert(offer);
            if (newProductOptional.get().isValid()) {
                var offers = offerRepository.findByProductId(existingProduct.getId()).stream().map(Offer::getName).toList();
                operations.add(SearchEngineOperationProvider.getUpsertSearchEngineOperation(existingProduct, offers));
            }
        } else {
            var product = new Product();
            product.setId(relatedProductId);
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
    }
}