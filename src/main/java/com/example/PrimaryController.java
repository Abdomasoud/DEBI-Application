package com.example;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PrimaryController {

    @FXML
    private Button signupButton;

    @FXML
    private Button loginButton;

    @FXML
    private void switchToSignup(ActionEvent event) throws IOException {
        App.setRoot("signup");
    }

    @FXML
    private void switchToLogin(ActionEvent event) throws IOException {
        App.setRoot("login");
    }
}