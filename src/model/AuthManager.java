package src.model;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class AuthManager {
    private Connection conn;

    public AuthManager() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("resources/config.properties"));

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkLogin(String email, String password) {
        String sql = "SELECT * FROM Kunde WHERE MAILADRESSE = ? AND Passwort = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
