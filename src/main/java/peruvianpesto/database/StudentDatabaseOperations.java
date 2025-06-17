/**
 * StudentDatabaseOperations.java
 *
 * A utility class that handles all database operations specific to student interactions
 * within the Peruvian Pesto School Management System.
 *
 * This class provides functionality for:
 * - Retrieving available courses for enrollment
 * - Getting current enrolled courses with grades and attendance
 * - Fetching completed courses with final grades
 * - Course enrollment with transaction management
 * - Course dropping with data cleanup
 * - Maintaining course enrollment counts
 *
 * Database Schema Dependencies:
 * - courses table: crn, course_name, credits, course_size, num_students
 * - course_enrollments table: course_crn, student_id, enrollment_order
 * - current_course_grades table: course_crn, student_id, grade, attendance
 * - completed_course_grades table: course_crn, student_id, grade, attendance
 * - teacher_courses table: course_crn, teacher_id
 * - teachers table: id, name
 *
 * @author Peruvian Pesto Development Team
 * @version 1.0
 * @since 2024
 */

package peruvianpesto.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import peruvianpesto.ui.StudentDashboard;

public class StudentDatabaseOperations {

    /**
     * Retrieves all available courses that a student can enroll in.
     * Excludes courses the student is already enrolled in and courses that are at capacity.
     * Includes instructor information through LEFT JOINs to handle unassigned courses.
     *
     * @param studentId The unique identifier of the student
     * @return List of CourseInfo objects representing available courses
     */
    public static List<StudentDashboard.CourseInfo> getAvailableCourses(int studentId) {
        List<StudentDashboard.CourseInfo> courses = new ArrayList<>();

        // Complex SQL query with multiple JOINs and subqueries
        String sql = "SELECT c.crn, c.course_name, c.credits, c.course_size, c.num_students, " +
                "COALESCE(t.name, 'Not Assigned') as instructor " +  // Handle courses without assigned teachers
                "FROM courses c " +
                "LEFT JOIN teacher_courses tc ON c.crn = tc.course_crn " +  // Get teacher assignment
                "LEFT JOIN teachers t ON tc.teacher_id = t.id " +            // Get teacher details
                "WHERE c.crn NOT IN (SELECT course_crn FROM course_enrollments WHERE student_id = ?) " +  // Exclude enrolled courses
                "AND c.num_students < c.course_size " +  // Only show courses with available spots
                "ORDER BY c.course_name";  // Sort alphabetically for better UX

        // Use try-with-resources for automatic resource management
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the student ID parameter to filter out already enrolled courses
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            // Process each row and create CourseInfo objects
            while (rs.next()) {
                courses.add(new StudentDashboard.CourseInfo(
                        rs.getInt("crn"),              // Course Reference Number
                        rs.getString("course_name"),    // Course title/name
                        rs.getFloat("credits"),         // Credit hours for the course
                        rs.getInt("course_size"),       // Maximum enrollment capacity
                        rs.getInt("num_students"),      // Current enrollment count
                        rs.getString("instructor")      // Assigned instructor or "Not Assigned"
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting available courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Retrieves all courses that a student is currently enrolled in.
     * Includes current grades, attendance records, and instructor information.
     * Orders results by enrollment order to maintain registration sequence.
     *
     * @param studentId The unique identifier of the student
     * @return List of EnrolledCourseInfo objects with grade and attendance data
     */
    public static List<StudentDashboard.EnrolledCourseInfo> getCurrentCourses(int studentId) {
        List<StudentDashboard.EnrolledCourseInfo> courses = new ArrayList<>();

        // SQL query joining multiple tables to get complete enrollment information
        String sql = "SELECT c.crn, c.course_name, c.credits, ccg.grade, ccg.attendance, " +
                "COALESCE(t.name, 'Not Assigned') as instructor " +
                "FROM courses c " +
                "JOIN course_enrollments ce ON c.crn = ce.course_crn " +  // Get enrollment records
                "LEFT JOIN current_course_grades ccg ON c.crn = ccg.course_crn AND ce.student_id = ccg.student_id " +  // Get current grades
                "LEFT JOIN teacher_courses tc ON c.crn = tc.course_crn " +  // Get teacher assignments
                "LEFT JOIN teachers t ON tc.teacher_id = t.id " +           // Get teacher details
                "WHERE ce.student_id = ? " +  // Filter for specific student
                "ORDER BY ce.enrollment_order";  // Maintain enrollment sequence

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set student ID parameter
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            // Build list of enrolled courses with academic progress data
            while (rs.next()) {
                courses.add(new StudentDashboard.EnrolledCourseInfo(
                        rs.getInt("crn"),              // Course Reference Number
                        rs.getString("course_name"),    // Course title
                        rs.getFloat("credits"),         // Credit hours
                        rs.getString("instructor"),     // Assigned instructor
                        rs.getDouble("grade"),          // Current grade (may be null)
                        rs.getDouble("attendance")      // Attendance percentage (may be null)
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting current courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Retrieves all courses that a student has completed with final grades.
     * Only includes courses from the completed_course_grades table,
     * representing finalized academic records.
     *
     * @param studentId The unique identifier of the student
     * @return List of CompletedCourseInfo objects with final grades and attendance
     */
    public static List<StudentDashboard.CompletedCourseInfo> getCompletedCourses(int studentId) {
        List<StudentDashboard.CompletedCourseInfo> courses = new ArrayList<>();

        // Simple query for completed courses with final grades
        String sql = "SELECT c.crn, c.course_name, c.credits, ccg.grade, ccg.attendance " +
                "FROM courses c " +
                "JOIN completed_course_grades ccg ON c.crn = ccg.course_crn " +  // Only completed courses
                "WHERE ccg.student_id = ? " +  // Filter for specific student
                "ORDER BY c.course_name";  // Alphabetical ordering for transcript readability

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set student ID parameter
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            // Build list of completed courses for transcript/history view
            while (rs.next()) {
                courses.add(new StudentDashboard.CompletedCourseInfo(
                        rs.getInt("crn"),              // Course Reference Number
                        rs.getString("course_name"),    // Course title
                        rs.getFloat("credits"),         // Credit hours earned
                        rs.getDouble("grade"),          // Final grade received
                        rs.getDouble("attendance")      // Final attendance percentage
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting completed courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Enrolls a student in a new course using database transactions for data integrity.
     * Maintains enrollment order and updates course student count atomically.
     *
     * The enrollment process involves:
     * 1. Determining the next enrollment order number for the student
     * 2. Inserting the enrollment record
     * 3. Updating the course's student count
     *
     * @param studentId The unique identifier of the student
     * @param courseCrn The Course Reference Number to enroll in
     * @return true if enrollment is successful, false otherwise
     */
    public static boolean enrollInCourse(int studentId, int courseCrn) {
        Connection conn = null;
        try {
            // Get database connection and start transaction
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction for atomicity

            // Step 1: Get the maximum enrollment order for this student
            // This maintains the sequence of course enrollments
            String maxOrderSql = "SELECT COALESCE(MAX(enrollment_order), 0) as max_order " +
                    "FROM course_enrollments WHERE student_id = ?";
            PreparedStatement maxOrderStmt = conn.prepareStatement(maxOrderSql);
            maxOrderStmt.setInt(1, studentId);
            ResultSet rs = maxOrderStmt.executeQuery();

            // Calculate next enrollment order (starts at 1 for first course)
            int nextOrder = 1;
            if (rs.next()) {
                nextOrder = rs.getInt("max_order") + 1;
            }

            // Step 2: Insert the enrollment record with proper ordering
            String insertSql = "INSERT INTO course_enrollments (course_crn, student_id, enrollment_order) " +
                    "VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, courseCrn);     // Course to enroll in
            insertStmt.setInt(2, studentId);     // Student enrolling
            insertStmt.setInt(3, nextOrder);     // Maintain enrollment sequence

            // Execute enrollment insertion
            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows > 0) {
                // Step 3: Update course student count (+1 for new enrollment)
                updateCourseStudentCount(conn, courseCrn, 1);

                // Commit transaction - all operations successful
                conn.commit();
                return true;
            }

            // Rollback if enrollment insertion failed
            conn.rollback();
            return false;

        } catch (SQLException e) {
            // Rollback transaction on any error
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
            // Reset connection state and close
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // Reset to default auto-commit mode
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Removes a student from a course and cleans up associated data.
     * Uses database transactions to ensure data consistency across multiple tables.
     *
     * The drop process involves:
     * 1. Removing the enrollment record
     * 2. Cleaning up current grade records if they exist
     * 3. Updating the course's student count
     *
     * @param studentId The unique identifier of the student
     * @param courseCrn The Course Reference Number to drop
     * @return true if drop is successful, false otherwise
     */
    public static boolean dropCourse(int studentId, int courseCrn) {
        Connection conn = null;
        try {
            // Get database connection and start transaction
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction for data consistency

            // Step 1: Remove the enrollment record
            String deleteSql = "DELETE FROM course_enrollments WHERE student_id = ? AND course_crn = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, studentId);
            deleteStmt.setInt(2, courseCrn);
            int affectedRows = deleteStmt.executeUpdate();

            if (affectedRows > 0) {
                // Step 2: Clean up current grades if they exist
                // This prevents orphaned grade records
                String gradeSql = "DELETE FROM current_course_grades WHERE student_id = ? AND course_crn = ?";
                PreparedStatement gradeStmt = conn.prepareStatement(gradeSql);
                gradeStmt.setInt(1, studentId);
                gradeStmt.setInt(2, courseCrn);
                gradeStmt.executeUpdate();  // Execute cleanup (may affect 0 rows if no grades exist)

                // Step 3: Update course student count (-1 for dropped student)
                updateCourseStudentCount(conn, courseCrn, -1);

                // Commit transaction - all cleanup operations successful
                conn.commit();
                return true;
            }

            // Rollback if no enrollment was found to delete
            conn.rollback();
            return false;

        } catch (SQLException e) {
            // Rollback transaction on any error
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
            // Reset connection state and close
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // Reset to default auto-commit mode
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Helper method to update the student count for a specific course.
     * Used internally by enrollment and drop operations to maintain accurate counts.
     *
     * This method must be called within an existing transaction context
     * to ensure consistency with enrollment/drop operations.
     *
     * @param conn Database connection (must be in transaction mode)
     * @param courseCrn The Course Reference Number to update
     * @param change The change in student count (+1 for enrollment, -1 for drop)
     * @throws SQLException if the update operation fails
     */
    private static void updateCourseStudentCount(Connection conn, int courseCrn, int change) throws SQLException {
        // SQL to increment/decrement the student count atomically
        String sql = "UPDATE courses SET num_students = num_students + ? WHERE crn = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, change);     // Positive for enrollment, negative for drop
            stmt.setInt(2, courseCrn);  // Target course to update
            stmt.executeUpdate();       // Execute the count update
        }
        // Note: This method throws SQLException to be handled by calling methods
        // within their transaction context
    }
}