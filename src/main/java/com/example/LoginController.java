package com.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField nameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String name = nameField.getText();
        String password = passwordField.getText();

        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT password_hash FROM users WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("password_hash");
                if (storedHashedPassword.equals(hashPassword(password))) {
                    System.out.println("User logged in with name: " + name);
                    switchToHome(name);
                } else {
                    System.out.println("Invalid name or password.");
                    errorLabel.setText("Invalid name or password.");
                }
            } else {
                System.out.println("Invalid name or password.");
                errorLabel.setText("Invalid name or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error logging in user.");
            errorLabel.setText("Error logging in user.");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private void switchToHome(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Scene scene = new Scene(loader.load());
            HomeController controller = loader.getController();
            controller.setUserName(name);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToHome(ActionEvent event) {
        try {
            App.setRoot("primary");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}