package com.ingestionsystem.searchupdater.service;

import com.ingestionsystem.searchupdater.operation.*;
import com.ingestionsystem.searchupdater.providers.*;
import com.ingestionsystem.searchupdater.repository.OfferRepository;
import com.ingestionsystem.searchupdater.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpdaterServiceImpl implements UpdaterService {

    private final static Logger logger = LoggerFactory.getLogger(UpdaterServiceImpl.class);
    private final ProductRepository productRepository;
    private final OfferRepository offerRepository;

    @Autowired
    public UpdaterServiceImpl(ProductRepository productRepository, OfferRepository offerRepository) {
        this.productRepository = productRepository;
        this.offerRepository = offerRepository;
    }

    @Transactional
    @Override
    public List<BaseSearchEngineOperation> getBaseSearchEngineOperations(IngestionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body can't be null");
        }
        var provider = getProvider(request);
        return provider.getSearchOperations(request);
    }

    private SearchEngineOperationProvider getProvider(IngestionRequest request) {
        switch (request.operation()) {
            case DELETE_OFFER -> {
                return new DeleteOfferOperationProvider(productRepository, offerRepository, request);
            }
            case UPSERT_OFFER -> {
                return new UpsertOfferOperationProvider(productRepository, offerRepository, request);
            }
            case DELETE_PRODUCT -> {
                return new DeleteProductOperationProvider(productRepository, offerRepository, request);
            }
            case UPSERT_PRODUCT -> {
                return new UpsertProductOperationProvider(productRepository, offerRepository, request);
            }
            default -> throw new IllegalArgumentException("Incorrect operation");
        }
    }
}
