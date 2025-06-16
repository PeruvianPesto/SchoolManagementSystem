package peruvianpesto.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import peruvianpesto.database.DatabaseConnection;
import peruvianpesto.database.AdminDatabaseOperations;
import peruvianpesto.schoolmanagementsystem.MainApplication;

public class AdminDashboard {

    private Stage stage;
    private DatabaseConnection.UserInfo adminInfo;
    private TabPane tabPane;
    private Label statusLabel;

    // Student management components
    private TableView<StudentInfo> studentTable;
    private ObservableList<StudentInfo> studentData;

    // Teacher management components
    private TableView<TeacherInfo> teacherTable;
    private ObservableList<TeacherInfo> teacherData;

    // Course management components
    private TableView<CourseInfo> courseTable;
    private ObservableList<CourseInfo> courseData;

    // Course assignment components
    private ComboBox<TeacherInfo> teacherCombo;
    private ComboBox<CourseInfo> courseCombo;
    private TableView<CourseAssignmentInfo> assignmentTable;
    private ObservableList<CourseAssignmentInfo> assignmentData;

    public AdminDashboard(DatabaseConnection.UserInfo adminInfo) {
        this.adminInfo = adminInfo;
        createAdminWindow();
        loadAllData();
    }

    private void createAdminWindow() {
        stage = new Stage();
        stage.setTitle("Admin Dashboard - " + adminInfo.getName());
        stage.setResizable(true);

        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        Label headerLabel = new Label("Admin Dashboard");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerLabel.setStyle("-fx-text-fill: #333333;");

        Label welcomeLabel = new Label("Welcome, " + adminInfo.getName() + " (Admin ID: " + adminInfo.getId() + ")");
        welcomeLabel.setFont(Font.font("System", 14));
        welcomeLabel.setStyle("-fx-text-fill: #666666;");

        // Status label
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setWrapText(true);

        // Create tab pane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create tabs
        Tab studentTab = createStudentManagementTab();
        Tab teacherTab = createTeacherManagementTab();
        Tab courseTab = createCourseManagementTab();
        Tab assignmentTab = createCourseAssignmentTab();

        tabPane.getTabs().addAll(studentTab, teacherTab, courseTab, assignmentTab);

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> {
            stage.close();
            new MainApplication().start(new Stage()); // Reopen login window
        });

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(headerLabel, new Label(" | "), welcomeLabel);

        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.getChildren().add(headerBox);
        HBox.setHgrow(headerBox, javafx.scene.layout.Priority.ALWAYS);

        HBox logoutBox = new HBox();
        logoutBox.setAlignment(Pos.CENTER_RIGHT);
        logoutBox.getChildren().add(logoutButton);

        HBox fullHeaderBox = new HBox();
        fullHeaderBox.getChildren().addAll(topBox, logoutBox);
        HBox.setHgrow(topBox, javafx.scene.layout.Priority.ALWAYS);

        mainContainer.getChildren().addAll(fullHeaderBox, tabPane, statusLabel);

