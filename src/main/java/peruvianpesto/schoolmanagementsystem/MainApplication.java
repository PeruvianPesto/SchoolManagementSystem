/**
 * MainApplication.java
 *
 * This is the main entry point for the Peruvian Pesto School Management System.
 * It provides a login interface that authenticates users and routes them to
 * appropriate dashboards based on their user type (Admin, Teacher, or Student).
 *
 * Key Features:
 * - User type selection (Admin, Teacher, Student)
 * - Secure authentication via database
 * - Dynamic form updates based on user type
 * - Navigation to role-specific dashboards
 * - User registration capability
 * - Responsive UI with proper styling
 *
 * UI Components:
 * - Radio buttons for user type selection
 * - Login form with username/password fields
 * - Status messages for user feedback
 * - Buttons for login and user creation
 *
 * Navigation Flow:
 * 1. User selects their type (Admin/Teacher/Student)
 * 2. Login form appears with appropriate prompts
 * 3. User enters credentials and clicks login
 * 4. System authenticates and routes to dashboard
 *
 * @author Peruvian Pesto Team
 * @version 1.0
 */

package peruvianpesto.schoolmanagementsystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import peruvianpesto.database.DatabaseConnection;
import peruvianpesto.ui.StudentDashboard;
import peruvianpesto.ui.TeacherDashboard;
import peruvianpesto.ui.UserRegistrationForm;
import peruvianpesto.ui.AdminDashboard;

public class MainApplication extends Application {

    // UI Components - Login Form Elements
    private VBox loginFormContainer;        // Container for the login form
    private TextField usernameField;        // Username/ID input field
    private PasswordField passwordField;   // Password input field

    // User Type Selection Components
    private RadioButton adminRadio;         // Admin user type selection
    private RadioButton studentRadio;       // Student user type selection
    private RadioButton teacherRadio;       // Teacher user type selection
    private ToggleGroup userTypeGroup;      // Groups radio buttons for single selection

    // Feedback and Navigation Components
    private Label statusLabel;              // Displays status messages to user
    private Stage primaryStage;             // Main application window

