package com.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signUpButton;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleSignUp(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password.equals(confirmPassword)) {
            String hashedPassword = hashPassword(password);
            try (Connection connection = DatabaseUtil.getConnection()) {
                String query = "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, username);
                statement.setString(2, email);
                statement.setString(3, hashedPassword);
                statement.executeUpdate();
                System.out.println("User signed up with username: " + username);
                switchToHome(username);
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // MySQL error code for duplicate entry
                    System.out.println("Username or email already taken.");
                    errorLabel.setText("Username or email already taken.");
                } else {
                    e.printStackTrace();
                    System.out.println("Error signing up user.");
                    errorLabel.setText("Error signing up user.");
                }
            }
        } else {
            errorLabel.setText("Passwords do not match.");
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

    private void switchToHome(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Scene scene = new Scene(loader.load());
            HomeController controller = loader.getController();
            controller.setUserName(username);
            Stage stage = (Stage) signUpButton.getScene().getWindow();
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