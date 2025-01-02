package com.ingestionsystem.searchupdater.service;

import com.ingestionsystem.searchupdater.operation.RequestOperationType;

public record IngestionRequest(
    RequestOperationType operation,
    String offerId,
    String offerName,
    String productId,
    String relatedProductId,
    String productName)
{}
