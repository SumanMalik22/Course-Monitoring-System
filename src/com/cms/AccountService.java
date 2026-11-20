package com.cms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Account actions shared by admin and faculty. */
public class AccountService {

    public static void changePassword(int userId) throws SQLException {
        String current = Input.text("Current password: ");
        try (Connection con = DBUtil.getConnection()) {
            String stored;
            try (PreparedStatement ps = con.prepareStatement("SELECT password_hash FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Account not found.");
                        return;
                    }
                    stored = rs.getString(1);
                }
            }
            if (!PasswordUtil.verify(current, stored)) {
                System.out.println("Current password is incorrect.");
                return;
            }
            String newPassword = Input.text("New password (min 6 characters): ");
            if (newPassword.length() < 6) {
                System.out.println("Password must be at least 6 characters.");
                return;
            }
            if (!newPassword.equals(Input.text("Confirm new password: "))) {
                System.out.println("Passwords do not match.");
                return;
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
                ps.setString(1, PasswordUtil.hash(newPassword));
                ps.setInt(2, userId);
                ps.executeUpdate();
                System.out.println("Password changed.");
            }
        }
    }
}
