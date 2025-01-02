package com.ingestionsystem.searchupdater.operation;

public class DeleteOperation extends BaseSearchEngineOperation {
    public DeleteOperation(String productId) {
        super(productId, SearchEngineOperationType.DELETE_SEARCHABLE_PRODUCT);
    }
}
