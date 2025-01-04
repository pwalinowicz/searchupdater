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
                operations.addAll(
                        getOperationsForExistingProduct(existingOffer.get().getProduct(), offerFromRequest)
                );
            }
        }

        if (request.relatedProductId() == null) {
            // if relatedProductId is null then we don't need upsert operation for a new product because there isn't one
            // byt we must set a null association between newOffer and non-existing product
            deleteAssociationBetweenOfferAndProduct(offerFromRequest);
        } else {
            operations.addAll(getOperationsForNewProduct(offerFromRequest, request.relatedProductId()));
        }

        return operations;
    }

    private List<BaseSearchEngineOperation> getOperationsForExistingProduct(
            Product existingProduct, Offer offerFromRequest) {
        if (existingProduct != null && existingProduct.isValid()) {
            var existingOffers = offerRepository.findByProductId(existingProduct.getId());

            if (existingOffers.size() == 1) {
                // there is currently 1 offer related to the product
                // and request wants to delete it therefore, we need to delete a searchable product as well
                return List.of(SearchEngineOperationProvider.getDeleteSearchEngineOperation(existingProduct));
            } else if (existingOffers.size() > 1) {
                // there are more than 1 offer related to the product
                // and request wants to delete 1 of them therefore, we need to update a searchable product
                // with updated list of offers
                deleteAssociationBetweenOfferAndProduct(offerFromRequest);
                var offers = offerRepository.findByProductId(existingProduct.getId()).stream().map(Offer::getName).toList();
                return List.of(SearchEngineOperationProvider.getUpsertSearchEngineOperation(existingProduct, offers));
            }
        }
        return List.of();
    }

    private List<BaseSearchEngineOperation> getOperationsForNewProduct(Offer offer, String relatedProductId) {
        var newProductOptional = productRepository.findById(relatedProductId);
        if (newProductOptional.isPresent()) {
            var newProduct = newProductOptional.get();
            associateOfferAndProduct(offer, newProduct);
            if (newProductOptional.get().isValid()) {
                var offers = offerRepository.findByProductId(newProduct.getId()).stream().map(Offer::getName).toList();
                return List.of(SearchEngineOperationProvider.getUpsertSearchEngineOperation(newProduct, offers));
            }
        } else {
            var product = new Product();
            product.setId(relatedProductId);
            productRepository.updateOrInsert(product);
            associateOfferAndProduct(offer, product);
        }
        return List.of();
    }

    private void deleteAssociationBetweenOfferAndProduct(Offer offer) {
        associateOfferAndProduct(offer, null);
    }

    private void associateOfferAndProduct(Offer offer, Product product) {
        offer.setProduct(product);
        offerRepository.updateOrInsert(offer);
    }

    @Override
    protected void validateRequestForProvider(IngestionRequest request) {
        var operation = request.operation();
        FieldValidator.validateField("offerId", request.offerId(), operation);
        FieldValidator.validateField("offerName", request.offerName(), operation);
    }
}
