package com.ingestionsystem.searchupdater.operation;

import java.util.List;

public class UpsertOperation extends BaseSearchEngineOperation {
    private String productName;
    private List<String> offerNames;

    public UpsertOperation(String productId, String productName, List<String> offerNames) {
        super(productId, SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        this.productName = productName;
        this.offerNames = offerNames;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<String> getOfferNames() {
        return offerNames;
    }

    public void setOfferNames(List<String> offerNames) {
        this.offerNames = offerNames;
    }
}
