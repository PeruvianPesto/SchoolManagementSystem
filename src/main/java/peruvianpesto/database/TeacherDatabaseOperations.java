package peruvianpesto.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import peruvianpesto.ui.TeacherDashboard;

public class TeacherDatabaseOperations {

    // Get all courses taught by a specific teacher
    public static List<TeacherDashboard.CourseInfo> getTeacherCourses(int teacherId) {
        List<TeacherDashboard.CourseInfo> courses = new ArrayList<>();
        String sql = "SELECT c.crn, c.course_name, c.credits, c.course_size, c.num_students " +
                "FROM courses c " +
                "JOIN teacher_courses tc ON c.crn = tc.course_crn " +
                "WHERE tc.teacher_id = ? " +
                "ORDER BY tc.course_order";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new TeacherDashboard.CourseInfo(
                        rs.getInt("crn"),
                        rs.getString("course_name"),
                        rs.getFloat("credits"),
                        rs.getInt("course_size"),
                        rs.getInt("num_students")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting teacher courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    // Get all students in a specific course
    public static List<TeacherDashboard.StudentInfo> getCourseStudents(int courseCrn) {
        List<TeacherDashboard.StudentInfo> students = new ArrayList<>();
        String sql = "SELECT s.id, s.name, ccg.grade, ccg.attendance " +
                "FROM students s " +
                "JOIN course_enrollments ce ON s.id = ce.student_id " +
                "LEFT JOIN current_course_grades ccg ON s.id = ccg.student_id AND ce.course_crn = ccg.course_crn " +
                "WHERE ce.course_crn = ? " +
                "ORDER BY ce.enrollment_order";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseCrn);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(new TeacherDashboard.StudentInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("grade"),
                        rs.getDouble("attendance")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting course students: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }

    // Update student grade
    public static boolean updateStudentGrade(int studentId, int courseCrn, double grade) {
        String sql = "INSERT INTO current_course_grades (student_id, course_crn, grade, attendance) " +
                "VALUES (?, ?, ?, 0) " +
                "ON DUPLICATE KEY UPDATE grade = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseCrn);
            stmt.setDouble(3, grade);
            stmt.setDouble(4, grade);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student grade: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update student attendance
    public static boolean updateStudentAttendance(int studentId, int courseCrn, double attendance) {
        String sql = "INSERT INTO current_course_grades (student_id, course_crn, grade, attendance) " +
                "VALUES (?, ?, 0, ?) " +
                "ON DUPLICATE KEY UPDATE attendance = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseCrn);
            stmt.setDouble(3, attendance);
            stmt.setDouble(4, attendance);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Complete course for a student (move to completed courses)
    public static boolean completeCourseForStudent(int studentId, int courseCrn) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Get current grade and attendance
            String selectSql = "SELECT grade, attendance FROM current_course_grades " +
                    "WHERE student_id = ? AND course_crn = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, studentId);
            selectStmt.setInt(2, courseCrn);
            ResultSet rs = selectStmt.executeQuery();

            double grade = 0;
            double attendance = 0;
            if (rs.next()) {
                grade = rs.getDouble("grade");
                attendance = rs.getDouble("attendance");
            }

            // 2. Insert into completed courses
            String insertSql = "INSERT INTO completed_course_grades (student_id, course_crn, grade, attendance) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, studentId);
            insertStmt.setInt(2, courseCrn);
            insertStmt.setDouble(3, grade);
            insertStmt.setDouble(4, attendance);
            insertStmt.executeUpdate();

            // 3. Remove from current courses
            String deleteSql = "DELETE FROM current_course_grades WHERE student_id = ? AND course_crn = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, studentId);
            deleteStmt.setInt(2, courseCrn);
            deleteStmt.executeUpdate();

            // 4. Remove from enrollments
            String deleteEnrollSql = "DELETE FROM course_enrollments WHERE student_id = ? AND course_crn = ?";
            PreparedStatement deleteEnrollStmt = conn.prepareStatement(deleteEnrollSql);
            deleteEnrollStmt.setInt(1, studentId);
            deleteEnrollStmt.setInt(2, courseCrn);
            deleteEnrollStmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error completing course for student: " + e.getMessage());
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
}