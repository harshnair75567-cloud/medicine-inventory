package com.medicine.inventory.dao;

import com.medicine.inventory.exception.DatabaseException;
import com.medicine.inventory.exception.EntityNotFoundException;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

@FunctionalInterface
interface CheckedFunction<T, R> {
    R apply(T t) throws DatabaseException, EntityNotFoundException, SQLException;
}

@FunctionalInterface
interface CheckedConsumer<T> {
    void accept(T t) throws DatabaseException, EntityNotFoundException, SQLException;
}

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:medicine_inventory.db";
    private static DatabaseManager instance;
    private boolean initialized = false;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initialize() throws DatabaseException {
        if (initialized) return;
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = createConnection()) {
                createSchema(conn);
            }
            initialized = true;
            LOGGER.info("Database initialized successfully");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("SQLite JDBC driver not found", e);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database", e);
        }
    }

    private Connection createConnection() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        return DriverManager.getConnection(DB_URL, config.toProperties());
    }

    private void createSchema(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS medicines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT,
                quantity INTEGER NOT NULL DEFAULT 0 CHECK(quantity >= 0),
                purchase_date TEXT,
                expiry_date TEXT NOT NULL,
                notes TEXT,
                low_stock_threshold INTEGER NOT NULL DEFAULT 5 CHECK(low_stock_threshold > 0)
            );
            CREATE INDEX IF NOT EXISTS idx_medicines_name ON medicines(name);
            CREATE INDEX IF NOT EXISTS idx_medicines_expiry ON medicines(expiry_date);
            CREATE INDEX IF NOT EXISTS idx_medicines_category ON medicines(category);
            """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public <T> T execute(CheckedFunction<Connection, T> work) throws DatabaseException, EntityNotFoundException {
        try (Connection conn = createConnection()) {
            return work.apply(conn);
        } catch (SQLException e) {
            throw new DatabaseException("Database operation failed", e);
        }
    }

    public void executeVoid(CheckedConsumer<Connection> work) throws DatabaseException, EntityNotFoundException {
        execute(conn -> {
            work.accept(conn);
            return null;
        });
    }
}
