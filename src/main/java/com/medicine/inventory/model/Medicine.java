package com.medicine.inventory.model;

import java.time.LocalDate;

public class Medicine {
    private int id;
    private String name;
    private String category;
    private int quantity;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private String notes;
    private int lowStockThreshold;

    public Medicine() {
        this.lowStockThreshold = 5;
        this.quantity = 0;
    }

    public Medicine(String name, String category, int quantity, LocalDate purchaseDate,
                    LocalDate expiryDate, String notes, int lowStockThreshold) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.notes = notes;
        this.lowStockThreshold = lowStockThreshold > 0 ? lowStockThreshold : 5;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold > 0 ? lowStockThreshold : 5;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        return days >= 0 && days <= 30;
    }

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    public String getExpiryStatus() {
        if (expiryDate == null) return "Unknown";
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        if (days < 0) return "Expired";
        if (days <= 30) return "Expiring Soon";
        return "Valid";
    }

    public String getStockStatus() {
        return quantity <= lowStockThreshold ? "Low Stock" : "In Stock";
    }

    @Override
    public String toString() {
        return "Medicine{id=" + id + ", name='" + name + "', qty=" + quantity + "}";
    }
}