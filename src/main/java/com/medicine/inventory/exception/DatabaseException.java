package com.medicine.inventory.exception;

public class DatabaseException extends InventoryException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}