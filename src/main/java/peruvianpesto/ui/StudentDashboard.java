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
import javafx.stage.Stage;
import peruvianpesto.database.DatabaseConnection;
import peruvianpesto.database.StudentDatabaseOperations;
import peruvianpesto.schoolmanagementsystem.MainApplication;

public class StudentDashboard {

    private Stage stage;
    private DatabaseConnection.UserInfo studentInfo;
    private TabPane tabPane;
    private Label statusLabel;

    // Course management components
    private TableView<CourseInfo> availableCoursesTable;
    private ObservableList<CourseInfo> availableCoursesData;

    private TableView<EnrolledCourseInfo> currentCoursesTable;
    private ObservableList<EnrolledCourseInfo> currentCoursesData;

    private TableView<CompletedCourseInfo> completedCoursesTable;
    private ObservableList<CompletedCourseInfo> completedCoursesData;

    public StudentDashboard(DatabaseConnection.UserInfo studentInfo) {
        this.studentInfo = studentInfo;
        createStudentWindow();
        loadAllData();
    }

    private void createStudentWindow() {
        stage = new Stage();
        stage.setTitle("Student Dashboard - " + studentInfo.getName());
        stage.setResizable(true);

        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        Label headerLabel = new Label("Student Dashboard");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerLabel.setStyle("-fx-text-fill: #333333;");

        Label welcomeLabel = new Label("Welcome, " + studentInfo.getName() + " (Student ID: " + studentInfo.getId() + ")");
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
        Tab availableCoursesTab = createAvailableCoursesTab();
        Tab currentCoursesTab = createCurrentCoursesTab();
        Tab completedCoursesTab = createCompletedCoursesTab();

        tabPane.getTabs().addAll(availableCoursesTab, currentCoursesTab, completedCoursesTab);

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> {
            stage.close();
            new MainApplication().start(new Stage());
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

        Scene scene = new Scene(mainContainer, 900, 600);
        stage.setScene(scene);
    }

    private Tab createAvailableCoursesTab() {
        Tab tab = new Tab("Available Courses");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Available courses table
        availableCoursesTable = new TableView<>();
        availableCoursesData = FXCollections.observableArrayList();

        TableColumn<CourseInfo, Integer> crnCol = new TableColumn<>("CRN");
        crnCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        crnCol.setPrefWidth(80);

        TableColumn<CourseInfo, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(200);

        TableColumn<CourseInfo, Float> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(80);

        TableColumn<CourseInfo, Integer> seatsCol = new TableColumn<>("Available Seats");
        seatsCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(
                        cellData.getValue().getClassSize() - cellData.getValue().getStudentsEnrolled()
                ).asObject());
        seatsCol.setPrefWidth(100);

        TableColumn<CourseInfo, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        instructorCol.setPrefWidth(150);

        availableCoursesTable.getColumns().addAll(crnCol, nameCol, creditsCol, seatsCol, instructorCol);
        availableCoursesTable.setItems(availableCoursesData);

        // Enroll button
        Button enrollBtn = new Button("Enroll in Selected Course");
        enrollBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        enrollBtn.setOnAction(e -> enrollInCourse());

        // Refresh button
        Button refreshBtn = new Button("Refresh Courses");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadAvailableCourses());

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().addAll(enrollBtn, refreshBtn);

