package com.medicine.inventory.service;

import com.medicine.inventory.model.Medicine;
import java.util.List;

public class DashboardSummary {
    private final int totalMedicines;
    private final int expiredCount;
    private final int expiringSoonCount;
    private final int lowStockCount;
    private final List<Medicine> medicinesNeedingAttention;

    public DashboardSummary(int totalMedicines, int expiredCount, int expiringSoonCount,
                            int lowStockCount, List<Medicine> medicinesNeedingAttention) {
        this.totalMedicines = totalMedicines;
        this.expiredCount = expiredCount;
        this.expiringSoonCount = expiringSoonCount;
        this.lowStockCount = lowStockCount;
        this.medicinesNeedingAttention = medicinesNeedingAttention;
    }

    public int getTotalMedicines() { return totalMedicines; }
    public int getExpiredCount() { return expiredCount; }
    public int getExpiringSoonCount() { return expiringSoonCount; }
    public int getLowStockCount() { return lowStockCount; }
    public List<Medicine> getMedicinesNeedingAttention() { return medicinesNeedingAttention; }
}