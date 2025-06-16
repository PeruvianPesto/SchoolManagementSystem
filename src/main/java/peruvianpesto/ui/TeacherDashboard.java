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
import peruvianpesto.database.TeacherDatabaseOperations;
import peruvianpesto.schoolmanagementsystem.MainApplication;

public class TeacherDashboard {

    private Stage stage;
    private DatabaseConnection.UserInfo teacherInfo;
    private TabPane tabPane;
    private Label statusLabel;

    // Course management components
    private TableView<CourseInfo> courseTable;
    private ObservableList<CourseInfo> courseData;

    // Student management components
    private TableView<StudentInfo> studentTable;
    private ObservableList<StudentInfo> studentData;
    private ComboBox<CourseInfo> courseCombo;

    public TeacherDashboard(DatabaseConnection.UserInfo teacherInfo) {
        this.teacherInfo = teacherInfo;
        createTeacherWindow();
        loadCourseData();
    }

    private void createTeacherWindow() {
        stage = new Stage();
        stage.setTitle("Teacher Dashboard - " + teacherInfo.getName());
        stage.setResizable(true);

        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        Label headerLabel = new Label("Teacher Dashboard");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerLabel.setStyle("-fx-text-fill: #333333;");

        Label welcomeLabel = new Label("Welcome, " + teacherInfo.getName() + " (Teacher ID: " + teacherInfo.getId() + ")");
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
        Tab coursesTab = createCoursesTab();
        Tab studentsTab = createStudentsTab();

        tabPane.getTabs().addAll(coursesTab, studentsTab);

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

        Scene scene = new Scene(mainContainer, 800, 600);
        stage.setScene(scene);
    }

