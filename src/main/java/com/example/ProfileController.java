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
    private ListView<Friend> friendsListView;

    @FXML
    private ListView<FriendRequest> pendingRequestsListView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private HBox createPostBox;

    @FXML
    private HBox addFriendBox;

    @FXML
    private TextField postContentField;

    @FXML
    private TextField friendIdField;

    @FXML
    private Label friendRequestStatusLabel;

    private String userName;
    private int userId;
    private int editingPostId = -1;

    public void setUserName(String userName) {
        this.userName = userName;
        profileLabel.setText("Profile: " + userName);
        welcomeLabel.setText("Welcome, " + userName + "!");
        loadUserData();
        loadUserPosts();
        loadFriends();
        loadPendingRequests();
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

    private void loadFriends() {
        ObservableList<Friend> friends = FXCollections.observableArrayList();
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT u.user_id, u.name FROM Users u " +
                           "JOIN Friendships f ON (u.user_id = f.user1_id OR u.user_id = f.user2_id) " +
                           "WHERE (f.user1_id = ? OR f.user2_id = ?) AND u.user_id != ? AND f.status = 'accepted'";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int friendId = resultSet.getInt("user_id");
                String friendName = resultSet.getString("name");
                friends.add(new Friend(friendId, friendName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        friendsListView.setItems(friends);
        friendsListView.setCellFactory(new Callback<ListView<Friend>, ListCell<Friend>>() {
            @Override
            public ListCell<Friend> call(ListView<Friend> listView) {
                return new FriendCell();
            }
        });
    }

    private void loadPendingRequests() {
        ObservableList<FriendRequest> pendingRequests = FXCollections.observableArrayList();
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT f.friendship_id, u.user_id, u.name FROM Users u " +
                           "JOIN Friendships f ON u.user_id = f.user1_id " +
                           "WHERE f.user2_id = ? AND f.status = 'pending'";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int friendshipId = resultSet.getInt("friendship_id");
                int requesterId = resultSet.getInt("user_id");
                String requesterName = resultSet.getString("name");
                pendingRequests.add(new FriendRequest(friendshipId, requesterId, requesterName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pendingRequestsListView.setItems(pendingRequests);
        pendingRequestsListView.setCellFactory(new Callback<ListView<FriendRequest>, ListCell<FriendRequest>>() {
            @Override
            public ListCell<FriendRequest> call(ListView<FriendRequest> listView) {
                return new FriendRequestCell();
            }
        });
    }

    private void handleAcceptRequest(FriendRequest request) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "UPDATE Friendships SET status = 'accepted' WHERE friendship_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, request.getFriendshipId());
            statement.executeUpdate();
            loadFriends(); // Refresh the friends list
            loadPendingRequests(); // Refresh the pending requests list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeclineRequest(FriendRequest request) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "DELETE FROM Friendships WHERE friendship_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, request.getFriendshipId());
            statement.executeUpdate();
            loadPendingRequests(); // Refresh the pending requests list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfileClick() {
        // No action needed as we are already on the profile page
    }

    @FXML
    private void handleHomeClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Scene scene = new Scene(loader.load());
            HomeController homeController = loader.getController();
            homeController.setUserName(userName); // Pass the user data to the home controller
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true); // Maximize the window
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading home view.");
        }
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
    private void handleAddFriendAction() {
        addFriendBox.setVisible(true);
    }

    @FXML
    private void handleSendFriendRequest() {
        String friendNameText = friendIdField.getText();
        if (friendNameText.isEmpty()) {
            friendRequestStatusLabel.setText("Error: No one with this name");
            friendRequestStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT user_id FROM Users WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, friendNameText);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int friendId = resultSet.getInt("user_id");
                query = "INSERT INTO Friendships (user1_id, user2_id, status) VALUES (?, ?, 'pending')";
                statement = connection.prepareStatement(query);
                statement.setInt(1, userId);
                statement.setInt(2, friendId);
                statement.executeUpdate();
                friendRequestStatusLabel.setText("Friend request sent");
                friendRequestStatusLabel.setStyle("-fx-text-fill: green;");
                loadPendingRequests(); // Refresh the pending requests list
            } else {
                friendRequestStatusLabel.setText("Error: No one with this name");
                friendRequestStatusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            friendRequestStatusLabel.setText("Error: No one with this name");
            friendRequestStatusLabel.setStyle("-fx-text-fill: red;");
        }
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

    private class FriendCell extends ListCell<Friend> {
        @Override
        protected void updateItem(Friend friend, boolean empty) {
            super.updateItem(friend, empty);
            if (empty || friend == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hBox = new HBox(10);
                Label nameLabel = new Label(friend.getName());
                Button blockButton = new Button("Block");
                blockButton.setOnAction(event -> handleDeleteFriend(friend));
                hBox.getChildren().addAll(nameLabel, blockButton);
                setGraphic(hBox);
            }
        }
    }

    private void handleDeleteFriend(Friend friend) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "DELETE FROM Friendships WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friend.getId());
            statement.setInt(3, friend.getId());
            statement.setInt(4, userId);
            statement.executeUpdate();
            loadFriends(); // Refresh the friends list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class FriendRequestCell extends ListCell<FriendRequest> {
        @Override
        protected void updateItem(FriendRequest request, boolean empty) {
            super.updateItem(request, empty);
            if (empty || request == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hBox = new HBox(10);
                Label nameLabel = new Label(request.getRequesterName());
                Button acceptButton = new Button("Accept");
                acceptButton.setOnAction(event -> handleAcceptRequest(request));
                Button declineButton = new Button("Decline");
                declineButton.setOnAction(event -> handleDeclineRequest(request));
                hBox.getChildren().addAll(nameLabel, acceptButton, declineButton);
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

    private static class Friend {
        private final int id;
        private final String name;

        public Friend(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class FriendRequest {
        private final int friendshipId;
        private final int requesterId;
        private final String requesterName;

        public FriendRequest(int friendshipId, int requesterId, String requesterName) {
            this.friendshipId = friendshipId;
            this.requesterId = requesterId;
            this.requesterName = requesterName;
        }

        public int getFriendshipId() {
            return friendshipId;
        }

        public int getRequesterId() {
            return requesterId;
        }

        public String getRequesterName() {
            return requesterName;
        }
    }
}