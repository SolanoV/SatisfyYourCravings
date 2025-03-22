package com.example.daaproject;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BudgetController extends BaseController implements Initializable {
    private Stage stage;
    private List<MenuController.FoodCombination> combinations;
    private Map<String, Integer> stallToIndex;
    private int[][] distanceMatrix;
    private String mainCategory;
    private String dessertCategory;
    private String drinkCategory;
    private List<MenuController.FoodCombination> filteredCombinations;

    @FXML private TextField budgetField;
    @FXML private Label budgetFeedback;
    @FXML private Button checkBudgetButton;
    @FXML private Button retryExitButton;
    @FXML private Button budgetNext;


    private ScaleTransition pulse;
    private FadeTransition fade;
    private TranslateTransition bounce;
    private boolean isBouncing = false;
    private double originalY;
    private long lastHoverTime = 0;
    private static final long DEBOUNCE_TIME = 300;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCombinations(List<MenuController.FoodCombination> combinations, Map<String, Integer> stallToIndex, int[][] distanceMatrix) {
        this.combinations = combinations;
        this.stallToIndex = stallToIndex;
        this.distanceMatrix = distanceMatrix;
    }

    public void setCategories(String mainCategory, String dessertCategory, String drinkCategory) {
        this.mainCategory = mainCategory != null ? mainCategory.trim() : "";
        this.dessertCategory = dessertCategory != null ? dessertCategory.trim() : "";
        this.drinkCategory = drinkCategory != null ? drinkCategory.trim() : "";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBackgroundAnimation();  // Add the animated background

        // Optional: Add any additional initialization logic here
        budgetFeedback.setText("");  // Clear feedback label on start
        retryExitButton.setVisible(false);  // Hide retry/exit button initially
        retryExitButton.setManaged(false);
        budgetNext.setDisable(true);  // Disable next button until budget is checked

        originalY = budgetNext.getLayoutY();

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), budgetNext);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), budgetNext);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), budgetNext);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            budgetNext.setLayoutY(originalY);
            isBouncing = false;
            pulse.play();
            fade.play();
        });
    }



    @FXML
    public void checkBudget() throws IOException {
        String budgetText = budgetField.getText() != null ? budgetField.getText().trim() : "";

        // Reset label style to default
        budgetFeedback.setStyle("");

        if (budgetText.isEmpty()) {
            budgetFeedback.setText("Please enter a budget.");
            budgetFeedback.setStyle("-fx-text-fill: red;");
            showRetryExitButton();
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetText);
        } catch (NumberFormatException e) {
            budgetFeedback.setText("Please enter a valid budget (e.g., 150).");
            budgetFeedback.setStyle("-fx-text-fill: red;");
            showRetryExitButton();
            return;
        }

        filteredCombinations = getBestCombinations(mainCategory, dessertCategory, drinkCategory, budget);
        if (filteredCombinations.isEmpty()) {
            budgetFeedback.setText("Your budget is insufficient. No combinations found.");
            budgetFeedback.setStyle("-fx-text-fill: red;");
            showRetryExitButton();
            return;
        }

        // Success case - keep default text color
        budgetFeedback.setText(filteredCombinations.size() + " combinations available within your budget. Click 'Next' to view.");
        retryExitButton.setVisible(false);
        retryExitButton.setManaged(false);
        budgetNext.setDisable(false);
    }

    @FXML
    public void switchToSortedCombinations() throws IOException {
        if (filteredCombinations == null || filteredCombinations.isEmpty()) {
            budgetFeedback.setText("No combinations available to display.");
            budgetFeedback.setStyle("-fx-text-fill: red;");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sortedCombinations.fxml"));
        Parent root = loader.load();
        sortedController controller = loader.getController();
        controller.setStage(stage);
        controller.setCombinations(filteredCombinations, stallToIndex, distanceMatrix);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("file:src/main/resources/css/base.css");
        scene.getStylesheets().add("file:src/main/resources/css/sortedCombinations.css");
        stage.setScene(scene);
    }

    @FXML
    public void handleRetryExit() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Retry or Exit");
        alert.setHeaderText("Your budget was insufficient.");
        alert.setContentText("Would you like to retry or exit?");

        ButtonType retryButton = new ButtonType("Retry");
        ButtonType exitButton = new ButtonType("Exit");
        alert.getButtonTypes().setAll(retryButton, exitButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == retryButton) {
            budgetField.clear();
            budgetFeedback.setText("");
            budgetFeedback.setStyle(""); // Reset style when retrying
            retryExitButton.setVisible(false);
            retryExitButton.setManaged(false);
            budgetNext.setDisable(true);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
            Parent root = loader.load();
            MenuController controller = loader.getController();
            controller.setStage(stage);
            Scene scene = new Scene(root);
            scene.getStylesheets().add("file:src/main/resources/css/base.css");
            scene.getStylesheets().add("file:src/main/resources/css/Menu.css");
            stage.setScene(scene);
        } else {
            stage.close();
        }
    }

    private void showRetryExitButton() {
        retryExitButton.setVisible(true);
        retryExitButton.setManaged(true);
        budgetNext.setDisable(true);
    }

    private List<MenuController.FoodCombination> getBestCombinations(String mainCategory, String dessertCategory, String drinkCategory, double budget) {
        List<MenuController.FoodCombination> filtered = new ArrayList<>();
        if (combinations == null) {
            return filtered;
        }
        for (MenuController.FoodCombination combo : combinations) {
            if (combo.getTotalPrice() <= budget) {
                boolean matchesMain = mainCategory.isEmpty() || combo.getMainCourse().getName().toLowerCase().contains(mainCategory.toLowerCase());
                boolean matchesDessert = dessertCategory.isEmpty() || combo.getDessert().getName().toLowerCase().contains(dessertCategory.toLowerCase());
                boolean matchesDrink = drinkCategory.isEmpty() || combo.getDrink().getName().toLowerCase().contains(drinkCategory.toLowerCase());
                if (matchesMain && matchesDessert && matchesDrink) {
                    filtered.add(combo);
                }
            }
        }
        return filtered;
    }
}