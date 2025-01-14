package com.example;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    public void setUserName(String userName) {
        welcomeLabel.setText("Welcome, " + userName + "!");
    }

    @FXML
    private void handleLogoutButtonAction() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading login view.");
        }
    }

    @FXML
    private void handleProfileButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("User Profile");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(welcomeLabel.getScene().getWindow());
            ProfileController controller = loader.getController();
            controller.loadProfile(LoginController.currentUserId);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading profile view.");
        }
    }
}