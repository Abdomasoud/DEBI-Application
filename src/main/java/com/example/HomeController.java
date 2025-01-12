package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    public void setUserName(String userName) {
        welcomeLabel.setText("Welcome, " + userName + "!");
    }
}