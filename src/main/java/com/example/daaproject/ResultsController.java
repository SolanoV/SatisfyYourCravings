package com.example.daaproject;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ResultsController extends BaseController implements Initializable {
    private Stage stage;
    private List<MenuController.FoodCombination> combinations;
    private Map<String, Integer> stallToIndex;
    private int[][] distanceMatrix;
    private MenuController.FoodCombination bestCombination;
    private int currentIndex = 0;

    @FXML private Label mainStallLabel;
    @FXML private Label mainItemLabel;
    @FXML private Label dessertStallLabel;
    @FXML private Label dessertItemLabel;
    @FXML private Label drinkStallLabel;
    @FXML private Label drinkItemLabel;
    @FXML private Label distanceD2Label;
    @FXML private Label distanceD3Label;
    @FXML private Label totalPriceLabel;
    @FXML private Label totalStepsLabel;
    @FXML private Button resultsNext;
    @FXML private Button nextOptionButton;
    @FXML private Button showMapButton;

    private ScaleTransition pulse;
    private FadeTransition fade;
    private TranslateTransition bounce;
    private boolean isBouncing = false;
    private double originalY;
    private double originalX;
    private double originalZ;
    private long lastHoverTime = 0;
    private static final long DEBOUNCE_TIME = 300;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCombinations(List<MenuController.FoodCombination> combinations, Map<String, Integer> stallToIndex, int[][] distanceMatrix) {
        this.combinations = combinations;
        this.stallToIndex = stallToIndex;
        this.distanceMatrix = distanceMatrix;
        selectBestCombination();
        populateUI();
    }

    // New method to set the current index and restore the state
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
        selectBestCombination();
        populateUI();
    }

    // Getter for currentIndex so tspShowMapController can access it
    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<MenuController.FoodCombination> getCombinations() {
        return combinations;
    }

    private void selectBestCombination() {
        if (combinations == null || combinations.isEmpty()) {
            return;
        }
        bestCombination = combinations.get(currentIndex);
    }

    private int calculateTotalDistance(MenuController.FoodCombination combo) {
        Integer startIdx = stallToIndex.get("Starting Point");
        Integer mainIdx = stallToIndex.get(combo.getMainCourse().getStall());
        Integer dessertIdx = stallToIndex.get(combo.getDessert().getStall());
        Integer drinkIdx = stallToIndex.get(combo.getDrink().getStall());

        return distanceMatrix[startIdx][mainIdx] +
                distanceMatrix[mainIdx][dessertIdx] +
                distanceMatrix[dessertIdx][drinkIdx] +
                distanceMatrix[drinkIdx][startIdx];
    }

    private void populateUI() {
        if (bestCombination == null) {
            return;
        }

        mainStallLabel.setText(bestCombination.getMainCourse().getStall());
        mainItemLabel.setText(String.format("%s - ₱ %.2f", bestCombination.getMainCourse().getName(), bestCombination.getMainCourse().getPrice()));
        dessertStallLabel.setText(bestCombination.getDessert().getStall());
        dessertItemLabel.setText(String.format("%s - ₱ %.2f", bestCombination.getDessert().getName(), bestCombination.getDessert().getPrice()));
        drinkStallLabel.setText(bestCombination.getDrink().getStall());
        drinkItemLabel.setText(String.format("%s - ₱ %.2f", bestCombination.getDrink().getName(), bestCombination.getDrink().getPrice()));

        Integer startIdx = stallToIndex.get("Starting Point");
        Integer mainIdx = stallToIndex.get(bestCombination.getMainCourse().getStall());
        Integer dessertIdx = stallToIndex.get(bestCombination.getDessert().getStall());
        Integer drinkIdx = stallToIndex.get(bestCombination.getDrink().getStall());

        int d2 = distanceMatrix[mainIdx][dessertIdx];
        int d3 = distanceMatrix[dessertIdx][drinkIdx];
        distanceD2Label.setText(String.valueOf(d2));
        distanceD3Label.setText(String.valueOf(d3));

        totalPriceLabel.setText(String.format("₱ %.2f", bestCombination.getTotalPrice()));
        totalStepsLabel.setText(String.valueOf(calculateTotalDistance(bestCombination)));
    }

    @FXML
    public void switchToTspShowMap() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("tspShowMap.fxml"));
        Parent root = loader.load();
        tspShowMapController controller = loader.getController();
        controller.setStage(stage);
        controller.setData(bestCombination, stallToIndex, distanceMatrix, this);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("file:src/main/resources/css/base.css");
        scene.getStylesheets().add("file:src/main/resources/css/tspShowMap.css");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToFeedback() throws IOException {
        System.out.println("Switching to Feedback from Results with combination: " + bestCombination);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("feedback.fxml"));
        Parent root = loader.load();
        FeedbackController controller = loader.getController();
        controller.setStage(stage);
        controller.setCombination(bestCombination);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("file:src/main/resources/css/base.css");
        scene.getStylesheets().add("file:src/main/resources/css/Feedback.css");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void showNextOption() {
        if (combinations == null || combinations.isEmpty()) {
            return;
        }

        currentIndex = (currentIndex + 1) % combinations.size();
        bestCombination = combinations.get(currentIndex);
        populateUI();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBackgroundAnimation();  // Ensure the animated background is set up

        originalY = resultsNext.getLayoutY();

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), resultsNext);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), resultsNext);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), resultsNext);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            resultsNext.setLayoutY(originalY);
            isBouncing = false;
            pulse.play();
            fade.play();
        });

        // Play bounce animation on hover with debounce
        resultsNext.setOnMouseEntered(event -> {
            long currentTime = System.currentTimeMillis();
            if (!isBouncing && (currentTime - lastHoverTime) >= DEBOUNCE_TIME) {
                isBouncing = true;
                lastHoverTime = currentTime;
                pulse.stop();
                fade.stop();
                bounce.stop();
                resultsNext.setLayoutY(originalY);
                bounce.playFromStart();
            }
        });

        // Resume pulsing and fading after mouse exits
        resultsNext.setOnMouseExited(event -> {
            if (!isBouncing) {
                pulse.play();
                fade.play();
            }
        });

        originalX = nextOptionButton.getLayoutY();

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), nextOptionButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), nextOptionButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), nextOptionButton);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            nextOptionButton.setLayoutX(originalX);
            isBouncing = false;
            pulse.play();
            fade.play();
        });

        // Play bounce animation on hover with debounce
        nextOptionButton.setOnMouseEntered(event -> {
            long currentTime = System.currentTimeMillis();
            if (!isBouncing && (currentTime - lastHoverTime) >= DEBOUNCE_TIME) {
                isBouncing = true;
                lastHoverTime = currentTime;
                pulse.stop();
                fade.stop();
                bounce.stop();
                nextOptionButton.setLayoutY(originalX);
                bounce.playFromStart();
            }
        });

        // Resume pulsing and fading after mouse exits
        nextOptionButton.setOnMouseExited(event -> {
            if (!isBouncing) {
                pulse.play();
                fade.play();
            }
        });

        originalZ = showMapButton.getLayoutY();

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), showMapButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), showMapButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), showMapButton);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            showMapButton.setLayoutY(originalZ);
            isBouncing = false;
            pulse.play();
            fade.play();
        });

        // Play bounce animation on hover with debounce
        showMapButton.setOnMouseEntered(event -> {
            long currentTime = System.currentTimeMillis();
            if (!isBouncing && (currentTime - lastHoverTime) >= DEBOUNCE_TIME) {
                isBouncing = true;
                lastHoverTime = currentTime;
                pulse.stop();
                fade.stop();
                bounce.stop();
                showMapButton.setLayoutY(originalZ);
                bounce.playFromStart();
            }
        });

        // Resume pulsing and fading after mouse exits
        showMapButton.setOnMouseExited(event -> {
            if (!isBouncing) {
                pulse.play();
                fade.play();
            }
        });
    }
}