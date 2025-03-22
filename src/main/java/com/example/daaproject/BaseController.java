package com.example.daaproject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;  // Add this import
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class BaseController implements Initializable {
    protected Stage stage;
    @FXML protected Parent root;  // Generic Parent to support both VBox and BorderPane

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    protected void setupBackgroundAnimation() {
        if (root == null) {
            throw new IllegalStateException("Root element is null. Ensure fx:id=\"root\" is set in FXML for " + this.getClass().getSimpleName());
        }

        // Get the shared background pane from the singleton
        BackgroundAnimationManager backgroundManager = BackgroundAnimationManager.getInstance();
        Pane backgroundPane = backgroundManager.getBackgroundPane();

        // Add the backgroundPane based on the root type
        if (root instanceof VBox) {
            ((VBox) root).getChildren().add(0, backgroundPane);  // Add as first child for VBox
            System.out.println("Root children after adding background in " + this.getClass().getSimpleName() + ": " + ((VBox) root).getChildren().size());
        } else if (root instanceof BorderPane) {
            // For BorderPane, wrap the background in a StackPane in the center
            BorderPane borderPane = (BorderPane) root;
            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(backgroundPane);  // Add background first (bottom layer)
            if (borderPane.getCenter() != null) {
                stackPane.getChildren().add(borderPane.getCenter());  // Add existing center on top
            }
            borderPane.setCenter(stackPane);
            System.out.println("Added background to BorderPane center in " + this.getClass().getSimpleName());
        } else {
            throw new IllegalStateException("Unsupported root type: " + root.getClass().getSimpleName() + " in " + this.getClass().getSimpleName());
        }

        // Ensure transparency for the root
        root.setStyle("-fx-background-color: transparent;");
    }
}