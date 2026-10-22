package com.medicine.inventory.exception;

public class EntityNotFoundException extends InventoryException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}