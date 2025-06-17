/**
 * TeacherDatabaseOperations.java
 *
 * This class provides database operations specifically for teacher-related functionality
 * in the Peruvian Pesto educational management system. It handles CRUD operations
 * for managing courses, students, grades, and attendance from a teacher's perspective.
 *
 * Key functionalities:
 * - Retrieve courses assigned to a specific teacher
 * - Get student lists for courses
 * - Update student grades and attendance
 * - Complete courses for students (move from current to completed)
 *
 * Database Tables Used:
 * - courses: Contains course information (CRN, name, credits, size)
 * - teacher_courses: Maps teachers to their assigned courses
 * - students: Student information
 * - course_enrollments: Current student enrollments
 * - current_course_grades: Ongoing grades and attendance
 * - completed_course_grades: Historical completed course data
 *
 * @author Peruvian Pesto Team
 * @version 1.0
 */

package peruvianpesto.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import peruvianpesto.ui.TeacherDashboard;

public class TeacherDatabaseOperations {

    /**
     * Retrieves all courses taught by a specific teacher.
     *
     * This method joins the courses and teacher_courses tables to get
     * comprehensive course information for a given teacher ID.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of CourseInfo objects containing course details
     */
    public static List<TeacherDashboard.CourseInfo> getTeacherCourses(int teacherId) {
        List<TeacherDashboard.CourseInfo> courses = new ArrayList<>();

        // SQL query to get course information for a specific teacher
        String sql = "SELECT c.crn, c.course_name, c.credits, c.course_size, c.num_students " +
                "FROM courses c " +
                "JOIN teacher_courses tc ON c.crn = tc.course_crn " +
                "WHERE tc.teacher_id = ? " +
                "ORDER BY tc.course_order";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the teacher ID parameter
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            // Process each course record
            while (rs.next()) {
                courses.add(new TeacherDashboard.CourseInfo(
                        rs.getInt("crn"),           // Course Reference Number
                        rs.getString("course_name"), // Course name
                        rs.getFloat("credits"),      // Credit hours
                        rs.getInt("course_size"),    // Maximum enrollment
                        rs.getInt("num_students")    // Current enrollment
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting teacher courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Retrieves all students enrolled in a specific course.
     *
     * This method performs a join between students, course_enrollments, and
     * current_course_grades to get student information along with their
     * current grades and attendance for the specified course.
     *
     * @param courseCrn The Course Reference Number
     * @return List of StudentInfo objects containing student details
     */
    public static List<TeacherDashboard.StudentInfo> getCourseStudents(int courseCrn) {
        List<TeacherDashboard.StudentInfo> students = new ArrayList<>();

        // SQL query with LEFT JOIN to include students even if they don't have grades yet
        String sql = "SELECT s.id, s.name, ccg.grade, ccg.attendance " +
                "FROM students s " +
                "JOIN course_enrollments ce ON s.id = ce.student_id " +
                "LEFT JOIN current_course_grades ccg ON s.id = ccg.student_id AND ce.course_crn = ccg.course_crn " +
                "WHERE ce.course_crn = ? " +
                "ORDER BY ce.enrollment_order";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the course CRN parameter
            stmt.setInt(1, courseCrn);
            ResultSet rs = stmt.executeQuery();

            // Process each student record
            while (rs.next()) {
                students.add(new TeacherDashboard.StudentInfo(
                        rs.getInt("id"),           // Student ID
                        rs.getString("name"),      // Student name
                        rs.getDouble("grade"),     // Current grade (may be null)
                        rs.getDouble("attendance") // Attendance percentage
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting course students: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }

    /**
     * Updates a student's grade for a specific course.
     *
     * Uses INSERT ... ON DUPLICATE KEY UPDATE to either create a new grade record
     * or update an existing one. If no record exists, it creates one with
     * attendance set to 0. If a record exists, it only updates the grade.
     *
     * @param studentId The student's unique identifier
     * @param courseCrn The course reference number
     * @param grade The new grade value
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateStudentGrade(int studentId, int courseCrn, double grade) {
        // Use MySQL's ON DUPLICATE KEY UPDATE for upsert operation
        String sql = "INSERT INTO current_course_grades (student_id, course_crn, grade, attendance) " +
                "VALUES (?, ?, ?, 0) " +
                "ON DUPLICATE KEY UPDATE grade = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for both INSERT and UPDATE cases
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseCrn);
            stmt.setDouble(3, grade);      // For INSERT
            stmt.setDouble(4, grade);      // For UPDATE

            // Return true if at least one row was affected
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student grade: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates a student's attendance for a specific course.
     *
     * Similar to updateStudentGrade, this uses INSERT ... ON DUPLICATE KEY UPDATE
     * to either create a new attendance record or update an existing one.
     * If no record exists, it creates one with grade set to 0.
     *
     * @param studentId The student's unique identifier
     * @param courseCrn The course reference number
     * @param attendance The new attendance percentage
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateStudentAttendance(int studentId, int courseCrn, double attendance) {
        // Use MySQL's ON DUPLICATE KEY UPDATE for upsert operation
        String sql = "INSERT INTO current_course_grades (student_id, course_crn, grade, attendance) " +
                "VALUES (?, ?, 0, ?) " +
                "ON DUPLICATE KEY UPDATE attendance = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for both INSERT and UPDATE cases
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseCrn);
            stmt.setDouble(3, attendance); // For INSERT
            stmt.setDouble(4, attendance); // For UPDATE

            // Return true if at least one row was affected
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Completes a course for a student by moving their records from current to completed status.
     *
     * This method performs a multi-step transaction:
     * 1. Retrieves the student's current grade and attendance
     * 2. Inserts the data into completed_course_grades table
     * 3. Removes the record from current_course_grades table
     * 4. Removes the enrollment record from course_enrollments table
     *
     * All operations are wrapped in a transaction to ensure data consistency.
     * If any step fails, the entire transaction is rolled back.
     *
     * @param studentId The student's unique identifier
     * @param courseCrn The course reference number
     * @return true if the course completion was successful, false otherwise
     */
    public static boolean completeCourseForStudent(int studentId, int courseCrn) {
        Connection conn = null;
        try {
            // Get connection and start transaction
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Step 1: Get current grade and attendance data
            String selectSql = "SELECT grade, attendance FROM current_course_grades " +
                    "WHERE student_id = ? AND course_crn = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, studentId);
            selectStmt.setInt(2, courseCrn);
            ResultSet rs = selectStmt.executeQuery();

            // Initialize default values in case no current grades exist
            double grade = 0;
            double attendance = 0;
            if (rs.next()) {
                grade = rs.getDouble("grade");
                attendance = rs.getDouble("attendance");
            }

            // Step 2: Insert into completed courses table
            String insertSql = "INSERT INTO completed_course_grades (student_id, course_crn, grade, attendance) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, studentId);
            insertStmt.setInt(2, courseCrn);
            insertStmt.setDouble(3, grade);
            insertStmt.setDouble(4, attendance);
            insertStmt.executeUpdate();

            // Step 3: Remove from current course grades table
            String deleteSql = "DELETE FROM current_course_grades WHERE student_id = ? AND course_crn = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, studentId);
            deleteStmt.setInt(2, courseCrn);
            deleteStmt.executeUpdate();

            // Step 4: Remove from course enrollments table
            String deleteEnrollSql = "DELETE FROM course_enrollments WHERE student_id = ? AND course_crn = ?";
            PreparedStatement deleteEnrollStmt = conn.prepareStatement(deleteEnrollSql);
            deleteEnrollStmt.setInt(1, studentId);
            deleteEnrollStmt.setInt(2, courseCrn);
            deleteEnrollStmt.executeUpdate();

            // Commit the transaction if all steps succeeded
            conn.commit();
            return true;

        } catch (SQLException e) {
            // Rollback transaction on any error
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
            // Restore auto-commit and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore default behavior
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}