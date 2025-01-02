package com.ingestionsystem.searchupdater.validation;

import com.ingestionsystem.searchupdater.operation.RequestOperationType;

public class FieldValidator {
    public static void validateField(String fieldName, String fieldValue, RequestOperationType operationType) {
        if (fieldValue == null) {
            var message = String.format("Field: %s must not be null for operation: %s", fieldName, operationType);
            throw new IllegalArgumentException(message);
        }
    }
}
