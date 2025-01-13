package com.ingestionsystem.searchupdater.providers;

import com.ingestionsystem.searchupdater.model.Product;
import com.ingestionsystem.searchupdater.operation.BaseSearchEngineOperation;
import com.ingestionsystem.searchupdater.repository.OfferRepository;
import com.ingestionsystem.searchupdater.repository.ProductRepository;
import com.ingestionsystem.searchupdater.service.IngestionRequest;
import com.ingestionsystem.searchupdater.validation.FieldValidator;

import java.util.ArrayList;
import java.util.List;

public class DeleteProductOperationProvider extends SearchEngineOperationProvider {

    public DeleteProductOperationProvider(ProductRepository productRepository, OfferRepository offerRepository, IngestionRequest request) {
        super(productRepository, offerRepository, request);
    }

    @Override
    public List<BaseSearchEngineOperation> getSearchOperations(IngestionRequest request) {
        var operations = new ArrayList<BaseSearchEngineOperation>();

        var productId = request.productId();
        var productOptional = productRepository.findById(productId);
        productOptional.ifPresent(product -> {
            var offers = offerRepository.findByProductId(product.getId());
            if (!offers.isEmpty()) {
                operations.add(SearchEngineOperationProvider.getDeleteSearchEngineOperation(product));
            }
            deleteAssociationBetweenProductAndOffers(product);
            productRepository.delete(product);
        });
        return operations;
    }

    @Override
    protected void validateRequestForProvider(IngestionRequest request) {
        FieldValidator.validateField("productId", request.productId(), request.operation());
    }

    private void deleteAssociationBetweenProductAndOffers(Product product) {
        var offers = offerRepository.findByProductId(product.getId());
        offers.forEach(offer -> offer.setProduct(null));
    }
}