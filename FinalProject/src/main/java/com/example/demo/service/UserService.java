package com.example.demo.service;
import com.example.demo.model.User;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Service
public class UserService {

    private static final String DB_URL = "jdbc:sqlite:FinalProject/src/main/resources/db/video_games.db";

    public boolean checkIfUsernameExists(String username) {
        String sql = "SELECT 1 FROM User WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // If a record exists, username is taken
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; // If an error occurs, assume username doesn't exist
    }

    public boolean createUser(String username, String password) {
        String sql = "INSERT INTO User (username, password) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true; // Return true if insertion is successful
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; // Return false if an error occurs
    }

    public Long createUserAndGetId(String username, String password) {
        String sqlInsert = "INSERT INTO User (username, password) VALUES (?, ?)";
        String sqlSelect = "SELECT userId FROM User WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Insert the new user
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setString(1, username);
                pstmtInsert.setString(2, password);
                pstmtInsert.executeUpdate();
            }

            // Retrieve the new user's ID
            try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect)) {
                pstmtSelect.setString(1, username);
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("userId");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // Return null if user creation fails
    }

    public Long getUserIdIfValid(String username, String password) {
        String sql = "SELECT userId FROM User WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("userId");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}