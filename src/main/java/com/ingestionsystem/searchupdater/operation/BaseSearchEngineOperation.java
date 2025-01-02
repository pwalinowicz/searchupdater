package com.ingestionsystem.searchupdater.operation;

public abstract class BaseSearchEngineOperation {
    private String productId;
    private SearchEngineOperationType operationType;

    public BaseSearchEngineOperation(String productId, SearchEngineOperationType operationType) {
        this.productId = productId;
        this.operationType = operationType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public SearchEngineOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(SearchEngineOperationType operationType) {
        this.operationType = operationType;
    }
}
