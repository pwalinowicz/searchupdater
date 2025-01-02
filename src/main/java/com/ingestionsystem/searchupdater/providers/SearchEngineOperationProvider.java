package com.ingestionsystem.searchupdater.providers;

import com.ingestionsystem.searchupdater.model.Product;
import com.ingestionsystem.searchupdater.operation.BaseSearchEngineOperation;
import com.ingestionsystem.searchupdater.operation.DeleteOperation;
import com.ingestionsystem.searchupdater.operation.UpsertOperation;
import com.ingestionsystem.searchupdater.repository.OfferRepository;
import com.ingestionsystem.searchupdater.repository.ProductRepository;
import com.ingestionsystem.searchupdater.service.IngestionRequest;

import java.util.List;

public abstract class SearchEngineOperationProvider {
    protected final ProductRepository productRepository;
    protected final OfferRepository offerRepository;

    public SearchEngineOperationProvider(ProductRepository productRepository, OfferRepository offerRepository,
                                         IngestionRequest request) {
        validateRequestForProvider(request);
        this.productRepository = productRepository;
        this.offerRepository = offerRepository;
    }

    public static BaseSearchEngineOperation getDeleteSearchEngineOperation(Product existingProduct) {
        return new DeleteOperation(
                existingProduct.getId()
        );
    }

    public static BaseSearchEngineOperation getUpsertSearchEngineOperation(Product product, List<String> offers) {
        return new UpsertOperation(
                product.getId(),
                product.getName(),
                offers
        );
    }

    public abstract List<BaseSearchEngineOperation> getSearchOperations(IngestionRequest request);

    protected abstract void validateRequestForProvider(IngestionRequest request);
}
