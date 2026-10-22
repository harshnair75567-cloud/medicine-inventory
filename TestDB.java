import com.medicine.inventory.dao.DatabaseManager;
import com.medicine.inventory.dao.MedicineDAO;
import com.medicine.inventory.model.Medicine;
import java.time.LocalDate;

public class TestDB {
    public static void main(String[] args) throws Exception {
        DatabaseManager db = DatabaseManager.getInstance();
        db.initialize();
        MedicineDAO dao = new MedicineDAO(db);
        
        Medicine m1 = new Medicine("Paracetamol", "Tablet", 10, LocalDate.now().minusDays(30), 
            LocalDate.now().plusDays(60), "Pain relief", 5);
        dao.insert(m1);
        System.out.println("Added: " + m1.getName() + " (ID: " + m1.getId() + ")");
        
        Medicine m2 = new Medicine("Cough Syrup", "Syrup", 2, LocalDate.now().minusDays(10), 
            LocalDate.now().plusDays(5), "For cough", 3);
        dao.insert(m2);
        System.out.println("Added: " + m2.getName() + " (ID: " + m2.getId() + ")");
        
        Medicine m3 = new Medicine("Expired Medicine", "Tablet", 5, LocalDate.now().minusDays(100), 
            LocalDate.now().minusDays(10), "Expired", 5);
        dao.insert(m3);
        System.out.println("Added: " + m3.getName() + " (ID: " + m3.getId() + ")");
        
        System.out.println("\nAll medicines:");
        for (Medicine m : dao.findAll()) {
            System.out.println("  " + m.getName() + " | Qty: " + m.getQuantity() + 
                " | Expiry: " + m.getExpiryDate() + " | Status: " + m.getExpiryStatus() + " | Stock: " + m.getStockStatus());
        }
    }
}
