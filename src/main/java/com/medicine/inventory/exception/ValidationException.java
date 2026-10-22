package com.medicine.inventory.exception;

import java.util.List;

public class ValidationException extends InventoryException {
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}