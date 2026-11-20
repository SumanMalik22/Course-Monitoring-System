package com.cms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens JDBC connections using settings from db.properties. */
public class DBUtil {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = new FileInputStream("db.properties")) {
            PROPS.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read db.properties in the current folder", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("url"),
                PROPS.getProperty("user"),
                PROPS.getProperty("password"));
    }
}