        content.getChildren().addAll(
                new Label("Available Courses for Enrollment"),
                availableCoursesTable,
                buttonBox
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createCurrentCoursesTab() {
        Tab tab = new Tab("Current Courses");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Current courses table
        currentCoursesTable = new TableView<>();
        currentCoursesData = FXCollections.observableArrayList();

        TableColumn<EnrolledCourseInfo, Integer> crnCol = new TableColumn<>("CRN");
        crnCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        crnCol.setPrefWidth(80);

        TableColumn<EnrolledCourseInfo, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(200);

        TableColumn<EnrolledCourseInfo, Float> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(80);

        TableColumn<EnrolledCourseInfo, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        instructorCol.setPrefWidth(150);

        TableColumn<EnrolledCourseInfo, Double> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        gradeCol.setPrefWidth(100);
        gradeCol.setCellFactory(column -> new TableCell<EnrolledCourseInfo, Double>() {
            @Override
            protected void updateItem(Double grade, boolean empty) {
                super.updateItem(grade, empty);
                if (empty || grade == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", grade));
                    if (grade < 60) {
                        setStyle("-fx-text-fill: red;");
                    } else if (grade < 70) {
                        setStyle("-fx-text-fill: orange;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        TableColumn<EnrolledCourseInfo, Double> attendanceCol = new TableColumn<>("Attendance");
        attendanceCol.setCellValueFactory(new PropertyValueFactory<>("attendance"));
        attendanceCol.setPrefWidth(100);
        attendanceCol.setCellFactory(column -> new TableCell<EnrolledCourseInfo, Double>() {
            @Override
            protected void updateItem(Double attendance, boolean empty) {
                super.updateItem(attendance, empty);
                if (empty || attendance == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", attendance));
                    if (attendance < 70) {
                        setStyle("-fx-text-fill: red;");
                    } else if (attendance < 80) {
                        setStyle("-fx-text-fill: orange;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        currentCoursesTable.getColumns().addAll(crnCol, nameCol, creditsCol, instructorCol, gradeCol, attendanceCol);
        currentCoursesTable.setItems(currentCoursesData);

        // Drop button
        Button dropBtn = new Button("Drop Selected Course");
        dropBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        dropBtn.setOnAction(e -> dropCourse());

        // Refresh button
        Button refreshBtn = new Button("Refresh Courses");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadCurrentCourses());

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().addAll(dropBtn, refreshBtn);

        content.getChildren().addAll(
                new Label("Your Current Courses"),
                currentCoursesTable,
                buttonBox
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createCompletedCoursesTab() {
        Tab tab = new Tab("Completed Courses");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Completed courses table
        completedCoursesTable = new TableView<>();
        completedCoursesData = FXCollections.observableArrayList();

        TableColumn<CompletedCourseInfo, Integer> crnCol = new TableColumn<>("CRN");
        crnCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        crnCol.setPrefWidth(80);

        TableColumn<CompletedCourseInfo, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(200);

        TableColumn<CompletedCourseInfo, Float> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(80);

        TableColumn<CompletedCourseInfo, Double> gradeCol = new TableColumn<>("Final Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        gradeCol.setPrefWidth(100);
        gradeCol.setCellFactory(column -> new TableCell<CompletedCourseInfo, Double>() {
            @Override
            protected void updateItem(Double grade, boolean empty) {
                super.updateItem(grade, empty);
                if (empty || grade == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", grade));
                    if (grade < 60) {
                        setStyle("-fx-text-fill: red;");
                    } else if (grade < 70) {
                        setStyle("-fx-text-fill: orange;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        TableColumn<CompletedCourseInfo, Double> attendanceCol = new TableColumn<>("Attendance");
        attendanceCol.setCellValueFactory(new PropertyValueFactory<>("attendance"));
        attendanceCol.setPrefWidth(100);
        attendanceCol.setCellFactory(column -> new TableCell<CompletedCourseInfo, Double>() {
            @Override
            protected void updateItem(Double attendance, boolean empty) {
                super.updateItem(attendance, empty);
                if (empty || attendance == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", attendance));
                    if (attendance < 70) {
                        setStyle("-fx-text-fill: red;");
                    } else if (attendance < 80) {
                        setStyle("-fx-text-fill: orange;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        completedCoursesTable.getColumns().addAll(crnCol, nameCol, creditsCol, gradeCol, attendanceCol);
        completedCoursesTable.setItems(completedCoursesData);

        // Refresh button
        Button refreshBtn = new Button("Refresh Courses");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadCompletedCourses());

        content.getChildren().addAll(
                new Label("Your Completed Courses"),
                completedCoursesTable,
                refreshBtn
        );

        tab.setContent(content);
        return tab;
    }

    private void loadAllData() {
        loadAvailableCourses();
        loadCurrentCourses();
        loadCompletedCourses();
    }

    private void loadAvailableCourses() {
        try {
            availableCoursesData.clear();
            availableCoursesData.addAll(StudentDatabaseOperations.getAvailableCourses(studentInfo.getId()));
            statusLabel.setText("Available courses loaded successfully");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            statusLabel.setText("Error loading available courses: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void loadCurrentCourses() {
        try {
            currentCoursesData.clear();
            currentCoursesData.addAll(StudentDatabaseOperations.getCurrentCourses(studentInfo.getId()));
            statusLabel.setText("Current courses loaded successfully");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            statusLabel.setText("Error loading current courses: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void loadCompletedCourses() {
        try {
            completedCoursesData.clear();
            completedCoursesData.addAll(StudentDatabaseOperations.getCompletedCourses(studentInfo.getId()));
            statusLabel.setText("Completed courses loaded successfully");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            statusLabel.setText("Error loading completed courses: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void enrollInCourse() {
        CourseInfo selectedCourse = availableCoursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert("Please select a course to enroll in");
            return;
        }

        if (StudentDatabaseOperations.enrollInCourse(studentInfo.getId(), selectedCourse.getCrn())) {
            statusLabel.setText("Successfully enrolled in " + selectedCourse.getCourseName());
            statusLabel.setStyle("-fx-text-fill: green;");
            loadAllData(); // Refresh all tables
        } else {
            statusLabel.setText("Failed to enroll in course");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void dropCourse() {
        EnrolledCourseInfo selectedCourse = currentCoursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert("Please select a course to drop");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Drop");
        confirmation.setHeaderText("Drop Course");
        confirmation.setContentText("Are you sure you want to drop " + selectedCourse.getCourseName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (StudentDatabaseOperations.dropCourse(studentInfo.getId(), selectedCourse.getCrn())) {
                    statusLabel.setText("Successfully dropped " + selectedCourse.getCourseName());
                    statusLabel.setStyle("-fx-text-fill: green;");
                    loadAllData(); // Refresh all tables
                } else {
                    statusLabel.setText("Failed to drop course");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        stage.show();
    }

    // Data classes for table views
    public static class CourseInfo {
        private final int crn;
        private final String courseName;
        private final float credits;
        private final int classSize;
        private final int studentsEnrolled;
        private final String instructor;

        public CourseInfo(int crn, String courseName, float credits, int classSize, int studentsEnrolled, String instructor) {
            this.crn = crn;
            this.courseName = courseName;
            this.credits = credits;
            this.classSize = classSize;
            this.studentsEnrolled = studentsEnrolled;
            this.instructor = instructor;
        }

        public int getCrn() { return crn; }
        public String getCourseName() { return courseName; }
        public float getCredits() { return credits; }
        public int getClassSize() { return classSize; }
        public int getStudentsEnrolled() { return studentsEnrolled; }
        public String getInstructor() { return instructor; }

        @Override
        public String toString() {
            return courseName + " (CRN: " + crn + ")";
        }
    }

    public static class EnrolledCourseInfo {
        private final int crn;
        private final String courseName;
        private final float credits;
        private final String instructor;
        private final Double grade;
        private final Double attendance;

        public EnrolledCourseInfo(int crn, String courseName, float credits, String instructor, Double grade, Double attendance) {
            this.crn = crn;
            this.courseName = courseName;
            this.credits = credits;
            this.instructor = instructor;
            this.grade = grade;
            this.attendance = attendance;
        }

        public int getCrn() { return crn; }
        public String getCourseName() { return courseName; }
        public float getCredits() { return credits; }
        public String getInstructor() { return instructor; }
        public Double getGrade() { return grade; }
        public Double getAttendance() { return attendance; }
    }

    public static class CompletedCourseInfo {
        private final int crn;
        private final String courseName;
        private final float credits;
        private final Double grade;
        private final Double attendance;

        public CompletedCourseInfo(int crn, String courseName, float credits, Double grade, Double attendance) {
            this.crn = crn;
            this.courseName = courseName;
            this.credits = credits;
            this.grade = grade;
            this.attendance = attendance;
        }

        public int getCrn() { return crn; }
        public String getCourseName() { return courseName; }
        public float getCredits() { return credits; }
        public Double getGrade() { return grade; }
        public Double getAttendance() { return attendance; }
    }
}