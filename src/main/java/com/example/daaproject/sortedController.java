package com.example.daaproject;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class sortedController extends BaseController implements Initializable {
    private Stage stage;
    private List<MenuController.FoodCombination> combinations;
    private Map<String, Integer> stallToIndex;
    private int[][] distanceMatrix;

    @FXML private TableView<FoodCombinationDisplay> combinationsTable;
    @FXML private TableColumn<FoodCombinationDisplay, String> indexColumn;
    @FXML private TableColumn<FoodCombinationDisplay, String> mainCourseColumn;
    @FXML private TableColumn<FoodCombinationDisplay, String> dessertColumn;
    @FXML private TableColumn<FoodCombinationDisplay, String> drinkColumn;
    @FXML private TableColumn<FoodCombinationDisplay, Double> stall1RatingColumn;
    @FXML private TableColumn<FoodCombinationDisplay, Double> stall2RatingColumn;
    @FXML private TableColumn<FoodCombinationDisplay, Double> stall3RatingColumn;
    @FXML private TableColumn<FoodCombinationDisplay, String> totalDistanceColumn;
    @FXML private TableColumn<FoodCombinationDisplay, String> totalPriceColumn;
    @FXML private TableColumn<FoodCombinationDisplay, String> totalRatingColumn;
    @FXML private Button sortedCombinationsNext;


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
        sortCombinations();
        populateTable();
    }

    @FXML
    public void switchToResults() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("results.fxml"));
        Parent root = loader.load();
        ResultsController controller = loader.getController();
        controller.setStage(stage);
        controller.setCombinations(combinations, stallToIndex, distanceMatrix);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("file:src/main/resources/css/base.css");
        scene.getStylesheets().add("file:src/main/resources/css/Results.css");
        stage.setScene(scene);
    }

    @FXML
    public void switchToFeedback() throws IOException {
        FoodCombinationDisplay selectedDisplay = combinationsTable.getSelectionModel().getSelectedItem();
        MenuController.FoodCombination selectedCombination = null;
        if (selectedDisplay != null) {
            int index = Integer.parseInt(selectedDisplay.getIndex().replace("★", "").trim()) - 1; // Adjust for 1-based index and star
            selectedCombination = combinations.get(index);
        } else if (!combinations.isEmpty()) {
            selectedCombination = combinations.get(0); // Default to first
        }

        if (selectedCombination == null) {
            System.out.println("No combination selected or available, cannot proceed to Feedback.");
            return;
        }

        System.out.println("Switching to Feedback with combination: " + selectedCombination);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Feedback.fxml"));
        Parent root = loader.load();
        FeedbackController controller = loader.getController();
        controller.setStage(stage);
        controller.setCombination(selectedCombination);
        stage.setScene(new Scene(root));
    }

    private void sortCombinations() {
        if (combinations != null) {
            combinations.sort(Comparator
                    .comparingDouble(MenuController.FoodCombination::getTotalRating).reversed()
                    .thenComparingInt(MenuController.FoodCombination::getTotalDistance)
                    .thenComparingDouble(MenuController.FoodCombination::getTotalPrice));
        }
    }

    private void populateTable() {
        if (combinations == null || stallToIndex == null || distanceMatrix == null) {
            return;
        }

        combinationsTable.getItems().clear();
        for (int i = 0; i < combinations.size(); i++) {
            MenuController.FoodCombination combo = combinations.get(i);
            FoodCombinationDisplay display = new FoodCombinationDisplay(i + 1, combo, i == 0);
            combinationsTable.getItems().add(display);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBackgroundAnimation();  // Ensure the animated background is set up

        indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
        mainCourseColumn.setCellValueFactory(new PropertyValueFactory<>("mainCourseName"));
        dessertColumn.setCellValueFactory(new PropertyValueFactory<>("dessertName"));
        drinkColumn.setCellValueFactory(new PropertyValueFactory<>("drinkName"));
        stall1RatingColumn.setCellValueFactory(new PropertyValueFactory<>("stall1Rating"));
        stall2RatingColumn.setCellValueFactory(new PropertyValueFactory<>("stall2Rating"));
        stall3RatingColumn.setCellValueFactory(new PropertyValueFactory<>("stall3Rating"));
        totalDistanceColumn.setCellValueFactory(new PropertyValueFactory<>("totalDistance"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalRatingColumn.setCellValueFactory(new PropertyValueFactory<>("totalRating"));

        originalY = sortedCombinationsNext.getLayoutY();

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), sortedCombinationsNext);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), sortedCombinationsNext);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), sortedCombinationsNext);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            sortedCombinationsNext.setLayoutY(originalY);
            isBouncing = false;
            pulse.play();
            fade.play();
        });

        // Play bounce animation on hover with debounce
        sortedCombinationsNext.setOnMouseEntered(event -> {
            long currentTime = System.currentTimeMillis();
            if (!isBouncing && (currentTime - lastHoverTime) >= DEBOUNCE_TIME) {
                isBouncing = true;
                lastHoverTime = currentTime;
                pulse.stop();
                fade.stop();
                bounce.stop();
                sortedCombinationsNext.setLayoutY(originalY);
                bounce.playFromStart();
            }
        });

        // Resume pulsing and fading after mouse exits
        sortedCombinationsNext.setOnMouseExited(event -> {
            if (!isBouncing) {
                pulse.play();
                fade.play();
            }
        });
    }

    public class FoodCombinationDisplay {
        private final SimpleStringProperty index;
        private final SimpleStringProperty mainCourseName;
        private final SimpleStringProperty dessertName;
        private final SimpleStringProperty drinkName;
        private final SimpleDoubleProperty stall1Rating;
        private final SimpleDoubleProperty stall2Rating;
        private final SimpleDoubleProperty stall3Rating;
        private final SimpleStringProperty totalDistance;
        private final SimpleStringProperty totalPrice;
        private final SimpleStringProperty totalRating;

        public FoodCombinationDisplay(int index, MenuController.FoodCombination combo, boolean isTop) {
            this.index = new SimpleStringProperty(isTop ? index + " ★" : String.valueOf(index));

            MenuController.FoodItem main = combo.getMainCourse();
            MenuController.FoodItem dessert = combo.getDessert();
            MenuController.FoodItem drink = combo.getDrink();

            this.mainCourseName = new SimpleStringProperty(
                    String.format("%s (%s)", main.getName(), main.getStall())
            );
            this.dessertName = new SimpleStringProperty(
                    String.format("%s (%s)", dessert.getName(), dessert.getStall())
            );
            this.drinkName = new SimpleStringProperty(
                    String.format("%s (%s)", drink.getName(), drink.getStall())
            );

            this.stall1Rating = new SimpleDoubleProperty(main.getRating());
            this.stall2Rating = new SimpleDoubleProperty(dessert.getRating());
            this.stall3Rating = new SimpleDoubleProperty(drink.getRating());

            Integer startIdx = stallToIndex.get("Starting Point");
            Integer mainIdx = stallToIndex.get(main.getStall());
            Integer dessertIdx = stallToIndex.get(dessert.getStall());
            Integer drinkIdx = stallToIndex.get(drink.getStall());

            int totalDistance = distanceMatrix[startIdx][mainIdx] +
                    distanceMatrix[mainIdx][dessertIdx] +
                    distanceMatrix[dessertIdx][drinkIdx] +
                    distanceMatrix[drinkIdx][startIdx];
            this.totalDistance = new SimpleStringProperty(totalDistance + " steps");
            this.totalPrice = new SimpleStringProperty(String.format("₱ %.2f", combo.getTotalPrice()));

            double avgRating = (main.getRating() + dessert.getRating() + drink.getRating()) / 3.0;
            this.totalRating = new SimpleStringProperty(String.format("%.1f", avgRating));
        }

        public String getIndex() { return index.get(); }
        public String getMainCourseName() { return mainCourseName.get(); }
        public String getDessertName() { return dessertName.get(); }
        public String getDrinkName() { return drinkName.get(); }
        public double getStall1Rating() { return stall1Rating.get(); }
        public double getStall2Rating() { return stall2Rating.get(); }
        public double getStall3Rating() { return stall3Rating.get(); }
        public String getTotalDistance() { return totalDistance.get(); }
        public String getTotalPrice() { return totalPrice.get(); }
        public String getTotalRating() { return totalRating.get(); }
    }
}