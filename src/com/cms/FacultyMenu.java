package com.cms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Faculty: view own batches and update day-wise status (only for their own batches). */
public class FacultyMenu {

    public static void run(int facultyId) {
        while (true) {
            System.out.println("\n--- Faculty Menu ---");
            System.out.println("1. View my batches");
            System.out.println("2. View plan of my batch");
            System.out.println("3. Update status of a day");
            System.out.println("4. Progress summary");
            System.out.println("5. Export my progress (CSV)");
            System.out.println("6. Change my password");
            System.out.println("0. Logout");
            int choice = Input.integer("Choose: ");
            try {
                switch (choice) {
                    case 1: myBatches(facultyId); break;
                    case 2: viewPlan(facultyId); break;
                    case 3: updateStatus(facultyId); break;
                    case 4: ReportService.printProgress(facultyId); break;
                    case 5: ReportService.exportCsv(facultyId); break;
                    case 6: AccountService.changePassword(facultyId); break;
                    case 0: return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static void myBatches(int facultyId) throws SQLException {
        String sql = "SELECT b.id, b.name, c.name, b.start_date, b.timing FROM batches b "
                + "JOIN courses c ON c.id = b.course_id WHERE b.faculty_id = ? ORDER BY b.id";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-18s %-22s %-12s %-14s%n", "ID", "Batch", "Course", "Start", "Timing");
                while (rs.next()) {
                    System.out.printf("%-5d %-18s %-22s %-12s %-14s%n",
                            rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDate(4), rs.getString(5));
                }
            }
        }
    }

    private static boolean ownsBatch(Connection con, int facultyId, int batchId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM batches WHERE id = ? AND faculty_id = ?")) {
            ps.setInt(1, batchId);
            ps.setInt(2, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void viewPlan(int facultyId) throws SQLException {
        int batchId = Input.integer("Batch ID: ");
        try (Connection con = DBUtil.getConnection()) {
            if (!ownsBatch(con, facultyId, batchId)) {
                System.out.println("That batch is not assigned to you.");
                return;
            }
        }
        AdminMenu.printPlan(batchId);
    }

    private static void updateStatus(int facultyId) throws SQLException {
        int planId = Input.integer("Plan entry ID: ");
        String status = Input.text("New status (PENDING/COMPLETED): ").toUpperCase();
        if (!status.equals("PENDING") && !status.equals("COMPLETED")) {
            System.out.println("Status must be PENDING or COMPLETED.");
            return;
        }
        String sql = "UPDATE course_plans cp JOIN batches b ON b.id = cp.batch_id "
                + "SET cp.status = ?, cp.updated_at = NOW() WHERE cp.id = ? AND b.faculty_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, planId);
            ps.setInt(3, facultyId);
            System.out.println(ps.executeUpdate() > 0 ? "Status updated." : "No such entry in your batches.");
        }
    }
}
