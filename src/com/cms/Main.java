package com.cms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            ensureDefaultAdmin();
        } catch (SQLException e) {
            System.out.println("Cannot connect to the database: " + e.getMessage());
            System.out.println("Check db.properties and that MySQL is running with sql/schema.sql applied.");
            return;
        }

        System.out.println("=== Course Monitoring System ===");
        while (true) {
            System.out.println("\n1. Admin login");
            System.out.println("2. Faculty login");
            System.out.println("0. Exit");
            int choice = Input.integer("Choose: ");
            try {
                switch (choice) {
                    case 1: login("ADMIN"); break;
                    case 2: login("FACULTY"); break;
                    case 0: System.out.println("Goodbye."); return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static void login(String role) throws SQLException {
        String username = Input.text("Username: ");
        String password = Input.text("Password: ");

        String sql = "SELECT id, password_hash, full_name FROM users WHERE username = ? AND role = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !PasswordUtil.verify(password, rs.getString("password_hash"))) {
                    System.out.println("Invalid username or password.");
                    return;
                }
                int id = rs.getInt("id");
                System.out.println("Welcome, " + rs.getString("full_name") + "!");
                if (role.equals("ADMIN")) {
                    AdminMenu.run(id);
                } else {
                    FacultyMenu.run(id);
                }
            }
        }
    }

    /** Creates admin / admin123 the first time the app runs. Change this password after logging in. */
    private static void ensureDefaultAdmin() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement ins = con.prepareStatement(
                        "INSERT INTO users (username, password_hash, full_name, role) VALUES (?,?,?,'ADMIN')")) {
                    ins.setString(1, "admin");
                    ins.setString(2, PasswordUtil.hash("admin123"));
                    ins.setString(3, "System Admin");
                    ins.executeUpdate();
                    System.out.println("First run: created admin account (admin / admin123).");
                }
            }
        }
    }
}