    /**
     * The main entry point for the JavaFX application.
     * Sets up the login interface with user type selection and authentication form.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create main container with spacing and styling
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;"); // Light gray background

        // Application title
        Label titleLabel = new Label("School Management System");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #333333;"); // Dark gray text

        // Subtitle for user guidance
        Label subtitleLabel = new Label("Please login to continue");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setStyle("-fx-text-fill: #666666;"); // Medium gray text

        // User type selection section
        VBox userTypeContainer = new VBox(10);
        userTypeContainer.setAlignment(Pos.CENTER);

        Label userTypeLabel = new Label("Select User Type:");
        userTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        userTypeLabel.setStyle("-fx-text-fill: #333333;");

        // Create radio buttons for user type selection
        userTypeGroup = new ToggleGroup(); // Ensures only one can be selected at a time
        adminRadio = new RadioButton("Administrator");
        teacherRadio = new RadioButton("Teacher");
        studentRadio = new RadioButton("Student");

        // Associate radio buttons with the toggle group
        adminRadio.setToggleGroup(userTypeGroup);
        teacherRadio.setToggleGroup(userTypeGroup);
        studentRadio.setToggleGroup(userTypeGroup);

        // Set consistent font for all radio buttons
        adminRadio.setFont(Font.font("System", 14));
        teacherRadio.setFont(Font.font("System", 14));
        studentRadio.setFont(Font.font("System", 14));

        // Horizontal container for radio buttons
        HBox radioContainer = new HBox(20);
        radioContainer.setAlignment(Pos.CENTER);
        radioContainer.getChildren().addAll(adminRadio, teacherRadio, studentRadio);

        userTypeContainer.getChildren().addAll(userTypeLabel, radioContainer);

        // Login form container (initially hidden until user type is selected)
        loginFormContainer = new VBox(15);
        loginFormContainer.setAlignment(Pos.CENTER);
        loginFormContainer.setPadding(new Insets(20));
        loginFormContainer.setStyle("-fx-background-color: #ffffff; " +      // White background
                "-fx-background-radius: 10; " +                               // Rounded corners
                "-fx-border-color: #cccccc; " +                               // Light border
                "-fx-border-radius: 10; " +                                   // Rounded border
                "-fx-border-width: 1;");                                      // Border thickness
        loginFormContainer.setVisible(false);  // Hide initially
        loginFormContainer.setManaged(false);  // Don't allocate space when hidden

        // Username input section
        VBox usernameContainer = new VBox(5);
        usernameContainer.setAlignment(Pos.CENTER);
        Label usernameLabel = new Label("Username/ID:");
        usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        usernameField = new TextField();
        usernameField.setPrefWidth(200);
        usernameField.setPromptText("Enter username or student ID"); // Default placeholder
        usernameContainer.getChildren().addAll(usernameLabel, usernameField);

        // Password input section
        VBox passwordContainer = new VBox(5);
        passwordContainer.setAlignment(Pos.CENTER);
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        passwordField = new PasswordField();
        passwordField.setPrefWidth(200);
        passwordField.setPromptText("Enter password");
        passwordContainer.getChildren().addAll(passwordLabel, passwordField);

        // Button section
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        // Login button with green styling
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(80);
        loginButton.setStyle("-fx-background-color: #4CAF50; " +      // Green background
                "-fx-text-fill: white; " +                            // White text
                "-fx-font-weight: bold;");                            // Bold font

        // Create user button with blue styling
        Button createUserButton = new Button("Create User");
        createUserButton.setPrefWidth(100);
        createUserButton.setStyle("-fx-background-color: #2196F3; " + // Blue background
                "-fx-text-fill: white; " +                            // White text
                "-fx-font-weight: bold;");                            // Bold font

        buttonContainer.getChildren().addAll(loginButton, createUserButton);

        // Add all form elements to the login container
        loginFormContainer.getChildren().addAll(usernameContainer, passwordContainer, buttonContainer);

        // Status label for displaying messages to the user
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setWrapText(true); // Allow text wrapping for long messages

        // Add all main components to the primary container
        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, userTypeContainer,
                loginFormContainer, statusLabel);

        // Event handlers for user interactions

        // Show login form when user type is selected
        userTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showLoginForm();
            }
        });

        // Handle login button click
        loginButton.setOnAction(e -> handleLogin());

        // Handle create user button click
        createUserButton.setOnAction(e -> handleCreateUser());

        // Allow Enter key to trigger login from password field
        passwordField.setOnAction(e -> handleLogin());

        // Create and configure the main scene
        Scene scene = new Scene(mainContainer, 450, 650);
        primaryStage.setTitle("School Management System - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Fixed window size for consistent layout

        // Clean up database connections when application closes
        primaryStage.setOnCloseRequest(e -> {
            DatabaseConnection.closeConnection();
        });

        // Display the application window
        primaryStage.show();
    }

    /**
     * Shows the login form and updates the interface based on the selected user type.
     * This method is called when a user type radio button is selected.
     * It customizes the form prompts and status messages for the selected user type.
     */
    private void showLoginForm() {
        // Make the login form visible and allocate layout space
        loginFormContainer.setVisible(true);
        loginFormContainer.setManaged(true);

        // Get the currently selected radio button
        RadioButton selected = (RadioButton) userTypeGroup.getSelectedToggle();
        if (selected != null) {
            // Update UI elements based on selected user type
            if (selected == adminRadio) {
                usernameField.setPromptText("Admin Username");
                statusLabel.setText("Please enter admin credentials");
                statusLabel.setStyle("-fx-text-fill: #333333;");
            } else if (selected == teacherRadio) {
                usernameField.setPromptText("Teacher Username");
                statusLabel.setText("Please enter teacher credentials");
                statusLabel.setStyle("-fx-text-fill: #333333;");
            } else { // Student selected
                usernameField.setPromptText("Student ID");
                statusLabel.setText("Please enter student credentials");
                statusLabel.setStyle("-fx-text-fill: #333333;");
            }
        }

        // Clear any previous input to ensure clean state
        usernameField.clear();
        passwordField.clear();
    }

