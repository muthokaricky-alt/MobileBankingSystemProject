package wire;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:wire.db";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("Database connection successful!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            throw new SQLException("SQLite JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
    
    public static void initializeDatabase() {
        System.out.println("Initializing database...");
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Create users table with name field
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    id_number TEXT UNIQUE NOT NULL,
                    phone_number TEXT UNIQUE NOT NULL,
                    pin TEXT NOT NULL,
                    balance REAL DEFAULT 0.00,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);
            System.out.println("Users table created/verified");
            
            // Create admins table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS admins (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);
            System.out.println("Admins table created/verified");
            
            // Create transactions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    transaction_type TEXT,
                    amount REAL,
                    recipient_phone TEXT,
                    till_number TEXT,
                    transaction_fee REAL DEFAULT 0.00,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);
            System.out.println("Transactions table created/verified");
            
            // Create savings table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS savings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    amount REAL,
                    interest_rate REAL DEFAULT 5.00,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);
            System.out.println("Savings table created/verified");
            
            // Create company income table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS company_income (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id INTEGER,
                    transaction_fee REAL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
                )
            """);
            System.out.println("Company income table created/verified");
            
            // Insert default admin
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admins WHERE username = 'admin'");
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO admins (username, password) VALUES ('admin', 'admin123')");
                System.out.println("Default admin created: username='admin', password='admin123'");
            } else {
                System.out.println("Admin user already exists");
            }
            
            System.out.println("Database initialized successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Test method to check database status
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println("✅ Database connection test: SUCCESS");
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ Database connection test: FAILED - " + e.getMessage());
        }
    }
}