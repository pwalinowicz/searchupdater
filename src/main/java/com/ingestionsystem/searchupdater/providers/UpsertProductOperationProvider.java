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
import java.util.Optional;

public class UpsertProductOperationProvider extends SearchEngineOperationProvider {
    public UpsertProductOperationProvider(ProductRepository productRepository, OfferRepository offerRepository,
                                          IngestionRequest request) {
        super(productRepository, offerRepository, request);
    }

    @Override
    public List<BaseSearchEngineOperation> getSearchOperations(IngestionRequest request) {
        var operations = new ArrayList<BaseSearchEngineOperation>();
        getProductToUpsert(request).ifPresent(product ->{
            productRepository.updateOrInsert(product);
            var offers = offerRepository.findByProductId(product.getId()).stream().map(Offer::getName).toList();
            if (!offers.isEmpty()) {
                operations.add(SearchEngineOperationProvider.getUpsertSearchEngineOperation(product, offers));
            }
        });
        return operations;
    }

    @Override
    protected void validateRequestForProvider(IngestionRequest request) {
        var operation = request.operation();
        FieldValidator.validateField("productId", request.productId(), operation);
        FieldValidator.validateField("productName", request.productName(), operation);
    }

    private Optional<Product> getProductToUpsert(IngestionRequest request) {
        var requestProduct = new Product(request.productId(), request.productName());
        var productOptional = productRepository.findById(request.productId());
        if (productOptional.isPresent()) {
            var existingProduct = productOptional.get();
            if (requestProduct.equals(existingProduct)) {
                return Optional.empty();
            } else {
                existingProduct.setName(request.productName());
                return Optional.of(existingProduct);
            }
        } else {
            return Optional.of(requestProduct);
        }
    }
}
