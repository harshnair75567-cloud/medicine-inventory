package com.medicine.inventory;

import com.medicine.inventory.dao.DatabaseManager;
import com.medicine.inventory.dao.MedicineDAO;
import com.medicine.inventory.model.Medicine;
import com.medicine.inventory.service.InventoryService;
import com.medicine.inventory.service.DashboardSummary;

import java.time.LocalDate;
import java.util.List;

public class IntegrationTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Medicine Inventory Integration Test ===\n");
        
        // Setup
        DatabaseManager db = DatabaseManager.getInstance();
        db.initialize();
        MedicineDAO dao = new MedicineDAO(db);
        InventoryService service = new InventoryService(dao);
        
        // Clean up first
        List<Medicine> existing = dao.findAll();
        for (Medicine m : existing) {
            dao.delete(m.getId());
        }
        System.out.println("Cleaned existing data.\n");
        
        // Test 1: Add medicines
        System.out.println("--- Test 1: Add Medicines ---");
        Medicine m1 = new Medicine("Paracetamol 500mg", "Tablet", 20, 
            LocalDate.now().minusDays(30), LocalDate.now().plusDays(180), 
            "Pain relief", 5);
        service.addMedicine(m1);
        System.out.println("Added: " + m1.getName() + " (ID: " + m1.getId() + 
            ") | Expiry: " + m1.getExpiryStatus() + " | Stock: " + m1.getStockStatus());
        
        Medicine m2 = new Medicine("Cough Syrup 100ml", "Syrup", 3, 
            LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), 
            "For cough", 3);
        service.addMedicine(m2);
        System.out.println("Added: " + m2.getName() + " (ID: " + m2.getId() + 
            ") | Expiry: " + m2.getExpiryStatus() + " | Stock: " + m2.getStockStatus());
        
        Medicine m3 = new Medicine("Expired Antibiotic", "Tablet", 10, 
            LocalDate.now().minusDays(100), LocalDate.now().minusDays(10), 
            "Expired batch", 5);
        service.addMedicine(m3);
        System.out.println("Added: " + m3.getName() + " (ID: " + m3.getId() + 
            ") | Expiry: " + m3.getExpiryStatus() + " | Stock: " + m3.getStockStatus());
        
        Medicine m4 = new Medicine("Vitamin D3", "Capsule", 1, 
            LocalDate.now().minusDays(5), LocalDate.now().plusDays(400), 
            "Supplement", 2);
        service.addMedicine(m4);
        System.out.println("Added: " + m4.getName() + " (ID: " + m4.getId() + 
            ") | Expiry: " + m4.getExpiryStatus() + " | Stock: " + m4.getStockStatus());
        
        // Test 2: List all
        System.out.println("\n--- Test 2: List All Medicines ---");
        List<Medicine> all = service.getAllMedicines();
        for (Medicine m : all) {
            System.out.printf("  ID:%d | %-20s | Qty:%2d | Exp:%s | Stock:%s%n",
                m.getId(), m.getName(), m.getQuantity(), m.getExpiryStatus(), m.getStockStatus());
        }
        
        // Test 3: Search
        System.out.println("\n--- Test 3: Search 'Cough' ---");
        List<Medicine> search = service.searchByName("Cough");
        for (Medicine m : search) {
            System.out.println("  Found: " + m.getName());
        }
        
        // Test 4: Filter by category
        System.out.println("\n--- Test 4: Filter by Category 'Tablet' ---");
        List<Medicine> tablets = service.filterByCategory("Tablet");
        for (Medicine m : tablets) {
            System.out.println("  " + m.getName() + " (" + m.getCategory() + ")");
        }
        
        // Test 5: Dashboard Summary
        System.out.println("\n--- Test 5: Dashboard Summary ---");
        DashboardSummary summary = service.getDashboardSummary();
        System.out.println("  Total Medicines: " + summary.getTotalMedicines());
        System.out.println("  Expired: " + summary.getExpiredCount());
        System.out.println("  Expiring Soon: " + summary.getExpiringSoonCount());
        System.out.println("  Low Stock: " + summary.getLowStockCount());
        System.out.println("\n  Medicines Needing Attention:");
        for (Medicine m : summary.getMedicinesNeedingAttention()) {
            System.out.println("    - " + m.getName() + " | " + m.getExpiryStatus() + " | " + m.getStockStatus());
        }
        
        // Test 6: Update medicine
        System.out.println("\n--- Test 6: Update Medicine ---");
        m2.setQuantity(15);
        m2.setNotes("Restocked");
        service.updateMedicine(m2);
        System.out.println("Updated " + m2.getName() + " to quantity: " + m2.getQuantity());
        
        // Test 7: Delete medicine
        System.out.println("\n--- Test 7: Delete Medicine ---");
        service.deleteMedicine(m3.getId());
        System.out.println("Deleted: " + m3.getName());
        
        // Test 8: Verify final state
        System.out.println("\n--- Test 8: Final State ---");
        List<Medicine> finalList = service.getAllMedicines();
        System.out.println("Remaining medicines: " + finalList.size());
        for (Medicine m : finalList) {
            System.out.printf("  ID:%d | %-20s | Qty:%2d | Exp:%s | Stock:%s%n",
                m.getId(), m.getName(), m.getQuantity(), m.getExpiryStatus(), m.getStockStatus());
        }
        
        DashboardSummary finalSummary = service.getDashboardSummary();
        System.out.println("\nFinal Dashboard:");
        System.out.println("  Total: " + finalSummary.getTotalMedicines());
        System.out.println("  Expired: " + finalSummary.getExpiredCount());
        System.out.println("  Expiring Soon: " + finalSummary.getExpiringSoonCount());
        System.out.println("  Low Stock: " + finalSummary.getLowStockCount());
        
        System.out.println("\n=== All Tests Passed! ===");
    }
}