        Scene scene = new Scene(mainContainer, 900, 700);
        stage.setScene(scene);
    }

    private Tab createStudentManagementTab() {
        Tab tab = new Tab("Student Management");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Student table
        studentTable = new TableView<>();
        studentData = FXCollections.observableArrayList();

        TableColumn<StudentInfo, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<StudentInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<StudentInfo, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<StudentInfo, Integer> coursesCol = new TableColumn<>("Enrolled Courses");
        coursesCol.setCellValueFactory(new PropertyValueFactory<>("enrolledCourses"));
        coursesCol.setPrefWidth(120);

        studentTable.getColumns().addAll(idCol, nameCol, usernameCol, coursesCol);
        studentTable.setItems(studentData);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button addStudentBtn = new Button("Add Student");
        addStudentBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addStudentBtn.setOnAction(e -> showAddStudentDialog());

        Button removeStudentBtn = new Button("Remove Student");
        removeStudentBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        removeStudentBtn.setOnAction(e -> removeSelectedStudent());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadStudentData());

        buttonBox.getChildren().addAll(addStudentBtn, removeStudentBtn, refreshBtn);

        content.getChildren().addAll(
                new Label("Student Management"),
                studentTable,
                buttonBox
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createTeacherManagementTab() {
        Tab tab = new Tab("Teacher Management");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Teacher table
        teacherTable = new TableView<>();
        teacherData = FXCollections.observableArrayList();

        TableColumn<TeacherInfo, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<TeacherInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<TeacherInfo, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<TeacherInfo, Integer> coursesCol = new TableColumn<>("Courses Teaching");
        coursesCol.setCellValueFactory(new PropertyValueFactory<>("coursesTeaching"));
        coursesCol.setPrefWidth(120);

        teacherTable.getColumns().addAll(idCol, nameCol, usernameCol, coursesCol);
        teacherTable.setItems(teacherData);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button addTeacherBtn = new Button("Add Teacher");
        addTeacherBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addTeacherBtn.setOnAction(e -> showAddTeacherDialog());

        Button removeTeacherBtn = new Button("Remove Teacher");
        removeTeacherBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        removeTeacherBtn.setOnAction(e -> removeSelectedTeacher());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadTeacherData());

        buttonBox.getChildren().addAll(addTeacherBtn, removeTeacherBtn, refreshBtn);

        content.getChildren().addAll(
                new Label("Teacher Management"),
                teacherTable,
                buttonBox
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createCourseManagementTab() {
        Tab tab = new Tab("Course Management");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Course table
        courseTable = new TableView<>();
        courseData = FXCollections.observableArrayList();

        TableColumn<CourseInfo, Integer> crnCol = new TableColumn<>("CRN");
        crnCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        crnCol.setPrefWidth(80);

        TableColumn<CourseInfo, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(200);

        TableColumn<CourseInfo, Float> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(80);

        TableColumn<CourseInfo, Integer> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        capacityCol.setPrefWidth(80);

        TableColumn<CourseInfo, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        instructorCol.setPrefWidth(150);

        courseTable.getColumns().addAll(crnCol, nameCol, creditsCol, capacityCol, instructorCol);
        courseTable.setItems(courseData);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button addCourseBtn = new Button("Add Course");
        addCourseBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addCourseBtn.setOnAction(e -> showAddCourseDialog());

        Button removeCourseBtn = new Button("Remove Course");
        removeCourseBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        removeCourseBtn.setOnAction(e -> removeSelectedCourse());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadCourseData());

        buttonBox.getChildren().addAll(addCourseBtn, removeCourseBtn, refreshBtn);

        content.getChildren().addAll(
                new Label("Course Management"),
                courseTable,
                buttonBox
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createCourseAssignmentTab() {
        Tab tab = new Tab("Course Assignment");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Assign Courses to Teachers");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Assignment form
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        // Teacher selection
        HBox teacherBox = new HBox(10);
        teacherBox.setAlignment(Pos.CENTER_LEFT);
        Label teacherLabel = new Label("Select Teacher:");
        teacherLabel.setPrefWidth(120);
        teacherCombo = new ComboBox<>();
        teacherCombo.setPrefWidth(200);
        teacherCombo.setPromptText("Choose a teacher");
        teacherBox.getChildren().addAll(teacherLabel, teacherCombo);

        // Course selection
        HBox courseBox = new HBox(10);
        courseBox.setAlignment(Pos.CENTER_LEFT);
        Label courseLabel = new Label("Select Course:");
        courseLabel.setPrefWidth(120);
        courseCombo = new ComboBox<>();
        courseCombo.setPrefWidth(200);
        courseCombo.setPromptText("Choose a course");
        courseBox.getChildren().addAll(courseLabel, courseCombo);

        // Buttons
        HBox assignButtonBox = new HBox(10);
        assignButtonBox.setAlignment(Pos.CENTER_LEFT);

        Button assignBtn = new Button("Assign Course");
        assignBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        assignBtn.setOnAction(e -> assignCourseToTeacher());

        Button unassignBtn = new Button("Unassign Course");
        unassignBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        unassignBtn.setOnAction(e -> unassignCourseFromTeacher());

        Button refreshComboBtn = new Button("Refresh Lists");
        refreshComboBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshComboBtn.setOnAction(e -> loadComboBoxData());

        assignButtonBox.getChildren().addAll(assignBtn, unassignBtn, refreshComboBtn);

        formBox.getChildren().addAll(teacherBox, courseBox, assignButtonBox);

        // Current assignments table
        Label assignmentsLabel = new Label("Current Course Assignments");
        assignmentsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        assignmentTable = new TableView<>();
        assignmentData = FXCollections.observableArrayList();

        TableColumn<CourseAssignmentInfo, String> teacherNameCol = new TableColumn<>("Teacher");
        teacherNameCol.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        teacherNameCol.setPrefWidth(150);

        TableColumn<CourseAssignmentInfo, String> courseNameCol = new TableColumn<>("Course");
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        courseNameCol.setPrefWidth(200);

        TableColumn<CourseAssignmentInfo, Integer> crnCol = new TableColumn<>("CRN");
        crnCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        crnCol.setPrefWidth(80);

        assignmentTable.getColumns().addAll(teacherNameCol, courseNameCol, crnCol);
        assignmentTable.setItems(assignmentData);

        // Refresh assignments button
        Button refreshAssignmentsBtn = new Button("Refresh Assignments");
        refreshAssignmentsBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshAssignmentsBtn.setOnAction(e -> loadAssignmentData());

        content.getChildren().addAll(titleLabel, formBox, assignmentsLabel, assignmentTable, refreshAssignmentsBtn);

        tab.setContent(content);
        return tab;
    }

    private void showAddStudentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student information");
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);

        // Create form fields
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField maxUnitsField = new TextField();
        maxUnitsField.setPromptText("Max Units (default: 18)");
        maxUnitsField.setText("18.0");

        content.getChildren().addAll(
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("Full Name:"), nameField,
                new Label("Max Units:"), maxUnitsField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String name = nameField.getText().trim();
                String maxUnitsText = maxUnitsField.getText().trim();

                if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    showStatus("Please fill in all required fields", false);
                    return null;
                }

                float maxUnits = 18.0f;
                try {
                    maxUnits = Float.parseFloat(maxUnitsText);
                } catch (NumberFormatException e) {
                    showStatus("Invalid max units value, using default 18.0", false);
                }

                if (AdminDatabaseOperations.addStudent(username, password, name, maxUnits)) {
                    showStatus("Student added successfully", true);
                    loadStudentData();
                } else {
                    showStatus("Failed to add student", false);
                }
            }
            return buttonType;
        });

        dialog.showAndWait();
    }

    private void showAddTeacherDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Teacher");
        dialog.setHeaderText("Enter teacher information");
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);

        // Create form fields
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField maxCoursesField = new TextField();
        maxCoursesField.setPromptText("Max Courses (default: 5)");
        maxCoursesField.setText("5");

        TextField coursesTaughtField = new TextField();
        coursesTaughtField.setPromptText("Courses Currently Teaching (default: 0)");
        coursesTaughtField.setText("0");

        content.getChildren().addAll(
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("Full Name:"), nameField,
                new Label("Max Courses:"), maxCoursesField,
                new Label("Current Courses:"), coursesTaughtField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String name = nameField.getText().trim();
                String maxCoursesText = maxCoursesField.getText().trim();
                String coursesTaughtText = coursesTaughtField.getText().trim();

                if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    showStatus("Please fill in all required fields", false);
                    return null;
                }

                int maxCourses = 5;
                int coursesTaught = 0;
                try {
                    maxCourses = Integer.parseInt(maxCoursesText);
                    coursesTaught = Integer.parseInt(coursesTaughtText);
                } catch (NumberFormatException e) {
                    showStatus("Invalid course numbers, using defaults", false);
                }

                if (AdminDatabaseOperations.addTeacher(username, password, name, maxCourses, coursesTaught)) {
                    showStatus("Teacher added successfully", true);
                    loadTeacherData();
                } else {
                    showStatus("Failed to add teacher", false);
                }
            }
            return buttonType;
        });

        dialog.showAndWait();
    }

    private void showAddCourseDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        dialog.setHeaderText("Enter course information");
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);

        // Create form fields
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField crnField = new TextField();
        crnField.setPromptText("CRN (Course Reference Number)");

        TextField courseNameField = new TextField();
        courseNameField.setPromptText("Course Name");

        TextField creditsField = new TextField();
        creditsField.setPromptText("Credits");

        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity");

        content.getChildren().addAll(
                new Label("CRN:"), crnField,
                new Label("Course Name:"), courseNameField,
                new Label("Credits:"), creditsField,
                new Label("Capacity:"), capacityField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String crnText = crnField.getText().trim();
                String courseName = courseNameField.getText().trim();
                String creditsText = creditsField.getText().trim();
                String capacityText = capacityField.getText().trim();

                if (crnText.isEmpty() || courseName.isEmpty() || creditsText.isEmpty() || capacityText.isEmpty()) {
                    showStatus("Please fill in all fields", false);
                    return null;
                }

                try {
                    int crn = Integer.parseInt(crnText);
                    float credits = Float.parseFloat(creditsText);
                    int capacity = Integer.parseInt(capacityText);

                    if (AdminDatabaseOperations.addCourse(courseName, crn, credits, capacity)) {
                        showStatus("Course added successfully", true);
                        loadCourseData();
                        loadComboBoxData(); // Refresh course combo box
                    } else {
                        showStatus("Failed to add course", false);
                    }
                } catch (NumberFormatException e) {
                    showStatus("Please enter valid numbers for CRN, credits, and capacity", false);
                }
            }
            return buttonType;
        });

        dialog.showAndWait();
    }

    private void removeSelectedStudent() {
        StudentInfo selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select a student to remove", false);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove Student");
        alert.setContentText("Are you sure you want to remove student: " + selected.getName() + "?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (AdminDatabaseOperations.removeUser(selected.getId())) {
                showStatus("Student removed successfully", true);
                loadStudentData();
            } else {
                showStatus("Failed to remove student", false);
            }
        }
    }

    private void removeSelectedTeacher() {
        TeacherInfo selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select a teacher to remove", false);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove Teacher");
        alert.setContentText("Are you sure you want to remove teacher: " + selected.getName() + "?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (AdminDatabaseOperations.removeUser(selected.getId())) {
                showStatus("Teacher removed successfully", true);
                loadTeacherData();
                loadComboBoxData(); // Refresh teacher combo box
                loadAssignmentData(); // Refresh assignments
            } else {
                showStatus("Failed to remove teacher", false);
            }
        }
    }

    private void removeSelectedCourse() {
        CourseInfo selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select a course to remove", false);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove Course");
        alert.setContentText("Are you sure you want to remove course: " + selected.getCourseName() + "?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (AdminDatabaseOperations.removeCourse(selected.getCrn())) {
                showStatus("Course removed successfully", true);
                loadCourseData();
                loadComboBoxData(); // Refresh course combo box
                loadAssignmentData(); // Refresh assignments
            } else {
                showStatus("Failed to remove course", false);
            }
        }
    }

    private void assignCourseToTeacher() {
        TeacherInfo teacher = teacherCombo.getValue();
        CourseInfo course = courseCombo.getValue();

        if (teacher == null || course == null) {
            showStatus("Please select both teacher and course", false);
            return;
        }

        if (AdminDatabaseOperations.assignCourseToTeacher(teacher.getId(), course.getCrn())) {
            showStatus("Course assigned successfully", true);
            loadTeacherData();
            loadCourseData();
            loadAssignmentData();
        } else {
            showStatus("Failed to assign course (may already be assigned)", false);
        }
    }

    private void unassignCourseFromTeacher() {
        TeacherInfo teacher = teacherCombo.getValue();
        CourseInfo course = courseCombo.getValue();

        if (teacher == null || course == null) {
            showStatus("Please select both teacher and course", false);
            return;
        }

        if (AdminDatabaseOperations.unassignCourseFromTeacher(teacher.getId(), course.getCrn())) {
            showStatus("Course unassigned successfully", true);
            loadTeacherData();
            loadCourseData();
            loadAssignmentData();
        } else {
            showStatus("Failed to unassign course (may not be assigned)", false);
        }
    }

    private void loadAllData() {
        try {
            System.out.println("Loading student data...");
            loadStudentData();
            System.out.println("Loading teacher data...");
            loadTeacherData();
            System.out.println("Loading course data...");
            loadCourseData();
            System.out.println("Loading combo box data...");
            loadComboBoxData();
            System.out.println("Loading assignment data...");
            loadAssignmentData();
            showStatus("Data loaded successfully", true);
        } catch (Exception e) {
            showStatus("Error loading data: " + e.getMessage(), false);
            e.printStackTrace();

            // Show detailed error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Data Loading Error");
            alert.setHeaderText("Failed to load data");
            alert.setContentText("Error details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadStudentData() {
        studentData.clear();
        studentData.addAll(AdminDatabaseOperations.getAllStudents());
    }

    private void loadTeacherData() {
        teacherData.clear();
        teacherData.addAll(AdminDatabaseOperations.getAllTeachers());
    }

    private void loadCourseData() {
        courseData.clear();
        courseData.addAll(AdminDatabaseOperations.getAllCourses());
    }

    private void loadComboBoxData() {
        teacherCombo.getItems().clear();
        teacherCombo.getItems().addAll(AdminDatabaseOperations.getAllTeachers());

        courseCombo.getItems().clear();
        courseCombo.getItems().addAll(AdminDatabaseOperations.getAllCourses());
    }

    private void loadAssignmentData() {
        assignmentData.clear();
        assignmentData.addAll(AdminDatabaseOperations.getAllCourseAssignments());
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    public void show() {
        stage.show();
    }

    // Data classes for table views
    public static class StudentInfo {
        private int id;
        private String name;
        private String username;
        private int enrolledCourses;

        public StudentInfo(int id, String name, String username, int enrolledCourses) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.enrolledCourses = enrolledCourses;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getUsername() { return username; }
        public int getEnrolledCourses() { return enrolledCourses; }
    }

    public static class TeacherInfo {
        private int id;
        private String name;
        private String username;
        private int coursesTeaching;

        public TeacherInfo(int id, String name, String username, int coursesTeaching) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.coursesTeaching = coursesTeaching;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getUsername() { return username; }
        public int getCoursesTeaching() { return coursesTeaching; }

        @Override
        public String toString() {
            return name + " (" + username + ")";
        }
    }

    public static class CourseInfo {
        private final int crn;
        private final String courseName;
        private final float credits;
        private final int capacity;
        private final String instructor;

        public CourseInfo(int crn, String courseName, float credits, int capacity, String instructor) {
            this.crn = crn;
            this.courseName = courseName;
            this.credits = credits;
            this.capacity = capacity;
            this.instructor = instructor;
        }

        // Getters
        public int getCrn() { return crn; }
        public String getCourseName() { return courseName; }
        public float getCredits() { return credits; }
        public int getCapacity() { return capacity; }
        public String getInstructor() { return instructor; }

        @Override
        public String toString() {
            return courseName + " (CRN: " + crn + ")";
        }
    }

    public static class CourseAssignmentInfo {
        private String teacherName;
        private String courseName;
        private int crn;

        public CourseAssignmentInfo(String teacherName, String courseName, int crn) {
            this.teacherName = teacherName;
            this.courseName = courseName;
            this.crn = crn;
        }

        // Getters
        public String getTeacherName() { return teacherName; }
        public String getCourseName() { return courseName; }
        public int getCrn() { return crn; }
    }
}