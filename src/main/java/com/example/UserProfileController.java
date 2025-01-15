package com.example;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UserProfileController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea bioField;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Button uploadButton;

    @FXML
    private Button saveButton;

    @FXML
    private Label errorLabel;

    private String profilePicturePath;

    @FXML
    private void handleUploadButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog((Stage) uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            profilePicturePath = selectedFile.toURI().toString();
            profileImageView.setImage(new Image(profilePicturePath));
        }
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        String name = nameField.getText();
        String bio = bioField.getText();

        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "UPDATE Profiles SET bio = ?, profile_picture = ? WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, bio);
            statement.setString(2, profilePicturePath);
            statement.setInt(3, LoginController.currentUserId);
            statement.executeUpdate();
            System.out.println("Profile updated for user: " + name);
            refreshHomePage();
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Error updating profile.");
        }
    }

    private void refreshHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Scene scene = new Scene(loader.load());
            HomeController controller = loader.getController();
            controller.setUserName(nameField.getText());
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadProfile(int userId) {
        System.out.println("Loading profile for userId: " + userId);
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT u.name, p.bio, p.profile_picture FROM Users u JOIN Profiles p ON u.user_id = p.user_id WHERE u.user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Profile data found for userId: " + userId);
                nameField.setText(resultSet.getString("name"));
                bioField.setText(resultSet.getString("bio"));
                profilePicturePath = resultSet.getString("profile_picture");
                if (profilePicturePath != null) {
                    profileImageView.setImage(new Image(profilePicturePath));
                }
            } else {
                System.out.println("No profile data found for userId: " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
