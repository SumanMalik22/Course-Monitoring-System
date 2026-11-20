package com.cms;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Batch progress reports: on screen and as CSV. Pass a faculty id to limit to one faculty, or null for all. */
public class ReportService {

    private static final String[] HEADERS =
            {"Batch ID", "Batch", "Course", "Faculty", "Total days", "Completed", "Progress %"};

    private static List<String[]> fetch(Integer facultyId) throws SQLException {
        String sql = "SELECT b.id, b.name, c.name, u.full_name, COUNT(cp.id), "
                + "COALESCE(SUM(cp.status = 'COMPLETED'), 0) "
                + "FROM batches b JOIN courses c ON c.id = b.course_id "
                + "JOIN users u ON u.id = b.faculty_id "
                + "LEFT JOIN course_plans cp ON cp.batch_id = b.id "
                + (facultyId != null ? "WHERE b.faculty_id = ? " : "")
                + "GROUP BY b.id, b.name, c.name, u.full_name ORDER BY b.id";
        List<String[]> rows = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (facultyId != null) ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int total = rs.getInt(5);
                    int done = rs.getInt(6);
                    rows.add(new String[]{
                            String.valueOf(rs.getInt(1)), rs.getString(2), rs.getString(3), rs.getString(4),
                            String.valueOf(total), String.valueOf(done), percent(done, total)});
                }
            }
        }
        return rows;
    }

    static String percent(int done, int total) {
        return total == 0 ? "0.0" : String.format("%.1f", done * 100.0 / total);
    }

    public static void printProgress(Integer facultyId) throws SQLException {
        System.out.printf("%-6s %-16s %-20s %-16s %-6s %-10s %-10s%n",
                "ID", "Batch", "Course", "Faculty", "Days", "Completed", "Progress");
        for (String[] r : fetch(facultyId)) {
            System.out.printf("%-6s %-16s %-20s %-16s %-6s %-10s %-10s%n",
                    r[0], r[1], r[2], r[3], r[4], r[5], r[6] + "%");
        }
    }

    public static void exportCsv(Integer facultyId) throws SQLException {
        String name = Input.text("File name (e.g. progress_report.csv): ");
        if (name.contains("/") || name.contains("\\") || name.contains("..")) {
            System.out.println("Enter just a file name, without folders.");
            return;
        }
        if (!name.toLowerCase().endsWith(".csv")) name += ".csv";

        List<String[]> rows = fetch(facultyId);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get(name), StandardCharsets.UTF_8))) {
            out.println(csvLine(HEADERS));
            for (String[] r : rows) out.println(csvLine(r));
            System.out.println("Saved " + rows.size() + " rows to " + name);
        } catch (IOException e) {
            System.out.println("Could not write file: " + e.getMessage());
        }
    }

    static String csvLine(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(csvEscape(fields[i]));
        }
        return sb.toString();
    }

    /** Quotes a field if needed and neutralises spreadsheet formula injection (=, +, -, @ at the start). */
    static String csvEscape(String value) {
        if (value == null) return "";
        String v = value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
