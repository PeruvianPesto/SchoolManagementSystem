package peruvianpesto.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import peruvianpesto.ui.AdminDashboard;

public class AdminDatabaseOperations {

    public static List<AdminDashboard.StudentInfo> getAllStudents() {
        List<AdminDashboard.StudentInfo> students = new ArrayList<>();
        String sql = "SELECT u.id, u.username, s.name, " +
                "COALESCE(enrolled_count.count, 0) as enrolled_courses " +
                "FROM users u " +
                "JOIN students s ON u.id = s.id " +
                "LEFT JOIN (SELECT student_id, COUNT(*) as count FROM course_enrollments GROUP BY student_id) enrolled_count " +
                "ON u.id = enrolled_count.student_id " +
                "WHERE u.user_type = 'student' " +
                "ORDER BY s.name";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(new AdminDashboard.StudentInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getInt("enrolled_courses")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
            throw new RuntimeException("Failed to load students", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }

        return students;
    }

    // Get all teachers
    public static List<AdminDashboard.TeacherInfo> getAllTeachers() {
        List<AdminDashboard.TeacherInfo> teachers = new ArrayList<>();
        String sql = "SELECT u.id, u.username, t.name, " +
                "COALESCE(teaching_count.count, 0) as courses_teaching " +
                "FROM users u " +
                "JOIN teachers t ON u.id = t.id " +
                "LEFT JOIN (SELECT teacher_id, COUNT(*) as count FROM teacher_courses GROUP BY teacher_id) teaching_count " +
                "ON u.id = teaching_count.teacher_id " +
                "WHERE u.user_type = 'teacher' " +
                "ORDER BY t.name";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                teachers.add(new AdminDashboard.TeacherInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getInt("courses_teaching")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error loading teachers: " + e.getMessage());
            e.printStackTrace();
        }

        return teachers;
    }

    // Get all courses
    public static List<AdminDashboard.CourseInfo> getAllCourses() {
        List<AdminDashboard.CourseInfo> courses = new ArrayList<>();
        String sql = "SELECT c.crn, c.course_name, c.credits, c.course_size as capacity, " +
                "COALESCE(t.name, 'Not Assigned') as instructor_name " +
                "FROM courses c " +
                "LEFT JOIN teacher_courses tc ON c.crn = tc.course_crn " +
                "LEFT JOIN teachers t ON tc.teacher_id = t.id " +
                "ORDER BY c.course_name";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new AdminDashboard.CourseInfo(
                        rs.getInt("crn"),
                        rs.getString("course_name"),
                        rs.getFloat("credits"),
                        rs.getInt("capacity"),
                        rs.getString("instructor_name")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error loading courses: " + e.getMessage());
            e.printStackTrace();
        }

        return courses;
    }

    // Add a new course
    public static boolean addCourse(String courseName, int crn, float credits, int capacity) {
        String sql = "INSERT INTO courses (crn, course_name, credits, course_size, num_students) VALUES (?, ?, ?, ?, 0)";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, crn);
            stmt.setString(2, courseName);
            stmt.setFloat(3, credits);
            stmt.setInt(4, capacity);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Course added successfully: " + courseName);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error adding course: " + e.getMessage());
            if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate entry")) {
                System.out.println("Course with CRN " + crn + " already exists");
            }
            e.printStackTrace();
        }

        return false;
    }

    // Remove a course
    public static boolean removeCourse(int crn) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Remove from course_enrollments first
            String removeEnrollmentsSql = "DELETE FROM course_enrollments WHERE course_crn = ?";
            PreparedStatement enrollStmt = conn.prepareStatement(removeEnrollmentsSql);
            enrollStmt.setInt(1, crn);
            enrollStmt.executeUpdate();

            // Remove from teacher_courses
            String removeTeacherAssignmentsSql = "DELETE FROM teacher_courses WHERE course_crn = ?";
            PreparedStatement teacherStmt = conn.prepareStatement(removeTeacherAssignmentsSql);
            teacherStmt.setInt(1, crn);
            teacherStmt.executeUpdate();

            // Remove from current_course_grades
            String removeGradesSql = "DELETE FROM current_course_grades WHERE course_crn = ?";
            PreparedStatement gradesStmt = conn.prepareStatement(removeGradesSql);
            gradesStmt.setInt(1, crn);
            gradesStmt.executeUpdate();

            // Remove from completed_course_grades
            String removeCompletedGradesSql = "DELETE FROM completed_course_grades WHERE course_crn = ?";
            PreparedStatement completedGradesStmt = conn.prepareStatement(removeCompletedGradesSql);
            completedGradesStmt.setInt(1, crn);
            completedGradesStmt.executeUpdate();

            // Finally, remove the course
            String sql = "DELETE FROM courses WHERE crn = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, crn);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                conn.commit();
                System.out.println("Course removed successfully (CRN: " + crn + ")");
                return true;
            } else {
                conn.rollback();
                System.out.println("No course found with CRN: " + crn);
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error removing course: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    // Remove a user (student or teacher)
    public static boolean removeUser(int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // First, determine the user type
            String userTypeQuery = "SELECT user_type FROM users WHERE id = ?";
            PreparedStatement userTypeStmt = conn.prepareStatement(userTypeQuery);
            userTypeStmt.setInt(1, userId);
            ResultSet rs = userTypeStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("User not found with ID: " + userId);
                conn.rollback();
                return false;
            }

            String userType = rs.getString("user_type");

            // Remove from specific table first (students or teachers)
            if ("student".equals(userType)) {
                // Remove student enrollments first
                String removeEnrollmentsSql = "DELETE FROM course_enrollments WHERE student_id = ?";
                PreparedStatement enrollStmt = conn.prepareStatement(removeEnrollmentsSql);
                enrollStmt.setInt(1, userId);
                enrollStmt.executeUpdate();

                // Remove from current grades
                String removeCurrentGradesSql = "DELETE FROM current_course_grades WHERE student_id = ?";
                PreparedStatement currentGradesStmt = conn.prepareStatement(removeCurrentGradesSql);
                currentGradesStmt.setInt(1, userId);
                currentGradesStmt.executeUpdate();

                // Remove from completed grades
                String removeCompletedGradesSql = "DELETE FROM completed_course_grades WHERE student_id = ?";
                PreparedStatement completedGradesStmt = conn.prepareStatement(removeCompletedGradesSql);
                completedGradesStmt.setInt(1, userId);
                completedGradesStmt.executeUpdate();

                // Remove from students table
                String removeStudentSql = "DELETE FROM students WHERE id = ?";
                PreparedStatement studentStmt = conn.prepareStatement(removeStudentSql);
                studentStmt.setInt(1, userId);
                studentStmt.executeUpdate();

            } else if ("teacher".equals(userType)) {
                // Remove teacher course assignments
                String removeTeacherCoursesSql = "DELETE FROM teacher_courses WHERE teacher_id = ?";
                PreparedStatement teacherCoursesStmt = conn.prepareStatement(removeTeacherCoursesSql);
                teacherCoursesStmt.setInt(1, userId);
                teacherCoursesStmt.executeUpdate();

                // Remove from teachers table
                String removeTeacherSql = "DELETE FROM teachers WHERE id = ?";
                PreparedStatement teacherStmt = conn.prepareStatement(removeTeacherSql);
                teacherStmt.setInt(1, userId);
                teacherStmt.executeUpdate();

            } else {
                System.out.println("Cannot remove admin users");
                conn.rollback();
                return false;
            }

            // Finally, remove from users table
            String userSql = "DELETE FROM users WHERE id = ?";
            PreparedStatement userStmt = conn.prepareStatement(userSql);
            userStmt.setInt(1, userId);
            int rowsAffected = userStmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                System.out.println("User removed successfully (ID: " + userId + ")");
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error removing user: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    // Assign a course to a teacher
    public static boolean assignCourseToTeacher(int teacherId, int crn) {
        String sql = "INSERT INTO teacher_courses (teacher_id, course_crn, course_order) VALUES (?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getConnection();

            // First check if assignment already exists
            String checkSql = "SELECT COUNT(*) FROM teacher_courses WHERE teacher_id = ? AND course_crn = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, teacherId);
            checkStmt.setInt(2, crn);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Course already assigned to this teacher");
                return false;
            }

            // Get the next course order for this teacher
            String orderSql = "SELECT COALESCE(MAX(course_order), 0) + 1 FROM teacher_courses WHERE teacher_id = ?";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setInt(1, teacherId);
            ResultSet orderRs = orderStmt.executeQuery();
            int courseOrder = 1;
            if (orderRs.next()) {
                courseOrder = orderRs.getInt(1);
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            stmt.setInt(2, crn);
            stmt.setInt(3, courseOrder);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Course assigned successfully (Teacher ID: " + teacherId + ", CRN: " + crn + ")");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error assigning course: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Unassign a course from a teacher
    public static boolean unassignCourseFromTeacher(int teacherId, int crn) {
        String sql = "DELETE FROM teacher_courses WHERE teacher_id = ? AND course_crn = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            stmt.setInt(2, crn);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Course unassigned successfully (Teacher ID: " + teacherId + ", CRN: " + crn + ")");
                return true;
            } else {
                System.out.println("No matching assignment found (Teacher ID: " + teacherId + ", CRN: " + crn + ")");
            }
        } catch (SQLException e) {
            System.out.println("Error unassigning course: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Get all course assignments
    public static List<AdminDashboard.CourseAssignmentInfo> getAllCourseAssignments() {
        List<AdminDashboard.CourseAssignmentInfo> assignments = new ArrayList<>();
        String sql = "SELECT t.name as teacher_name, c.course_name, c.crn " +
                "FROM teacher_courses tc " +
                "JOIN teachers t ON tc.teacher_id = t.id " +
                "JOIN courses c ON tc.course_crn = c.crn " +
                "ORDER BY t.name, tc.course_order";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                assignments.add(new AdminDashboard.CourseAssignmentInfo(
                        rs.getString("teacher_name"),
                        rs.getString("course_name"),
                        rs.getInt("crn")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error loading course assignments: " + e.getMessage());
            e.printStackTrace();
        }

        return assignments;
    }

    // Add a new student
    public static boolean addStudent(String username, String password, String name, float maxUnits) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert into users table
            String userSql = "INSERT INTO users (username, password_hash, user_type) VALUES (?, ?, 'student')";
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, password); // In production, this should be hashed
            userStmt.executeUpdate();

            // Get the generated user ID
            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                conn.rollback();
                return false;
            }
            int userId = generatedKeys.getInt(1);

            // Insert into students table
            String studentSql = "INSERT INTO students (id, name, max_units) VALUES (?, ?, ?)";
            PreparedStatement studentStmt = conn.prepareStatement(studentSql);
            studentStmt.setInt(1, userId);
            studentStmt.setString(2, name);
            studentStmt.setFloat(3, maxUnits);
            studentStmt.executeUpdate();

            conn.commit();
            System.out.println("Student added successfully: " + name);
            return true;

        } catch (SQLException e) {
            System.out.println("Error adding student: " + e.getMessage());
            if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate entry")) {
                System.out.println("Username '" + username + "' already exists");
            }
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    // Add a new teacher
    public static boolean addTeacher(String username, String password, String name, int maxCourses, int coursesTaught) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert into users table
            String userSql = "INSERT INTO users (username, password_hash, user_type) VALUES (?, ?, 'teacher')";
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, password); // In production, this should be hashed
            userStmt.executeUpdate();

            // Get the generated user ID
            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                conn.rollback();
                return false;
            }
            int userId = generatedKeys.getInt(1);

            // Insert into teachers table
            String teacherSql = "INSERT INTO teachers (id, name, max_courses, courses_taught) VALUES (?, ?, ?, ?)";
            PreparedStatement teacherStmt = conn.prepareStatement(teacherSql);
            teacherStmt.setInt(1, userId);
            teacherStmt.setString(2, name);
            teacherStmt.setInt(3, maxCourses);
            teacherStmt.setInt(4, coursesTaught);
            teacherStmt.executeUpdate();

            conn.commit();
            System.out.println("Teacher added successfully: " + name);
            return true;

        } catch (SQLException e) {
            System.out.println("Error adding teacher: " + e.getMessage());
            if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate entry")) {
                System.out.println("Username '" + username + "' already exists");
            }
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    // Overloaded methods for backward compatibility (using default values)
    public static boolean addStudent(String username, String password, String name) {
        return addStudent(username, password, name, 18.0f); // Default 18 units
    }

    public static boolean addTeacher(String username, String password, String name) {
        return addTeacher(username, password, name, 5, 0); // Default 5 max courses, 0 currently taught
    }
}