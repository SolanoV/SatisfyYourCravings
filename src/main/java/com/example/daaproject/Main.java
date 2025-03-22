package com.example.daaproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Image icon = new Image("file:src/main/resources/images/sycicon.png");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Start.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add("file:src/main/resources/css/base.css");
            scene.getStylesheets().add("file:src/main/resources/css/Start.css");
            StartController controller = loader.getController();
            controller.setStage(stage);
            stage.getIcons().add(icon);
            stage.setTitle("SatisfyYourCravings");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}