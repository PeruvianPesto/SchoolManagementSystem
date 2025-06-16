package peruvianpesto.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import peruvianpesto.ui.StudentDashboard;

public class StudentDatabaseOperations {

    // Get all available courses (not enrolled by student)
    public static List<StudentDashboard.CourseInfo> getAvailableCourses(int studentId) {
        List<StudentDashboard.CourseInfo> courses = new ArrayList<>();
        String sql = "SELECT c.crn, c.course_name, c.credits, c.course_size, c.num_students, " +
                "COALESCE(t.name, 'Not Assigned') as instructor " +
                "FROM courses c " +
                "LEFT JOIN teacher_courses tc ON c.crn = tc.course_crn " +
                "LEFT JOIN teachers t ON tc.teacher_id = t.id " +
                "WHERE c.crn NOT IN (SELECT course_crn FROM course_enrollments WHERE student_id = ?) " +
                "AND c.num_students < c.course_size " +
                "ORDER BY c.course_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new StudentDashboard.CourseInfo(
                        rs.getInt("crn"),
                        rs.getString("course_name"),
                        rs.getFloat("credits"),
                        rs.getInt("course_size"),
                        rs.getInt("num_students"),
                        rs.getString("instructor")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting available courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    // Get current enrolled courses with grades
    public static List<StudentDashboard.EnrolledCourseInfo> getCurrentCourses(int studentId) {
        List<StudentDashboard.EnrolledCourseInfo> courses = new ArrayList<>();
        String sql = "SELECT c.crn, c.course_name, c.credits, ccg.grade, ccg.attendance, " +
                "COALESCE(t.name, 'Not Assigned') as instructor " +
                "FROM courses c " +
                "JOIN course_enrollments ce ON c.crn = ce.course_crn " +
                "LEFT JOIN current_course_grades ccg ON c.crn = ccg.course_crn AND ce.student_id = ccg.student_id " +
                "LEFT JOIN teacher_courses tc ON c.crn = tc.course_crn " +
                "LEFT JOIN teachers t ON tc.teacher_id = t.id " +
                "WHERE ce.student_id = ? " +
                "ORDER BY ce.enrollment_order";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new StudentDashboard.EnrolledCourseInfo(
                        rs.getInt("crn"),
                        rs.getString("course_name"),
                        rs.getFloat("credits"),
                        rs.getString("instructor"),
                        rs.getDouble("grade"),
                        rs.getDouble("attendance")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting current courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    // Get completed courses with final grades
    public static List<StudentDashboard.CompletedCourseInfo> getCompletedCourses(int studentId) {
        List<StudentDashboard.CompletedCourseInfo> courses = new ArrayList<>();
        String sql = "SELECT c.crn, c.course_name, c.credits, ccg.grade, ccg.attendance " +
                "FROM courses c " +
                "JOIN completed_course_grades ccg ON c.crn = ccg.course_crn " +
                "WHERE ccg.student_id = ? " +
                "ORDER BY c.course_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new StudentDashboard.CompletedCourseInfo(
                        rs.getInt("crn"),
                        rs.getString("course_name"),
                        rs.getFloat("credits"),
                        rs.getDouble("grade"),
                        rs.getDouble("attendance")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting completed courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    // Enroll in a new course
    public static boolean enrollInCourse(int studentId, int courseCrn) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // First get the max enrollment order in a separate query
            String maxOrderSql = "SELECT COALESCE(MAX(enrollment_order), 0) as max_order " +
                    "FROM course_enrollments WHERE student_id = ?";
            PreparedStatement maxOrderStmt = conn.prepareStatement(maxOrderSql);
            maxOrderStmt.setInt(1, studentId);
            ResultSet rs = maxOrderStmt.executeQuery();

            int nextOrder = 1;
            if (rs.next()) {
                nextOrder = rs.getInt("max_order") + 1;
            }

            // Now perform the insert
            String insertSql = "INSERT INTO course_enrollments (course_crn, student_id, enrollment_order) " +
                    "VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, courseCrn);
            insertStmt.setInt(2, studentId);
            insertStmt.setInt(3, nextOrder);

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows > 0) {
                // Update course student count
                updateCourseStudentCount(conn, courseCrn, 1);
                conn.commit();
                return true;
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error enrolling in course: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Drop a course
    public static boolean dropCourse(int studentId, int courseCrn) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Remove from enrollments
            String deleteSql = "DELETE FROM course_enrollments WHERE student_id = ? AND course_crn = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, studentId);
            deleteStmt.setInt(2, courseCrn);
            int affectedRows = deleteStmt.executeUpdate();

            if (affectedRows > 0) {
                // 2. Remove from current grades if exists
                String gradeSql = "DELETE FROM current_course_grades WHERE student_id = ? AND course_crn = ?";
                PreparedStatement gradeStmt = conn.prepareStatement(gradeSql);
                gradeStmt.setInt(1, studentId);
                gradeStmt.setInt(2, courseCrn);
                gradeStmt.executeUpdate();

                // 3. Update course student count using the same connection
                updateCourseStudentCount(conn, courseCrn, -1);

                conn.commit();
                return true;
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error dropping course: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Helper method to update course student count
    private static void updateCourseStudentCount(Connection conn, int courseCrn, int change) throws SQLException {
        String sql = "UPDATE courses SET num_students = num_students + ? WHERE crn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, change);
            stmt.setInt(2, courseCrn);
            stmt.executeUpdate();
        }
    }
}