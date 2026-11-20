package com.cms;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Admin: manage faculty, courses, batches and course plans. */
public class AdminMenu {

    public static void run(int adminId) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println(" 1. Register faculty        2. View faculty        3. Delete faculty");
            System.out.println(" 4. Add course              5. View courses       6. Update course        7. Delete course");
            System.out.println(" 8. Create batch            9. View batches      10. Delete batch");
            System.out.println("11. Add plan entry         12. View batch plan   13. Delete plan entry");
            System.out.println("14. Register admin         15. View admins");
            System.out.println("16. Progress report        17. Export report (CSV)   18. Change my password");
            System.out.println(" 0. Logout");
            int choice = Input.integer("Choose: ");
            try {
                switch (choice) {
                    case 1: registerFaculty(); break;
                    case 2: viewFaculty(); break;
                    case 3: deleteById("users", "faculty", " AND role = 'FACULTY'"); break;
                    case 4: addCourse(); break;
                    case 5: viewCourses(); break;
                    case 6: updateCourse(); break;
                    case 7: deleteById("courses", "course", ""); break;
                    case 8: createBatch(); break;
                    case 9: viewBatches(); break;
                    case 10: deleteById("batches", "batch", ""); break;
                    case 11: addPlanEntry(); break;
                    case 12: viewPlan(); break;
                    case 13: deleteById("course_plans", "plan entry", ""); break;
                    case 14: registerAdmin(); break;
                    case 15: viewAdmins(); break;
                    case 16: ReportService.printProgress(null); break;
                    case 17: ReportService.exportCsv(null); break;
                    case 18: AccountService.changePassword(adminId); break;
                    case 0: return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    // ---------- Admins ----------
    private static void registerAdmin() throws SQLException {
        String name = Input.text("Full name: ");
        String username = Input.text("Username: ");
        String password = Input.text("Password: ");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO users (username, password_hash, full_name, role) VALUES (?,?,?,'ADMIN')")) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, name);
            ps.executeUpdate();
            System.out.println("Admin registered.");
        }
    }

    private static void viewAdmins() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, full_name, username FROM users WHERE role = 'ADMIN' ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            System.out.printf("%-5s %-25s %-15s%n", "ID", "Name", "Username");
            while (rs.next()) {
                System.out.printf("%-5d %-25s %-15s%n", rs.getInt(1), rs.getString(2), rs.getString(3));
            }
        }
    }

    // ---------- Faculty ----------
    private static void registerFaculty() throws SQLException {
        String name = Input.text("Full name: ");
        String username = Input.text("Username: ");
        String password = Input.text("Password: ");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO users (username, password_hash, full_name, role) VALUES (?,?,?,'FACULTY')")) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, name);
            ps.executeUpdate();
            System.out.println("Faculty registered.");
        }
    }

    private static void viewFaculty() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, full_name, username FROM users WHERE role = 'FACULTY' ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            System.out.printf("%-5s %-25s %-15s%n", "ID", "Name", "Username");
            while (rs.next()) {
                System.out.printf("%-5d %-25s %-15s%n", rs.getInt(1), rs.getString(2), rs.getString(3));
            }
        }
    }

    // ---------- Courses ----------
    private static void addCourse() throws SQLException {
        String name = Input.text("Course name: ");
        double fees = Input.decimal("Fees: ");
        int months = Input.integer("Duration (months): ");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO courses (name, fees, duration_months) VALUES (?,?,?)")) {
            ps.setString(1, name);
            ps.setDouble(2, fees);
            ps.setInt(3, months);
            ps.executeUpdate();
            System.out.println("Course added.");
        }
    }

    private static void viewCourses() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, name, fees, duration_months FROM courses ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            System.out.printf("%-5s %-30s %-12s %-10s%n", "ID", "Name", "Fees", "Months");
            while (rs.next()) {
                System.out.printf("%-5d %-30s %-12.2f %-10d%n",
                        rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getInt(4));
            }
        }
    }

    private static void updateCourse() throws SQLException {
        int id = Input.integer("Course ID to update: ");
        double fees = Input.decimal("New fees: ");
        int months = Input.integer("New duration (months): ");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE courses SET fees = ?, duration_months = ? WHERE id = ?")) {
            ps.setDouble(1, fees);
            ps.setInt(2, months);
            ps.setInt(3, id);
            System.out.println(ps.executeUpdate() > 0 ? "Course updated." : "No course with that ID.");
        }
    }

    // ---------- Batches ----------
    private static void createBatch() throws SQLException {
        int courseId = Input.integer("Course ID: ");
        int facultyId = Input.integer("Faculty ID: ");
        String name = Input.text("Batch name: ");
        String date = Input.text("Start date (YYYY-MM-DD): ");
        String timing = Input.text("Timing (e.g. 10:00-12:00): ");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO batches (course_id, faculty_id, name, start_date, timing) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, courseId);
            ps.setInt(2, facultyId);
            ps.setString(3, name);
            ps.setDate(4, Date.valueOf(date));
            ps.setString(5, timing);
            ps.executeUpdate();
            System.out.println("Batch created.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }
    }

    private static void viewBatches() throws SQLException {
        String sql = "SELECT b.id, b.name, c.name, u.full_name, b.start_date, b.timing "
                + "FROM batches b JOIN courses c ON c.id = b.course_id "
                + "JOIN users u ON u.id = b.faculty_id ORDER BY b.id";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.printf("%-5s %-18s %-22s %-18s %-12s %-14s%n",
                    "ID", "Batch", "Course", "Faculty", "Start", "Timing");
            while (rs.next()) {
                System.out.printf("%-5d %-18s %-22s %-18s %-12s %-14s%n",
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDate(5), rs.getString(6));
            }
        }
    }

    // ---------- Course plan ----------
    private static void addPlanEntry() throws SQLException {
        int batchId = Input.integer("Batch ID: ");
        int day = Input.integer("Day number: ");
        String topic = Input.text("Topic: ");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO course_plans (batch_id, day_no, topic) VALUES (?,?,?)")) {
            ps.setInt(1, batchId);
            ps.setInt(2, day);
            ps.setString(3, topic);
            ps.executeUpdate();
            System.out.println("Plan entry added (status PENDING until faculty updates it).");
        }
    }

    private static void viewPlan() throws SQLException {
        int batchId = Input.integer("Batch ID: ");
        printPlan(batchId);
    }

    static void printPlan(int batchId) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, day_no, topic, status, updated_at FROM course_plans "
                             + "WHERE batch_id = ? ORDER BY day_no")) {
            ps.setInt(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-5s %-35s %-10s %-20s%n", "ID", "Day", "Topic", "Status", "Updated");
                while (rs.next()) {
                    System.out.printf("%-5d %-5d %-35s %-10s %-20s%n",
                            rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4),
                            rs.getTimestamp(5) == null ? "-" : rs.getTimestamp(5).toString());
                }
            }
        }
    }

    // ---------- Generic delete ----------
    /** Table and extra clause are fixed strings from this class, never user input. */
    private static void deleteById(String table, String label, String extra) throws SQLException {
        int id = Input.integer("ID of " + label + " to delete: ");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM " + table + " WHERE id = ?" + extra)) {
            ps.setInt(1, id);
            System.out.println(ps.executeUpdate() > 0 ? "Deleted." : "No " + label + " with that ID.");
        }
    }
}
