package com.example;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;
    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary").load(), WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Facebook Application");
        stage.getIcons().add(new Image(App.class.getResourceAsStream("icon.png")));
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml).load());
    }

    static void setRoot(String fxml, int userId) throws IOException {
        FXMLLoader loader = loadFXML(fxml);
        scene.setRoot(loader.load());
        if (fxml.equals("userProfile")) {
            UserProfileController controller = loader.getController();
            controller.loadProfile(userId);
        }
    }

    private static FXMLLoader loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader;
    }

    public static void main(String[] args) {
        launch();
    }
}