# Medicine Inventory Management System

A desktop application for tracking medicine inventory, built with Java 17, Swing, and SQLite.

## Requirements

- Java 17 (LTS) or later
- Maven 3.8+

## Building

```bash
mvn clean package
```

## Running

```bash
# Using Maven exec plugin
mvn exec:java

# Or running the built JAR with dependencies
java -cp "target/medicine-inventory-1.0.0.jar:target/lib/*" com.medicine.inventory.Main
```

## Project Structure

```
src/main/java/com/medicine/inventory/
├── Main.java                    # Application entry point
├── model/
│   └── Medicine.java            # Medicine domain model
├── dao/
│   ├── DatabaseManager.java     # SQLite connection management
│   └── MedicineDAO.java         # Data access for medicines
├── service/
│   ├── InventoryService.java    # Business logic & validation
│   └── DashboardSummary.java    # Dashboard data transfer object
├── ui/
│   ├── MainFrame.java           # Main application window
│   ├── DashboardPanel.java      # Dashboard with summary cards
│   ├── InventoryPanel.java      # Medicine table with search/filter
│   ├── MedicineFormDialog.java  # Add/Edit medicine form
│   └── renderers/
│       ├── ExpiryStatusRenderer.java
│       └── StockStatusRenderer.java
├── util/
│   ├── DateUtils.java           # Date parsing/formatting
│   └── UIUtils.java             # Dialog helpers
└── exception/
    ├── InventoryException.java
    ├── ValidationException.java
    ├── DatabaseException.java
    └── EntityNotFoundException.java
```

## Features

- **Add/Edit/Delete** medicines with validation
- **Search** by name (real-time debounced)
- **Filter** by category, expiry status, stock status
- **Dashboard** with summary counts and attention list
- **Expiry tracking**: Expired / Expiring Soon (≤30 days) / Valid
- **Low stock alerts** with configurable thresholds
- **Persistent storage** via SQLite database
- **Color-coded status** with text labels for accessibility

## Database

SQLite database file: `medicine_inventory.db` (created automatically in working directory)

Schema:
```sql
CREATE TABLE medicines (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT,
    quantity INTEGER NOT NULL DEFAULT 0 CHECK(quantity >= 0),
    purchase_date TEXT,
    expiry_date TEXT NOT NULL,
    notes TEXT,
    low_stock_threshold INTEGER NOT NULL DEFAULT 5 CHECK(low_stock_threshold > 0)
);
```

## Keyboard Shortcuts

- `Ctrl+Q` - Exit application
- `Ctrl+1` - Show Dashboard
- `Ctrl+2` - Show Inventory
- `Enter` - Edit selected medicine (Inventory tab)
- `Delete` - Delete selected medicine (Inventory tab)