    private Tab createCoursesTab() {
        Tab tab = new Tab("My Courses");

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

        TableColumn<CourseInfo, Integer> sizeCol = new TableColumn<>("Class Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("classSize"));
        sizeCol.setPrefWidth(80);

        TableColumn<CourseInfo, Integer> studentsCol = new TableColumn<>("Students Enrolled");
        studentsCol.setCellValueFactory(new PropertyValueFactory<>("studentsEnrolled"));
        studentsCol.setPrefWidth(120);

        courseTable.getColumns().addAll(crnCol, nameCol, creditsCol, sizeCol, studentsCol);
        courseTable.setItems(courseData);

        // Refresh button
        Button refreshBtn = new Button("Refresh Courses");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadCourseData());

        content.getChildren().addAll(
                new Label("My Teaching Assignments"),
                courseTable,
                refreshBtn
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createStudentsTab() {
        Tab tab = new Tab("Student Management");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Course selection
        HBox courseSelectionBox = new HBox(10);
        courseSelectionBox.setAlignment(Pos.CENTER_LEFT);
        Label courseLabel = new Label("Select Course:");
        courseLabel.setPrefWidth(100);
        courseCombo = new ComboBox<>();
        courseCombo.setPrefWidth(300);
        courseCombo.setPromptText("Choose a course");
        courseCombo.setOnAction(e -> loadStudentData());
        courseSelectionBox.getChildren().addAll(courseLabel, courseCombo);

        // Student table
        studentTable = new TableView<>();
        studentData = FXCollections.observableArrayList();

        TableColumn<StudentInfo, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<StudentInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<StudentInfo, Double> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        gradeCol.setPrefWidth(100);
        gradeCol.setCellFactory(column -> new TableCell<StudentInfo, Double>() {
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

        TableColumn<StudentInfo, Double> attendanceCol = new TableColumn<>("Attendance");
        attendanceCol.setCellValueFactory(new PropertyValueFactory<>("attendance"));
        attendanceCol.setPrefWidth(100);
        attendanceCol.setCellFactory(column -> new TableCell<StudentInfo, Double>() {
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

        studentTable.getColumns().addAll(idCol, nameCol, gradeCol, attendanceCol);
        studentTable.setItems(studentData);

        // Grade/attendance controls
        HBox controlsBox = new HBox(15);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        Label gradeLabel = new Label("Grade:");
        TextField gradeField = new TextField();
        gradeField.setPromptText("0-100");
        gradeField.setPrefWidth(80);

        Label attendanceLabel = new Label("Attendance:");
        TextField attendanceField = new TextField();
        attendanceField.setPromptText("0-100%");
        attendanceField.setPrefWidth(80);

        Button updateGradeBtn = new Button("Update Grade");
        updateGradeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        updateGradeBtn.setOnAction(e -> updateGrade(gradeField));

        Button updateAttendanceBtn = new Button("Update Attendance");
        updateAttendanceBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateAttendanceBtn.setOnAction(e -> updateAttendance(attendanceField));

        Button completeCourseBtn = new Button("Complete Course");
        completeCourseBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        completeCourseBtn.setOnAction(e -> completeCourse());

        controlsBox.getChildren().addAll(
                gradeLabel, gradeField, updateGradeBtn,
                new Label("   "),
                attendanceLabel, attendanceField, updateAttendanceBtn,
                new Label("   "),
                completeCourseBtn
        );

        // Refresh button
        Button refreshBtn = new Button("Refresh Students");
        refreshBtn.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadStudentData());

        content.getChildren().addAll(
                new Label("Student Management"),
                courseSelectionBox,
                studentTable,
                controlsBox,
                refreshBtn
        );

        tab.setContent(content);
        return tab;
    }

    private void loadCourseData() {
        try {
            courseData.clear();
            courseData.addAll(TeacherDatabaseOperations.getTeacherCourses(teacherInfo.getId()));

            // Also update course combo in students tab
            courseCombo.getItems().clear();
            courseCombo.getItems().addAll(courseData);

            statusLabel.setText("Courses loaded successfully");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            statusLabel.setText("Error loading courses: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void loadStudentData() {
        CourseInfo selectedCourse = courseCombo.getValue();
        if (selectedCourse == null) {
            statusLabel.setText("Please select a course first");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }

        try {
            studentData.clear();
            studentData.addAll(TeacherDatabaseOperations.getCourseStudents(selectedCourse.getCrn()));
            statusLabel.setText("Students loaded successfully for " + selectedCourse.getCourseName());
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            statusLabel.setText("Error loading students: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void updateGrade(TextField gradeField) {
        StudentInfo selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        CourseInfo selectedCourse = courseCombo.getValue();

        if (selectedStudent == null || selectedCourse == null) {
            showAlert("Please select both a course and a student");
            return;
        }

        try {
            double grade = Double.parseDouble(gradeField.getText());
            if (grade < 0 || grade > 100) {
                showAlert("Grade must be between 0 and 100");
                return;
            }

            if (TeacherDatabaseOperations.updateStudentGrade(
                    selectedStudent.getId(),
                    selectedCourse.getCrn(),
                    grade)) {
                statusLabel.setText("Grade updated successfully");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadStudentData(); // Refresh the table
                gradeField.clear();
            } else {
                statusLabel.setText("Failed to update grade");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number for grade");
        }
    }

    private void updateAttendance(TextField attendanceField) {
        StudentInfo selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        CourseInfo selectedCourse = courseCombo.getValue();

        if (selectedStudent == null || selectedCourse == null) {
            showAlert("Please select both a course and a student");
            return;
        }

        try {
            double attendance = Double.parseDouble(attendanceField.getText());
            if (attendance < 0 || attendance > 100) {
                showAlert("Attendance must be between 0 and 100");
                return;
            }

            if (TeacherDatabaseOperations.updateStudentAttendance(
                    selectedStudent.getId(),
                    selectedCourse.getCrn(),
                    attendance)) {
                statusLabel.setText("Attendance updated successfully");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadStudentData(); // Refresh the table
                attendanceField.clear();
            } else {
                statusLabel.setText("Failed to update attendance");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number for attendance");
        }
    }

    private void completeCourse() {
        StudentInfo selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        CourseInfo selectedCourse = courseCombo.getValue();

        if (selectedStudent == null || selectedCourse == null) {
            showAlert("Please select both a course and a student");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Completion");
        confirmation.setHeaderText("Complete Course for Student");
        confirmation.setContentText("Are you sure you want to mark this course as completed for " +
                selectedStudent.getName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (TeacherDatabaseOperations.completeCourseForStudent(
                        selectedStudent.getId(),
                        selectedCourse.getCrn())) {
                    statusLabel.setText("Course marked as completed for " + selectedStudent.getName());
                    statusLabel.setStyle("-fx-text-fill: green;");
                    loadStudentData(); // Refresh the table
                } else {
                    statusLabel.setText("Failed to complete course");
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

        public CourseInfo(int crn, String courseName, float credits, int classSize, int studentsEnrolled) {
            this.crn = crn;
            this.courseName = courseName;
            this.credits = credits;
            this.classSize = classSize;
            this.studentsEnrolled = studentsEnrolled;
        }

        // Getters
        public int getCrn() { return crn; }
        public String getCourseName() { return courseName; }
        public float getCredits() { return credits; }
        public int getClassSize() { return classSize; }
        public int getStudentsEnrolled() { return studentsEnrolled; }

        @Override
        public String toString() {
            return String.format("%s (CRN: %d, Students: %d/%d)",
                    courseName, crn, studentsEnrolled, classSize);
        }
    }

    public static class StudentInfo {
        private final int id;
        private final String name;
        private final double grade;
        private final double attendance;

        public StudentInfo(int id, String name, double grade, double attendance) {
            this.id = id;
            this.name = name;
            this.grade = grade;
            this.attendance = attendance;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getGrade() { return grade; }
        public double getAttendance() { return attendance; }
    }
}