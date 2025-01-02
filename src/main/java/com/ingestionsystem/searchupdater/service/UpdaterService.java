package com.ingestionsystem.searchupdater.service;

import com.ingestionsystem.searchupdater.operation.BaseSearchEngineOperation;

import java.util.List;

public interface UpdaterService {
    List<BaseSearchEngineOperation> getBaseSearchEngineOperations(IngestionRequest request);
}
