package com.ingestionsystem.searchupdater.providers;

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
            productRepository.delete(product);
            operations.add(SearchEngineOperationProvider.getDeleteSearchEngineOperation(product));
        });
        return operations;
    }

    @Override
    protected void validateRequestForProvider(IngestionRequest request) {
        FieldValidator.validateField("productId", request.productId(), request.operation());
    }
}