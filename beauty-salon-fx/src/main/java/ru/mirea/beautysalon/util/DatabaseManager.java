package ru.mirea.beautysalon.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/database.properties")) {
            if (in == null) {
                throw new IllegalStateException("Не найден файл database.properties в resources");
            }
            PROPERTIES.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить database.properties: " + e.getMessage(), e);
        }
    }

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        String url = PROPERTIES.getProperty("db.url");
        String user = PROPERTIES.getProperty("db.user");
        String password = PROPERTIES.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
