package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/fakebook";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void createProfile(int userId) throws SQLException {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO Profiles (user_id) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    public static UserProfile getProfile(int userId) throws SQLException {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM Profiles WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new UserProfile(
                    resultSet.getInt("profile_id"),
                    resultSet.getInt("user_id"),
                    resultSet.getString("bio"),
                    resultSet.getString("profile_picture")
                );
            }
        }
        return null;
    }
}