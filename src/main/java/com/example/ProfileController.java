package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ProfileController {

    @FXML
    private Label profileLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private ListView<String> postsListView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private HBox createPostBox;

    @FXML
    private TextField postContentField;

    private String userName;
    private int userId;

    public void setUserName(String userName) {
        this.userName = userName;
        profileLabel.setText("Profile: " + userName);
        welcomeLabel.setText("Welcome, " + userName + "!");
        loadUserData();
        loadUserPosts();
    }

    private void loadUserData() {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT user_id, email FROM Users WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, userName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("user_id");
                String email = resultSet.getString("email");
                nameLabel.setText("Name: " + userName);
                emailLabel.setText("Email: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserPosts() {
        ObservableList<String> posts = FXCollections.observableArrayList();
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT post_id, content FROM Posts WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int postId = resultSet.getInt("post_id");
                String content = resultSet.getString("content");
                posts.add("Post No: " + postId + "\n" + content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        postsListView.setItems(posts);
    }

    @FXML
    private void handleProfileClick() {
        // No action needed as we are already on the profile page
    }

    @FXML
    private void handleLogoutButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading login view.");
        }
    }

    @FXML
    private void handleCreatePostAction() {
        createPostBox.setVisible(true);
    }

    @FXML
    private void handleCreatePost() {
        String content = postContentField.getText();
        if (content.isEmpty()) {
            return;
        }
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "INSERT INTO posts (user_id, content) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, content);
            statement.executeUpdate();
            loadUserPosts(); // Refresh the posts list
        } catch (SQLException e) {
            e.printStackTrace();
        }
        postContentField.clear();
        createPostBox.setVisible(false);
    }

    @FXML
    private void handleCancelPost() {
        postContentField.clear();
        createPostBox.setVisible(false);
    }
}