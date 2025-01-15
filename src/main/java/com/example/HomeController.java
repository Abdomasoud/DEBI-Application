package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label profileNameLabel;

    @FXML
    private Label profileBioLabel;

    @FXML
    private ImageView profileImageView;

    private String userName;

    public void setUserName(String userName) {
        this.userName = userName;
        welcomeLabel.setText("Welcome, " + userName + "!");
        loadProfileData(LoginController.currentUserId);
    }

    @FXML
    private void handleProfileClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
            Scene scene = new Scene(loader.load());
            ProfileController controller = loader.getController();
            controller.setUserName(userName);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading profile view.");
        }
    }

    @FXML
    private void handleLogoutButtonAction() {
        try {
            LoginController.currentUserId = -1; // Reset the current user ID
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading login view.");
        }
    }

    @FXML
    private void handleProfileButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("userProfile.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("User Profile");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(welcomeLabel.getScene().getWindow());
            UserProfileController controller = loader.getController();
            controller.loadProfile(LoginController.currentUserId);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading profile view.");
        }
    }

    private void loadProfileData(int userId) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT u.name, p.bio, p.profile_picture FROM Users u JOIN Profiles p ON u.user_id = p.user_id WHERE u.user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String bio = resultSet.getString("bio");
                String profilePicturePath = resultSet.getString("profile_picture");

                profileNameLabel.setText(name != null ? name : "User Name");
                profileBioLabel.setText(bio != null ? bio : "User Bio");
                if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                    profileImageView.setImage(new Image(profilePicturePath));
                } else {
                    profileImageView.setImage(null); // Clear the image if no profile picture is set
                }
            } else {
                profileNameLabel.setText("User Name");
                profileBioLabel.setText("User Bio");
                profileImageView.setImage(null); // Clear the image if no profile picture is set
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}