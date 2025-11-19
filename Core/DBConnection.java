package Core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:h2:file:C:/BankSoftware/mutemabank;AUTO_SERVER=TRUE";  // file-based database (saved in user folder)
    private static final String USER = "sa";               // default H2 username
    private static final String PASSWORD = "";             // default H2 password

    // Method to get database connection
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load H2 JDBC Driver (optional for modern JDBC)
            Class.forName("org.h2.Driver");

            // Establish the connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to H2 database successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("❌ H2 Driver not found: " + e.getMessage());
        }
        return connection;
    }
}
