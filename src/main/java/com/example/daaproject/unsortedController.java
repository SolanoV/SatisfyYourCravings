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
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class unsortedController extends BaseController implements Initializable {
    private Stage stage;
    private List<MenuController.FoodCombination> combinations;
    private Map<String, Integer> stallToIndex;
    private int[][] distanceMatrix;

    @FXML private TableView<FoodCombinationDisplay> combinationsTable;
    @FXML private Button unsortedCombinationsNext;
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
        populateTable();
    }

    public void switchToBudget() throws IOException {
        if (combinations == null || combinations.isEmpty()) {
            System.out.println("UnsortedController: No combinations to pass to BudgetController");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Budget.fxml"));
        Parent root = loader.load();
        BudgetController controller = loader.getController();
        controller.setStage(stage);
        controller.setCombinations(combinations, stallToIndex, distanceMatrix);
        controller.setCategories(
                combinations.get(0).getMainCourse().getName().split(" ")[0],
                combinations.get(0).getDessert().getName().split(" ")[0],
                combinations.get(0).getDrink().getName().split(" ")[0]
        );
        Scene scene = new Scene(root);
        scene.getStylesheets().add("file:src/main/resources/css/base.css");
        scene.getStylesheets().add("file:src/main/resources/css/Budget.css");
        stage.setScene(scene);
    }

    private void populateTable() {
        if (combinations == null || stallToIndex == null || distanceMatrix == null) {
            return;
        }

        combinationsTable.getItems().clear();
        for (int i = 0; i < combinations.size(); i++) {
            MenuController.FoodCombination combo = combinations.get(i);
            FoodCombinationDisplay display = new FoodCombinationDisplay(i + 1, combo);
            combinationsTable.getItems().add(display);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBackgroundAnimation();  // Ensure the animated background is set up

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), unsortedCombinationsNext);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), unsortedCombinationsNext);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), unsortedCombinationsNext);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            unsortedCombinationsNext.setLayoutY(originalY);
            isBouncing = false;
            pulse.play();
            fade.play();
        });

        // Play bounce animation on hover with debounce
        unsortedCombinationsNext.setOnMouseEntered(event -> {
            long currentTime = System.currentTimeMillis();
            if (!isBouncing && (currentTime - lastHoverTime) >= DEBOUNCE_TIME) {
                isBouncing = true;
                lastHoverTime = currentTime;
                pulse.stop();
                fade.stop();
                bounce.stop();
                unsortedCombinationsNext.setLayoutY(originalY);
                bounce.playFromStart();
            }
        });

        // Resume pulsing and fading after mouse exits
        unsortedCombinationsNext.setOnMouseExited(event -> {
            if (!isBouncing) {
                pulse.play();
                fade.play();
            }
        });
    }

    public class FoodCombinationDisplay {
        private final SimpleIntegerProperty index;
        private final SimpleStringProperty mainCourseName;
        private final SimpleStringProperty dessertName;
        private final SimpleStringProperty drinkName;
        private final SimpleIntegerProperty distanceFromStart;
        private final SimpleIntegerProperty mainToDessertDistance;
        private final SimpleIntegerProperty dessertToDrinkDistance;
        private final SimpleIntegerProperty drinkToStartDistance;
        private final SimpleStringProperty totalDistance;
        private final SimpleStringProperty totalPrice;

        public FoodCombinationDisplay(int index, MenuController.FoodCombination combo) {
            this.index = new SimpleIntegerProperty(index);

            MenuController.FoodItem main = combo.getMainCourse();
            MenuController.FoodItem dessert = combo.getDessert();
            MenuController.FoodItem drink = combo.getDrink();

            this.mainCourseName = new SimpleStringProperty(
                    String.format("%s (%s) - ₱ %.2f", main.getName(), main.getStall(), main.getPrice())
            );
            this.dessertName = new SimpleStringProperty(
                    String.format("%s (%s) - ₱ %.2f", dessert.getName(), dessert.getStall(), dessert.getPrice())
            );
            this.drinkName = new SimpleStringProperty(
                    String.format("%s (%s) - ₱ %.2f", drink.getName(), drink.getStall(), drink.getPrice())
            );

            Integer startIdx = stallToIndex.get("Starting Point");
            Integer mainIdx = stallToIndex.get(main.getStall());
            Integer dessertIdx = stallToIndex.get(dessert.getStall());
            Integer drinkIdx = stallToIndex.get(drink.getStall());

            this.distanceFromStart = new SimpleIntegerProperty(distanceMatrix[startIdx][mainIdx]);
            this.mainToDessertDistance = new SimpleIntegerProperty(distanceMatrix[mainIdx][dessertIdx]);
            this.dessertToDrinkDistance = new SimpleIntegerProperty(distanceMatrix[dessertIdx][drinkIdx]);
            this.drinkToStartDistance = new SimpleIntegerProperty(distanceMatrix[drinkIdx][startIdx]);
            this.totalDistance = new SimpleStringProperty(
                    String.format("%d steps",
                            distanceMatrix[startIdx][mainIdx] +
                                    distanceMatrix[mainIdx][dessertIdx] +
                                    distanceMatrix[dessertIdx][drinkIdx] +
                                    distanceMatrix[drinkIdx][startIdx])
            );
            this.totalPrice = new SimpleStringProperty(String.format("₱ %.2f", combo.getTotalPrice()));
        }

        public int getIndex() { return index.get(); }
        public String getMainCourseName() { return mainCourseName.get(); }
        public String getDessertName() { return dessertName.get(); }
        public String getDrinkName() { return drinkName.get(); }
        public int getDistanceFromStart() { return distanceFromStart.get(); }
        public int getMainToDessertDistance() { return mainToDessertDistance.get(); }
        public int getDessertToDrinkDistance() { return dessertToDrinkDistance.get(); }
        public int getDrinkToStartDistance() { return drinkToStartDistance.get(); }
        public String getTotalDistance() { return totalDistance.get(); }
        public String getTotalPrice() { return totalPrice.get(); }
    }
}