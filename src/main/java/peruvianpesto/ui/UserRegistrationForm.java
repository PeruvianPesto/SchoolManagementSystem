package peruvianpesto.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import peruvianpesto.database.DatabaseConnection;

public class UserRegistrationForm {

    private Stage stage;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField nameField;
    private ComboBox<String> userTypeCombo;
    private Label statusLabel;

    public UserRegistrationForm(Stage parentStage) {
        createRegistrationWindow(parentStage);
    }

    private void createRegistrationWindow(Stage parentStage) {
        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Create New User");
        stage.setResizable(false);

        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        Label titleLabel = new Label("Create New User Account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        // Form container
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: #ffffff; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1;");

        // User Type Selection
        VBox userTypeContainer = new VBox(5);
        userTypeContainer.setAlignment(Pos.CENTER_LEFT);
        Label userTypeLabel = new Label("User Type:");
        userTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        userTypeCombo = new ComboBox<>();
        userTypeCombo.getItems().addAll("Student", "Teacher", "Admin");
        userTypeCombo.setPromptText("Select user type");
        userTypeCombo.setPrefWidth(200);

        userTypeContainer.getChildren().addAll(userTypeLabel, userTypeCombo);

        // Full Name field
        VBox nameContainer = new VBox(5);
        nameContainer.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Full Name:");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameField = new TextField();
        nameField.setPrefWidth(200);
        nameField.setPromptText("Enter full name");
        nameContainer.getChildren().addAll(nameLabel, nameField);

        // Username field
        VBox usernameContainer = new VBox(5);
        usernameContainer.setAlignment(Pos.CENTER_LEFT);
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        usernameField = new TextField();
        usernameField.setPrefWidth(200);
        usernameField.setPromptText("Enter username");
        usernameContainer.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordContainer = new VBox(5);
        passwordContainer.setAlignment(Pos.CENTER_LEFT);
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        passwordField = new PasswordField();
        passwordField.setPrefWidth(200);
        passwordField.setPromptText("Enter password");
        passwordContainer.getChildren().addAll(passwordLabel, passwordField);

        // Confirm Password field
        VBox confirmPasswordContainer = new VBox(5);
        confirmPasswordContainer.setAlignment(Pos.CENTER_LEFT);
        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPrefWidth(200);
        confirmPasswordField.setPromptText("Confirm password");
        confirmPasswordContainer.getChildren().addAll(confirmPasswordLabel, confirmPasswordField);

        // Status label
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(300);

        // Buttons
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button createButton = new Button("Create User");
        createButton.setPrefWidth(100);
        createButton.setStyle("-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setStyle("-fx-background-color: #f44336; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold;");

        buttonContainer.getChildren().addAll(createButton, cancelButton);

        // Add all components to form
        formContainer.getChildren().addAll(
                userTypeContainer,
                nameContainer,
                usernameContainer,
                passwordContainer,
                confirmPasswordContainer,
                buttonContainer
        );

        mainContainer.getChildren().addAll(titleLabel, formContainer, statusLabel);

        // Event handlers
        createButton.setOnAction(e -> handleCreateUser());
        cancelButton.setOnAction(e -> stage.close());

        // Handle Enter key press
        confirmPasswordField.setOnAction(e -> handleCreateUser());

        // Username field validation
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && DatabaseConnection.usernameExists(newValue)) {
                statusLabel.setText("Username already exists!");
                statusLabel.setStyle("-fx-text-fill: orange;");
            } else if (!statusLabel.getText().startsWith("User created") &&
                    !statusLabel.getText().startsWith("Please fill")) {
                statusLabel.setText("");
            }
        });

        // Create scene and show stage
        Scene scene = new Scene(mainContainer, 400, 550);
        stage.setScene(scene);
    }

    private void handleCreateUser() {
        // Get form values
        String userType = userTypeCombo.getValue();
        String fullName = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input
        if (userType == null || userType.isEmpty()) {
            showError("Please select a user type");
            return;
        }

        if (fullName.isEmpty()) {
            showError("Please enter full name");
            return;
        }

        if (username.isEmpty()) {
            showError("Please enter username");
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter password");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Check if username already exists
        if (DatabaseConnection.usernameExists(username)) {
            showError("Username already exists. Please choose a different username.");
            return;
        }

        // Validate username format for students (should be numeric)
        if (userType.equals("Student")) {
            try {
                Integer.parseInt(username);
            } catch (NumberFormatException e) {
                showError("Student username must be numeric (Student ID)");
                return;
            }
        }

        // Attempt to create user in database
        boolean success = DatabaseConnection.createUser(username, password, userType, fullName);

        if (success) {
            showSuccess("User created successfully!");

            // Show success dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Created");
            alert.setHeaderText(null);
            alert.setContentText("User '" + username + "' has been created successfully!\n" +
                    "Name: " + fullName + "\n" +
                    "Type: " + userType);
            alert.showAndWait();

            // Clear form for next user
            clearForm();
        } else {
            showError("Failed to create user. Please try again.");
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    private void clearForm() {
        userTypeCombo.setValue(null);
        nameField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        statusLabel.setText("");
    }

    public void show() {
        stage.show();
    }

    public void showAndWait() {
        stage.showAndWait();
    }
}