    /**
     * Handles the login process when the user clicks the login button or presses Enter.
     * This method validates input, authenticates the user with the database,
     * verifies the user type matches the selection, and navigates to the appropriate dashboard.
     */
    private void handleLogin() {
        // Get and validate input fields
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Check for empty fields
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Ensure user type is selected
        RadioButton selected = (RadioButton) userTypeGroup.getSelectedToggle();
        if (selected == null) {
            statusLabel.setText("Please select user type");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Authenticate user with database
        DatabaseConnection.UserInfo userInfo = DatabaseConnection.authenticateUser(username, password);

        if (userInfo != null) {
            // Determine expected user type based on radio button selection
            String expectedUserType = "";
            if (selected == adminRadio) expectedUserType = "admin";
            else if (selected == teacherRadio) expectedUserType = "teacher";
            else if (selected == studentRadio) expectedUserType = "student";

            // Verify that the authenticated user type matches the selection
            if (!userInfo.getUserType().equals(expectedUserType)) {
                statusLabel.setText("Invalid user type for this account");
                statusLabel.setStyle("-fx-text-fill: red;");
                passwordField.clear(); // Clear password for security
                return;
            }

            // Authentication successful
            statusLabel.setText("Login successful! Welcome " + userInfo.getName());
            statusLabel.setStyle("-fx-text-fill: green;");

            // Navigate to the appropriate dashboard
            navigateToMainApplication(userInfo);

        } else {
            // Authentication failed
            statusLabel.setText("Invalid credentials. Please try again.");
            statusLabel.setStyle("-fx-text-fill: red;");
            passwordField.clear(); // Clear password for security
        }
    }

    /**
     * Opens the user registration form in a modal dialog.
     * This allows new users to create accounts in the system.
     */
    private void handleCreateUser() {
        UserRegistrationForm registrationForm = new UserRegistrationForm(primaryStage);
        registrationForm.showAndWait(); // Modal dialog - blocks until closed
    }

    /**
     * Navigates to the appropriate dashboard based on the authenticated user's type.
     * Closes the login window and opens the role-specific dashboard.
     *
     * @param userInfo The authenticated user's information including ID, name, and type
     */
    private void navigateToMainApplication(DatabaseConnection.UserInfo userInfo) {
        // Close the login window
        primaryStage.close();

        // Open the appropriate dashboard based on user type
        switch (userInfo.getUserType()) {
            case "admin":
                // Open administrator dashboard with full system access
                AdminDashboard adminDashboard = new AdminDashboard(userInfo);
                adminDashboard.show();
                break;

            case "teacher":
                // Open teacher dashboard with course and student management
                TeacherDashboard teacherDashboard = new TeacherDashboard(userInfo);
                teacherDashboard.show();
                break;

            case "student":
                // Open student dashboard with course viewing and grade access
                StudentDashboard studentDashboard = new StudentDashboard(userInfo);
                studentDashboard.show();
                break;

            default:
                // Fallback for unknown user types (should not occur with proper validation)
                showGenericDashboard(userInfo, "User");
                break;
        }
    }

    /**
     * Shows a generic placeholder dashboard for unimplemented user types.
     * This is a fallback method that displays basic user information
     * and reopens the login window.
     *
     * @param userInfo The user's information
     * @param userType The type of user (for display purposes)
     */
    private void showGenericDashboard(DatabaseConnection.UserInfo userInfo, String userType) {
        // Create an information dialog as a placeholder
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(userType + " Dashboard");
        alert.setHeaderText("Welcome " + userInfo.getName());
        alert.setContentText("The " + userType.toLowerCase() + " dashboard is not yet implemented.\n\n" +
                "User: " + userInfo.getName() + " (ID: " + userInfo.getId() + ")");
        alert.showAndWait();

        // Return to login window since dashboard is not implemented
        primaryStage.show();
    }

    /**
     * The main method that launches the JavaFX application.
     * This is the entry point when the application is run.
     *
     * @param args Command line arguments (not used in this application)
     */
    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}