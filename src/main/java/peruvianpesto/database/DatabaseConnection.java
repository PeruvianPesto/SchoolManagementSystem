/**
 * DatabaseConnection.java
 *
 * A utility class for managing database connections and user operations
 * for the Peruvian Pesto School Management System.
 *
 * This class provides:
 * - Database connection management with MySQL
 * - User authentication with password hashing (SHA-256)
 * - User creation for students, teachers, and admins
 * - Transaction management for data integrity
 * - Username validation and duplicate checking
 *
 * Database Schema Requirements:
 * - users table: id (auto-increment), username, password_hash, user_type
 * - students table: id (FK to users.id), name
 * - teachers table: id (FK to users.id), name
 * - admins table: id (FK to users.id), name
 *
 * @author Peruvian Pesto Development Team
 * @version 1.0
 * @since 2024
 */

package peruvianpesto.database;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DatabaseConnection {
    // Database connection configuration constants
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/schoolmanagementsystem";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Ss44046552.";

    // Singleton connection instance to prevent multiple connections
    private static Connection connection = null;

    /**
     * Establishes and returns a database connection using singleton pattern.
     * Creates a new connection if one doesn't exist or if the existing connection is closed.
     *
     * @return Connection object to the MySQL database
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        // Check if connection is null or closed before creating new one
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish connection to database
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Database connection established successfully");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
                throw new SQLException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                System.err.println("Failed to establish database connection: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Hashes a plain text password using SHA-256 algorithm for secure storage.
     * The hashed password is then encoded to Base64 for database storage.
     *
     * @param password Plain text password to be hashed
     * @return Base64 encoded SHA-256 hash of the password
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    public static String hashPassword(String password) {
        try {
            // Create SHA-256 message digest instance
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Generate hash bytes from password
            byte[] hashedBytes = md.digest(password.getBytes());

            // Encode hash to Base64 string for storage
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Creates a new user in the database with the specified credentials and type.
     * Uses database transactions to ensure data integrity across multiple tables.
     *
     * @param username Unique username for the new user
     * @param password Plain text password (will be hashed automatically)
     * @param userType Type of user: "student", "teacher", or "admin"
     * @param name Full name of the user
     * @return true if user creation is successful, false otherwise
     */
    public static boolean createUser(String username, String password, String userType, String name) {
        // SQL query to insert into main users table
        String userSql = "INSERT INTO users (username, password_hash, user_type) VALUES (?, ?, ?)";
        String specificSql = "";

        // Determine the specific table SQL based on user type
        switch (userType.toLowerCase()) {
            case "student":
                specificSql = "INSERT INTO students (id, name) VALUES (?, ?)";
                break;
            case "teacher":
                specificSql = "INSERT INTO teachers (id, name) VALUES (?, ?)";
                break;
            case "admin":
                specificSql = "INSERT INTO admins (id, name) VALUES (?, ?)";
                break;
            default:
                System.out.println("Invalid user type: " + userType);
                return false;
        }

        Connection conn = null;
        try {
            // Get database connection
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction for data integrity

            // Insert into main users table first
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, hashPassword(password)); // Hash password before storing
            userStmt.setString(3, userType.toLowerCase());

            // Execute insert and check if successful
            int rowsAffected = userStmt.executeUpdate();
            if (rowsAffected == 0) {
                conn.rollback(); // Rollback if insert failed
                return false;
            }

            // Get the auto-generated user ID from users table
            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            int userId = 0;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            } else {
                conn.rollback(); // Rollback if ID generation failed
                return false;
            }

            // Insert into specific user type table (students/teachers/admins)
            PreparedStatement specificStmt = conn.prepareStatement(specificSql);
            specificStmt.setInt(1, userId); // Use the generated ID as foreign key
            specificStmt.setString(2, name);

            // Execute specific table insert
            int specificRowsAffected = specificStmt.executeUpdate();
            if (specificRowsAffected == 0) {
                conn.rollback(); // Rollback if specific table insert failed
                return false;
            }

            // Commit transaction if all operations successful
            conn.commit();
            System.out.println("User created successfully with ID: " + userId);
            return true;

        } catch (SQLException e) {
            // Rollback transaction on any SQL error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }

            // Handle specific error cases
            if (e.getErrorCode() == 1062) { // MySQL duplicate entry error code
                System.out.println("Username already exists: " + username);
            } else {
                System.out.println("Error creating user: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        } finally {
            // Reset auto-commit to default state
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Authenticates a user by checking username and password against the database.
     * Joins user table with appropriate user type table to get complete user information.
     *
     * @param username Username to authenticate
     * @param password Plain text password to verify
     * @return UserInfo object containing user details if authentication successful, null otherwise
     */
    public static UserInfo authenticateUser(String username, String password) {
        // Complex SQL query with CASE statement to get name based on user type
        String sql = "SELECT u.id, u.user_type, " +
                "CASE " +
                "  WHEN u.user_type = 'student' THEN s.name " +
                "  WHEN u.user_type = 'teacher' THEN t.name " +
                "  WHEN u.user_type = 'admin' THEN a.name " +
                "END as name " +
                "FROM users u " +
                "LEFT JOIN students s ON u.id = s.id AND u.user_type = 'student' " +
                "LEFT JOIN teachers t ON u.id = t.id AND u.user_type = 'teacher' " +
                "LEFT JOIN admins a ON u.id = a.id AND u.user_type = 'admin' " +
                "WHERE u.username = ? AND u.password_hash = ?";

        try {
            // Get database connection
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password)); // Hash input password for comparison

            // Execute query and check results
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Create and return UserInfo object with retrieved data
                return new UserInfo(
                        rs.getInt("id"),
                        username,
                        rs.getString("name"),
                        rs.getString("user_type")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Authentication failed - user not found or wrong password
    }

    /**
     * Checks if a username already exists in the database.
     * Used for validation during user registration.
     *
     * @param username Username to check for existence
     * @return true if username exists, false otherwise
     */
    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Return true if count is greater than 0
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
        }
        return false; // Default to false on error
    }

    /**
     * Properly closes the database connection to free up resources.
     * Should be called when the application shuts down.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inner class to encapsulate user information returned from database queries.
     * Provides a clean way to pass user data between methods and classes.
     */
    public static class UserInfo {
        private int id;           // User's unique database ID
        private String username;  // User's login username
        private String name;      // User's full name
        private String userType;  // User's role: student, teacher, or admin

        /**
         * Constructor to create a new UserInfo object.
         *
         * @param id User's unique database identifier
         * @param username User's login username
         * @param name User's full name
         * @param userType User's role in the system
         */
        public UserInfo(int id, String username, String name, String userType) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.userType = userType;
        }

        // Getter methods to access private fields
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getName() { return name; }
        public String getUserType() { return userType; }

        /**
         * Returns a string representation of the UserInfo object.
         * Useful for debugging and logging purposes.
         *
         * @return Formatted string containing all user information
         */
        @Override
        public String toString() {
            return "UserInfo{id=" + id + ", username='" + username +
                    "', name='" + name + "', userType='" + userType + "'}";
        }
    }
}