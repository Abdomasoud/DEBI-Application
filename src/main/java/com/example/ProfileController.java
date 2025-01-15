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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ProfileController {

    @FXML
    private Label profileLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private ListView<Post> postsListView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private HBox createPostBox;

    @FXML
    private TextField postContentField;

    private String userName;
    private int userId;
    private int editingPostId = -1;

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
        ObservableList<Post> posts = FXCollections.observableArrayList();
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT post_id, content FROM Posts WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int postId = resultSet.getInt("post_id");
                String content = resultSet.getString("content");
                posts.add(new Post(postId, content));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        postsListView.setItems(posts);
        postsListView.setCellFactory(new Callback<ListView<Post>, ListCell<Post>>() {
            @Override
            public ListCell<Post> call(ListView<Post> listView) {
                return new PostCell();
            }
        });
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
            stage.setMaximized(true); // Maximize the window
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading login view.");
        }
    }

    @FXML
    private void handleCreatePostAction() {
        createPostBox.setVisible(true);
        editingPostId = -1; // Reset editing post ID
    }

    @FXML
    private void handleCreatePost() {
        String content = postContentField.getText();
        if (content.isEmpty()) {
            return;
        }
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query;
            if (editingPostId == -1) {
                query = "INSERT INTO Posts (user_id, content) VALUES (?, ?)";
            } else {
                query = "UPDATE Posts SET content = ? WHERE post_id = ?";
            }
            PreparedStatement statement = connection.prepareStatement(query);
            if (editingPostId == -1) {
                statement.setInt(1, userId);
                statement.setString(2, content);
            } else {
                statement.setString(1, content);
                statement.setInt(2, editingPostId);
            }
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

    private void handleEditPost(Post post) {
        postContentField.setText(post.getContent());
        createPostBox.setVisible(true);
        editingPostId = post.getId();
    }

    private void handleDeletePost(Post post) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "DELETE FROM Posts WHERE post_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, post.getId());
            statement.executeUpdate();
            loadUserPosts(); // Refresh the posts list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class PostCell extends ListCell<Post> {
        @Override
        protected void updateItem(Post post, boolean empty) {
            super.updateItem(post, empty);
            if (empty || post == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hBox = new HBox(10);
                Label contentLabel = new Label(post.getContent());
                Button editButton = new Button("Edit");
                editButton.setOnAction(event -> handleEditPost(post));
                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(event -> handleDeletePost(post));
                hBox.getChildren().addAll(contentLabel, editButton, deleteButton);
                setGraphic(hBox);
            }
        }
    }

    private static class Post {
        private final int id;
        private final String content;

        public Post(int id, String content) {
            this.id = id;
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }
    }
}