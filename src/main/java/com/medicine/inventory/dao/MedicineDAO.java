package com.medicine.inventory.dao;

import com.medicine.inventory.exception.DatabaseException;
import com.medicine.inventory.exception.EntityNotFoundException;
import com.medicine.inventory.model.Medicine;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MedicineDAO {
    private static final Logger LOGGER = Logger.getLogger(MedicineDAO.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DatabaseManager dbManager;

    public MedicineDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public int insert(Medicine medicine) throws DatabaseException, EntityNotFoundException {
        String sql = """
            INSERT INTO medicines (name, category, quantity, purchase_date, expiry_date, notes, low_stock_threshold)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        return dbManager.execute(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setStatementParams(stmt, medicine);
                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new DatabaseException("Insert failed, no rows affected");
                }
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        medicine.setId(keys.getInt(1));
                        return medicine.getId();
                    }
                }
                throw new DatabaseException("Insert failed, no ID generated");
            } catch (SQLException e) {
                throw new DatabaseException("Failed to insert medicine", e);
            }
        });
    }

    public int update(Medicine medicine) throws DatabaseException, EntityNotFoundException {
        String sql = """
            UPDATE medicines SET name=?, category=?, quantity=?, purchase_date=?, expiry_date=?, notes=?, low_stock_threshold=?
            WHERE id=?
            """;
        return dbManager.execute(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setStatementParams(stmt, medicine);
                stmt.setInt(8, medicine.getId());
                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new EntityNotFoundException("Medicine not found with id: " + medicine.getId());
                }
                return affected;
            } catch (SQLException e) {
                throw new DatabaseException("Failed to update medicine", e);
            }
        });
    }

    public int delete(int id) throws DatabaseException, EntityNotFoundException {
        String sql = "DELETE FROM medicines WHERE id = ?";
        return dbManager.execute(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new EntityNotFoundException("Medicine not found with id: " + id);
                }
                return affected;
            } catch (SQLException e) {
                throw new DatabaseException("Failed to delete medicine", e);
            }
        });
    }

    public List<Medicine> findAll() throws DatabaseException, EntityNotFoundException {
        String sql = "SELECT * FROM medicines ORDER BY name";
        return dbManager.execute(conn -> executeQuery(conn, sql));
    }

    public List<Medicine> findByName(String searchText) throws DatabaseException, EntityNotFoundException {
        String sql = "SELECT * FROM medicines WHERE LOWER(name) LIKE ? ORDER BY name";
        return dbManager.execute(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + searchText.toLowerCase() + "%");
                return executeQuery(stmt);
            } catch (SQLException e) {
                throw new DatabaseException("Failed to search medicines", e);
            }
        });
    }

    public List<Medicine> findByCategory(String category) throws DatabaseException, EntityNotFoundException {
        String sql = "SELECT * FROM medicines WHERE category = ? ORDER BY name";
        return dbManager.execute(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category);
                return executeQuery(stmt);
            } catch (SQLException e) {
                throw new DatabaseException("Failed to filter by category", e);
            }
        });
    }

    public List<String> getDistinctCategories() throws DatabaseException, EntityNotFoundException {
        String sql = "SELECT DISTINCT category FROM medicines WHERE category IS NOT NULL ORDER BY category";
        return dbManager.execute(conn -> {
            List<String> categories = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("category");
                    if (cat != null && !cat.isBlank()) {
                        categories.add(cat);
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to get categories", e);
            }
            return categories;
        });
    }

    private List<Medicine> executeQuery(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSet(rs);
        }
    }

    private List<Medicine> executeQuery(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            return mapResultSet(rs);
        }
    }

    private List<Medicine> mapResultSet(ResultSet rs) throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        while (rs.next()) {
            Medicine m = new Medicine();
            m.setId(rs.getInt("id"));
            m.setName(rs.getString("name"));
            m.setCategory(rs.getString("category"));
            m.setQuantity(rs.getInt("quantity"));
            m.setNotes(rs.getString("notes"));
            m.setLowStockThreshold(rs.getInt("low_stock_threshold"));

            String purchaseDateStr = rs.getString("purchase_date");
            if (purchaseDateStr != null && !purchaseDateStr.isBlank()) {
                m.setPurchaseDate(LocalDate.parse(purchaseDateStr, DATE_FORMATTER));
            }

            String expiryDateStr = rs.getString("expiry_date");
            if (expiryDateStr != null && !expiryDateStr.isBlank()) {
                m.setExpiryDate(LocalDate.parse(expiryDateStr, DATE_FORMATTER));
            }
            medicines.add(m);
        }
        return medicines;
    }

    private void setStatementParams(PreparedStatement stmt, Medicine m) throws SQLException {
        stmt.setString(1, m.getName());
        stmt.setString(2, m.getCategory());
        stmt.setInt(3, m.getQuantity());
        stmt.setString(4, m.getPurchaseDate() != null ? m.getPurchaseDate().format(DATE_FORMATTER) : null);
        stmt.setString(5, m.getExpiryDate() != null ? m.getExpiryDate().format(DATE_FORMATTER) : null);
        stmt.setString(6, m.getNotes());
        stmt.setInt(7, m.getLowStockThreshold());
    }
}
