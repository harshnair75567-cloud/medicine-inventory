package com.medicine.inventory.service;

import com.medicine.inventory.dao.MedicineDAO;
import com.medicine.inventory.exception.DatabaseException;
import com.medicine.inventory.exception.EntityNotFoundException;
import com.medicine.inventory.exception.ValidationException;
import com.medicine.inventory.model.Medicine;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class InventoryService {
    private static final Logger LOGGER = Logger.getLogger(InventoryService.class.getName());
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;
    private static final int EXPIRY_WARNING_DAYS = 30;

    private final MedicineDAO medicineDAO;

    public InventoryService(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
    }

    public Medicine addMedicine(Medicine medicine) throws ValidationException, DatabaseException, EntityNotFoundException {
        validateMedicine(medicine);
        if (medicine.getLowStockThreshold() <= 0) {
            medicine.setLowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD);
        }
        medicineDAO.insert(medicine);
        LOGGER.info("Added medicine: " + medicine.getName());
        return medicine;
    }

    public Medicine updateMedicine(Medicine medicine) throws ValidationException, DatabaseException, EntityNotFoundException {
        validateMedicine(medicine);
        if (medicine.getLowStockThreshold() <= 0) {
            medicine.setLowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD);
        }
        medicineDAO.update(medicine);
        LOGGER.info("Updated medicine: " + medicine.getName());
        return medicine;
    }

    public void deleteMedicine(int id) throws DatabaseException, EntityNotFoundException {
        medicineDAO.delete(id);
        LOGGER.info("Deleted medicine with id: " + id);
    }

    public List<Medicine> getAllMedicines() throws DatabaseException, EntityNotFoundException {
        return medicineDAO.findAll();
    }

    public List<Medicine> searchByName(String searchText) throws DatabaseException, EntityNotFoundException {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllMedicines();
        }
        return medicineDAO.findByName(searchText.trim());
    }

    public List<Medicine> filterByCategory(String category) throws DatabaseException, EntityNotFoundException {
        if (category == null || category.equals("All")) {
            return getAllMedicines();
        }
        return medicineDAO.findByCategory(category);
    }

    public List<String> getCategories() throws DatabaseException, EntityNotFoundException {
        return medicineDAO.getDistinctCategories();
    }

    public DashboardSummary getDashboardSummary() throws DatabaseException, EntityNotFoundException {
        List<Medicine> all = getAllMedicines();
        int total = 0, expired = 0, expiringSoon = 0, lowStock = 0;
        List<Medicine> attention = new ArrayList<>();

        for (Medicine m : all) {
            total++;
            String expiryStatus = calculateExpiryStatus(m.getExpiryDate());
            String stockStatus = calculateStockStatus(m.getQuantity(), m.getLowStockThreshold());

            boolean needsAttention = false;
            if ("Expired".equals(expiryStatus)) {
                expired++;
                needsAttention = true;
            } else if ("Expiring Soon".equals(expiryStatus)) {
                expiringSoon++;
                needsAttention = true;
            }
            if ("Low Stock".equals(stockStatus)) {
                lowStock++;
                needsAttention = true;
            }
            if (needsAttention) {
                attention.add(m);
            }
        }

        attention.sort(Comparator
            .comparing((Medicine m) -> !"Expired".equals(calculateExpiryStatus(m.getExpiryDate())))
            .thenComparing(m -> {
                LocalDate exp = m.getExpiryDate();
                return exp != null ? ChronoUnit.DAYS.between(LocalDate.now(), exp) : Long.MAX_VALUE;
            })
            .thenComparing(Medicine::getQuantity)
        );

        return new DashboardSummary(total, expired, expiringSoon, lowStock, attention);
    }

    public List<Medicine> getMedicinesWithFilters(String searchText, String category,
                                                   String expiryFilter, String stockFilter)
            throws DatabaseException, EntityNotFoundException {
        List<Medicine> base;
        if (searchText != null && !searchText.trim().isEmpty()) {
            base = searchByName(searchText);
        } else if (category != null && !category.equals("All")) {
            base = filterByCategory(category);
        } else {
            base = getAllMedicines();
        }

        List<Medicine> result = new ArrayList<>();
        for (Medicine m : base) {
            String expiryStatus = calculateExpiryStatus(m.getExpiryDate());
            String stockStatus = calculateStockStatus(m.getQuantity(), m.getLowStockThreshold());

            boolean matchesExpiry = expiryFilter == null || expiryFilter.equals("All")
                || expiryStatus.equals(expiryFilter);
            boolean matchesStock = stockFilter == null || stockFilter.equals("All")
                || stockStatus.equals(stockFilter);

            if (matchesExpiry && matchesStock) {
                result.add(m);
            }
        }
        return result;
    }

    private void validateMedicine(Medicine medicine) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (medicine.getName() == null || medicine.getName().trim().isEmpty()) {
            errors.add("Medicine name is required");
        }

        if (medicine.getExpiryDate() == null) {
            errors.add("Expiry date is required");
        }

        if (medicine.getQuantity() < 0) {
            errors.add("Quantity must be non-negative");
        }

        if (medicine.getPurchaseDate() != null && medicine.getPurchaseDate().isAfter(LocalDate.now())) {
            errors.add("Purchase date cannot be in the future");
        }

        if (medicine.getExpiryDate() != null && medicine.getPurchaseDate() != null
                && medicine.getExpiryDate().isBefore(medicine.getPurchaseDate())) {
            errors.add("Expiry date cannot be before purchase date");
        }

        if (medicine.getLowStockThreshold() <= 0) {
            errors.add("Low-stock threshold must be positive");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public static String calculateExpiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) return "Unknown";
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, expiryDate);
        if (days < 0) return "Expired";
        if (days <= EXPIRY_WARNING_DAYS) return "Expiring Soon";
        return "Valid";
    }

    public static String calculateStockStatus(int quantity, int threshold) {
        return quantity <= threshold ? "Low Stock" : "In Stock";
    }

    public void shutdown() {
        // Placeholder for any cleanup
    }
}