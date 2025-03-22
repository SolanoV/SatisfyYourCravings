package com.example.daaproject;

import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class BackgroundAnimationManager {
    private static BackgroundAnimationManager instance;
    private Pane backgroundPane;
    private TranslateTransition translate;

    // Private constructor to prevent instantiation
    private BackgroundAnimationManager() {
        setupBackground();
    }

    // Get the singleton instance
    public static BackgroundAnimationManager getInstance() {
        if (instance == null) {
            instance = new BackgroundAnimationManager();
        }
        return instance;
    }

    private void setupBackground() {
        // Create the background pane
        backgroundPane = new Pane();
        backgroundPane.setPrefSize(2000, 2000);
        Image backgroundImage = new Image("file:src/main/resources/images/backgroundTile2.png");

        // Debug: Check if the image loaded
        if (backgroundImage.isError()) {
            System.out.println("Error loading background image: " + backgroundImage.getException());
        } else {
            System.out.println("Background image loaded successfully in BackgroundAnimationManager: " + backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
        }

        // Tile the background image
        for (int x = 0; x < 2000; x += 500) {
            for (int y = 0; y < 2000; y += 500) {
                ImageView tile = new ImageView(backgroundImage);
                tile.setFitWidth(500);
                tile.setFitHeight(500);
                tile.setX(x);
                tile.setY(y);
                backgroundPane.getChildren().add(tile);
            }
        }

        // Prevent the backgroundPane from affecting the layout
        backgroundPane.setManaged(false);
        backgroundPane.setTranslateX(-500);
        backgroundPane.setTranslateY(-500);
        backgroundPane.setStyle("-fx-background-color: transparent;");

        // Set up the animation
        translate = new TranslateTransition(Duration.seconds(30), backgroundPane);
        translate.setFromX(-500);
        translate.setFromY(-500);
        translate.setToX(0);
        translate.setToY(0);
        translate.setCycleCount(TranslateTransition.INDEFINITE);
        translate.play();
        System.out.println("Background animation started in BackgroundAnimationManager: " + translate.getStatus());
    }

    public Pane getBackgroundPane() {
        return backgroundPane;
    }

    public TranslateTransition getTranslateTransition() {
        return translate;
    }

    public void pause() {
        if (translate != null) {
            translate.pause();
            System.out.println("Background animation paused: " + translate.getStatus());
        }
    }

    public void resume() {
        if (translate != null) {
            translate.play();
            System.out.println("Background animation resumed: " + translate.getStatus());
        }
    }
}