package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label profileNameLabel;

    @FXML
    private Label profileBioLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private ListView<Post> feedListView;

    private String userName;
    private int userId;

    public void setUserName(String userName) {
        this.userName = userName;
        welcomeLabel.setText("Welcome, " + userName + "!");
        loadUserId();
        loadProfileData(LoginController.currentUserId);
        loadFeed();
    }

    private void loadUserId() {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT user_id FROM Users WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, userName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    private void loadFeed() {
        ObservableList<Post> posts = FXCollections.observableArrayList();
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT p.post_id, p.content, u.name FROM Posts p " +
                           "JOIN Users u ON p.user_id = u.user_id " +
                           "JOIN Friendships f ON (p.user_id = f.user1_id OR p.user_id = f.user2_id) " +
                           "WHERE (f.user1_id = ? OR f.user2_id = ?) AND p.user_id != ? AND f.status = 'accepted'";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int postId = resultSet.getInt("post_id");
                String content = resultSet.getString("content");
                String author = resultSet.getString("name");
                int likes = getLikesCount(postId);
                List<Comment> comments = getComments(postId);
                posts.add(new Post(postId, content, author, likes, comments));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        feedListView.setItems(posts);
        feedListView.setCellFactory(new Callback<ListView<Post>, ListCell<Post>>() {
            @Override
            public ListCell<Post> call(ListView<Post> listView) {
                return new PostCell();
            }
        });
    }

    private int getLikesCount(int postId) {
        int likes = 0;
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT COUNT(*) AS likes FROM Likes WHERE post_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                likes = resultSet.getInt("likes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return likes;
    }

    private List<Comment> getComments(int postId) {
        List<Comment> comments = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT c.comment_id, c.content, u.name FROM Comments c " +
                           "JOIN Users u ON c.user_id = u.user_id WHERE c.post_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, postId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int commentId = resultSet.getInt("comment_id");
                String content = resultSet.getString("content");
                String author = resultSet.getString("name");
                comments.add(new Comment(commentId, content, author));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    private void handleLikePost(Post post) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "INSERT INTO Likes (post_id, user_id) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, post.getId());
            statement.setInt(2, userId);
            statement.executeUpdate();
            post.incrementLikes();
            feedListView.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleCommentPost(Post post, String commentContent) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "INSERT INTO Comments (post_id, user_id, content) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, post.getId());
            statement.setInt(2, userId);
            statement.setString(3, commentContent);
            statement.executeUpdate();
            post.addComment(new Comment(0, commentContent, userName)); // Assuming comment ID is auto-generated
            feedListView.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    private void handleProfileButtonAction() {
        handleProfileClick();
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

    private class PostCell extends ListCell<Post> {
        @Override
        protected void updateItem(Post post, boolean empty) {
            super.updateItem(post, empty);
            if (empty || post == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox vBox = new VBox(10);
                Label contentLabel = new Label(post.getContent());
                Label authorLabel = new Label("by " + post.getAuthor());
                Label likesLabel = new Label("Likes: " + post.getLikes());
                Button likeButton = new Button("Like");
                likeButton.setOnAction(event -> handleLikePost(post));
                TextArea commentArea = new TextArea();
                commentArea.setPromptText("Enter your comment...");
                Button commentButton = new Button("Comment");
                commentButton.setOnAction(event -> handleCommentPost(post, commentArea.getText()));
                VBox commentsBox = new VBox(5);
                for (Comment comment : post.getComments()) {
                    Label commentLabel = new Label(comment.getAuthor() + ": " + comment.getContent());
                    commentsBox.getChildren().add(commentLabel);
                }
                vBox.getChildren().addAll(contentLabel, authorLabel, likesLabel, likeButton, commentArea, commentButton, commentsBox);
                setGraphic(vBox);
            }
        }
    }

    private static class Post {
        private final int id;
        private final String content;
        private final String author;
        private int likes;
        private final List<Comment> comments;

        public Post(int id, String content, String author, int likes, List<Comment> comments) {
            this.id = id;
            this.content = content;
            this.author = author;
            this.likes = likes;
            this.comments = comments;
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public String getAuthor() {
            return author;
        }

        public int getLikes() {
            return likes;
        }

        public void incrementLikes() {
            this.likes++;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public void addComment(Comment comment) {
            this.comments.add(comment);
        }
    }

    private static class Comment {
        private final int id;
        private final String content;
        private final String author;

        public Comment(int id, String content, String author) {
            this.id = id;
            this.content = content;
            this.author = author;
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public String getAuthor() {
            return author;
        }
    }
}