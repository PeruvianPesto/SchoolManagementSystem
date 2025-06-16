package peruvianpesto.database;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/schoolmanagementsystem";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Ss44046552.";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
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

    // Hash password for security
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Create a new user in the database
    public static boolean createUser(String username, String password, String userType, String name) {
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
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert into users table
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, hashPassword(password));
            userStmt.setString(3, userType.toLowerCase());

            int rowsAffected = userStmt.executeUpdate();
            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }

            // Get the generated user ID
            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            int userId = 0;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            // Insert into specific user type table
            PreparedStatement specificStmt = conn.prepareStatement(specificSql);
            specificStmt.setInt(1, userId);
            specificStmt.setString(2, name);

            int specificRowsAffected = specificStmt.executeUpdate();
            if (specificRowsAffected == 0) {
                conn.rollback();
                return false;
            }

            conn.commit(); // Commit transaction
            System.out.println("User created successfully with ID: " + userId);
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }

            if (e.getErrorCode() == 1062) { // MySQL duplicate entry error
                System.out.println("Username already exists: " + username);
            } else {
                System.out.println("Error creating user: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Authenticate user login
    public static UserInfo authenticateUser(String username, String password) {
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
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
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

        return null; // Authentication failed
    }

    // Check if username exists
    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
        }
        return false;
    }

    // Close connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Inner class to hold user information
    public static class UserInfo {
        private int id;
        private String username;
        private String name;
        private String userType;

        public UserInfo(int id, String username, String name, String userType) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.userType = userType;
        }

        // Getters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getName() { return name; }
        public String getUserType() { return userType; }

        @Override
        public String toString() {
            return "UserInfo{id=" + id + ", username='" + username +
                    "', name='" + name + "', userType='" + userType + "'}";
        }
    }
}