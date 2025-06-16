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

    private VBox loginFormContainer;
    private TextField usernameField;
    private PasswordField passwordField;
    private RadioButton adminRadio;
    private RadioButton studentRadio;
    private RadioButton teacherRadio;
    private ToggleGroup userTypeGroup;
    private Label statusLabel;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        Label titleLabel = new Label("School Management System");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        // Subtitle
        Label subtitleLabel = new Label("Please login to continue");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setStyle("-fx-text-fill: #666666;");

        // User type selection
        VBox userTypeContainer = new VBox(10);
        userTypeContainer.setAlignment(Pos.CENTER);

        Label userTypeLabel = new Label("Select User Type:");
        userTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        userTypeLabel.setStyle("-fx-text-fill: #333333;");

        // Radio buttons
        userTypeGroup = new ToggleGroup();
        adminRadio = new RadioButton("Administrator");
        teacherRadio = new RadioButton("Teacher");
        studentRadio = new RadioButton("Student");

        adminRadio.setToggleGroup(userTypeGroup);
        teacherRadio.setToggleGroup(userTypeGroup);
        studentRadio.setToggleGroup(userTypeGroup);

        adminRadio.setFont(Font.font("System", 14));
        teacherRadio.setFont(Font.font("System", 14));
        studentRadio.setFont(Font.font("System", 14));

        HBox radioContainer = new HBox(20);
        radioContainer.setAlignment(Pos.CENTER);
        radioContainer.getChildren().addAll(adminRadio, teacherRadio, studentRadio);

        userTypeContainer.getChildren().addAll(userTypeLabel, radioContainer);

        // Login form container (initially hidden)
        loginFormContainer = new VBox(15);
        loginFormContainer.setAlignment(Pos.CENTER);
        loginFormContainer.setPadding(new Insets(20));
        loginFormContainer.setStyle("-fx-background-color: #ffffff; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1;");
        loginFormContainer.setVisible(false);
        loginFormContainer.setManaged(false);

        // Username field
        VBox usernameContainer = new VBox(5);
        usernameContainer.setAlignment(Pos.CENTER);
        Label usernameLabel = new Label("Username/ID:");
        usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        usernameField = new TextField();
        usernameField.setPrefWidth(200);
        usernameField.setPromptText("Enter username or student ID");
        usernameContainer.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordContainer = new VBox(5);
        passwordContainer.setAlignment(Pos.CENTER);
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        passwordField = new PasswordField();
        passwordField.setPrefWidth(200);
        passwordField.setPromptText("Enter password");
        passwordContainer.getChildren().addAll(passwordLabel, passwordField);

        // Buttons
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(80);
        loginButton.setStyle("-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold;");

        Button createUserButton = new Button("Create User");
        createUserButton.setPrefWidth(100);
        createUserButton.setStyle("-fx-background-color: #2196F3; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold;");

        buttonContainer.getChildren().addAll(loginButton, createUserButton);

        loginFormContainer.getChildren().addAll(usernameContainer, passwordContainer, buttonContainer);

        // Status label
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setWrapText(true);

        // Add all components to main container
        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, userTypeContainer,
                loginFormContainer, statusLabel);

        // Event handlers
        userTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showLoginForm();
            }
        });

        loginButton.setOnAction(e -> handleLogin());
        createUserButton.setOnAction(e -> handleCreateUser());

        // Handle Enter key press
        passwordField.setOnAction(e -> handleLogin());

        // Create scene and show stage
        Scene scene = new Scene(mainContainer, 450, 650);
        primaryStage.setTitle("School Management System - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // Handle application close
        primaryStage.setOnCloseRequest(e -> {
            DatabaseConnection.closeConnection();
        });

        primaryStage.show();
    }

    private void showLoginForm() {
        loginFormContainer.setVisible(true);
        loginFormContainer.setManaged(true);

        // Update placeholder text based on user type
        RadioButton selected = (RadioButton) userTypeGroup.getSelectedToggle();
        if (selected != null) {
            if (selected == adminRadio) {
                usernameField.setPromptText("Admin Username");
                statusLabel.setText("Please enter admin credentials");
                statusLabel.setStyle("-fx-text-fill: #333333;");
            } else if (selected == teacherRadio) {
                usernameField.setPromptText("Teacher Username");
                statusLabel.setText("Please enter teacher credentials");
                statusLabel.setStyle("-fx-text-fill: #333333;");
            } else {
                usernameField.setPromptText("Student ID");
                statusLabel.setText("Please enter student credentials");
                statusLabel.setStyle("-fx-text-fill: #333333;");
            }
        }

        // Clear previous inputs
        usernameField.clear();
        passwordField.clear();
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        RadioButton selected = (RadioButton) userTypeGroup.getSelectedToggle();
        if (selected == null) {
            statusLabel.setText("Please select user type");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Authenticate with database
        DatabaseConnection.UserInfo userInfo = DatabaseConnection.authenticateUser(username, password);

        if (userInfo != null) {
            // Verify user type matches selection
            String expectedUserType = "";
            if (selected == adminRadio) expectedUserType = "admin";
            else if (selected == teacherRadio) expectedUserType = "teacher";
            else if (selected == studentRadio) expectedUserType = "student";

            if (!userInfo.getUserType().equals(expectedUserType)) {
                statusLabel.setText("Invalid user type for this account");
                statusLabel.setStyle("-fx-text-fill: red;");
                passwordField.clear();
                return;
            }

            statusLabel.setText("Login successful! Welcome " + userInfo.getName());
            statusLabel.setStyle("-fx-text-fill: green;");

            // Navigate to the appropriate dashboard
            navigateToMainApplication(userInfo);

        } else {
            statusLabel.setText("Invalid credentials. Please try again.");
            statusLabel.setStyle("-fx-text-fill: red;");
            passwordField.clear();
        }
    }

    private void handleCreateUser() {
        UserRegistrationForm registrationForm = new UserRegistrationForm(primaryStage);
        registrationForm.showAndWait();
    }

    private void navigateToMainApplication(DatabaseConnection.UserInfo userInfo) {
        // Close the login window
        primaryStage.close();

        // Open the appropriate dashboard based on user type
        switch (userInfo.getUserType()) {
            case "admin":
                AdminDashboard adminDashboard = new AdminDashboard(userInfo);
                adminDashboard.show();
                break;
            case "teacher":
                TeacherDashboard teacherDashboard = new TeacherDashboard(userInfo);
                teacherDashboard.show();
                break;
            case "student":
                StudentDashboard studentDashboard = new StudentDashboard(userInfo);
                studentDashboard.show();
                break;
            default:
                showGenericDashboard(userInfo, "User");
                break;
        }
    }

    private void showGenericDashboard(DatabaseConnection.UserInfo userInfo, String userType) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(userType + " Dashboard");
        alert.setHeaderText("Welcome " + userInfo.getName());
        alert.setContentText("The " + userType.toLowerCase() + " dashboard is not yet implemented.\n\n" +
                "User: " + userInfo.getName() + " (ID: " + userInfo.getId() + ")");
        alert.showAndWait();

        // Reopen the login window
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}