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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.*;

public class FeedbackController extends BaseController implements Initializable {
    private Stage stage;
    private MenuController.FoodCombination combination;
    private Map<String, Double> stallRatings = new HashMap<>();
    private List<String[]> stallData;
    private final String csvFilePath = "src/main/resources/csv/FoodDrinkStallsRatings.csv";

    @FXML private Label mainStallLabel;
    @FXML private Label dessertStallLabel;
    @FXML private Label drinkStallLabel;
    @FXML private HBox mainStallRating;
    @FXML private HBox dessertStallRating;
    @FXML private HBox drinkStallRating;
    @FXML private Button feedbackNext;

    private ScaleTransition pulse;
    private FadeTransition fade;
    private TranslateTransition bounce;
    private boolean isBouncing = false;
    private double originalY;
    private long lastHoverTime = 0;
    private static final long DEBOUNCE_TIME = 300;

    public void setStage(Stage stage) {
        this.stage = stage;
        System.out.println("Stage set in FeedbackController: " + stage);
    }

    public void setCombination(MenuController.FoodCombination combination) {
        System.out.println("setCombination called with combination: " + combination);
        if (combination == null) {
            System.out.println("Warning: combination is null");
        } else {
            System.out.println("Main Course: " + combination.getMainCourse());
            System.out.println("Dessert: " + combination.getDessert());
            System.out.println("Drink: " + combination.getDrink());
        }
        this.combination = combination;
        try {
            this.stallData = readCSV();
            System.out.println("stallData after readCSV: " + (stallData != null ? "Not null" : "Null"));
        } catch (Exception e) {
            System.err.println("Unexpected error in readCSV: " + e.getMessage());
            e.printStackTrace();
            this.stallData = new ArrayList<>();
            this.stallData.add(new String[]{"Food/Drink Stall", "Ratings (out of 5.0):"});
            System.out.println("Initialized stallData with fallback data due to error.");
        }
        updateStallLabels();
    }

    private void updateStallLabels() {
        if (combination != null) {
            MenuController.FoodItem mainCourse = combination.getMainCourse();
            MenuController.FoodItem dessert = combination.getDessert();
            MenuController.FoodItem drink = combination.getDrink();

            String mainStall = mainCourse != null ? mainCourse.getStall() : null;
            String dessertStall = dessert != null ? dessert.getStall() : null;
            String drinkStall = drink != null ? drink.getStall() : null;

            mainStallLabel.setText(mainStall != null ? mainStall : "Unknown Main Stall");
            dessertStallLabel.setText(dessertStall != null ? dessertStall : "Unknown Dessert Stall");
            drinkStallLabel.setText(drinkStall != null ? drinkStall : "Unknown Drink Stall");

            System.out.println("Updated stall labels:");
            System.out.println("Main Course: " + (mainCourse != null ? mainCourse : "null"));
            System.out.println("Main Stall: " + mainStall);
            System.out.println("Dessert: " + (dessert != null ? dessert : "null"));
            System.out.println("Dessert Stall: " + dessertStall);
            System.out.println("Drink: " + (drink != null ? drink : "null"));
            System.out.println("Drink Stall: " + drinkStall);
        } else {
            System.out.println("No combination provided, labels not updated.");
            mainStallLabel.setText("Unknown Main Stall");
            dessertStallLabel.setText("Unknown Dessert Stall");
            drinkStallLabel.setText("Unknown Drink Stall");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("FeedbackController initialized.");
        setupBackgroundAnimation();  // Ensure the animated background is set up
        feedbackNext.setText("Skip"); // Initially set to "Skip" since no stars are filled
        updateButtonText(); // Ensure the button style is set initially
        setupStarRating(mainStallRating, "main");
        setupStarRating(dessertStallRating, "dessert");
        setupStarRating(drinkStallRating, "drink");

        originalY = feedbackNext.getLayoutY();

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), feedbackNext);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), feedbackNext);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), feedbackNext);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            feedbackNext.setLayoutY(originalY);
            isBouncing = false;
            pulse.play();
            fade.play();
        });

        // Play bounce animation on hover with debounce
        feedbackNext.setOnMouseEntered(event -> {
            long currentTime = System.currentTimeMillis();
            if (!isBouncing && (currentTime - lastHoverTime) >= DEBOUNCE_TIME) {
                isBouncing = true;
                lastHoverTime = currentTime;
                pulse.stop();
                fade.stop();
                bounce.stop();
                feedbackNext.setLayoutY(originalY);
                bounce.playFromStart();
            }
        });

        // Resume pulsing and fading after mouse exits
        feedbackNext.setOnMouseExited(event -> {
            if (!isBouncing) {
                pulse.play();
                fade.play();
            }
        });
    }

    private void setupStarRating(HBox starBox, String stallKey) {
        Text[] stars = new Text[5];
        double[] userRating = {0};

        for (int i = 0; i < 5; i++) {
            stars[i] = new Text("☆");
            stars[i].setStyle("-fx-font-size: 40;"); // Font size set to 40 (from previous change)
            stars[i].setFill(Color.WHITE); // Initial color is white (unfilled)
            final int rating = i + 1;
            stars[i].setOnMouseClicked(event -> {
                userRating[0] = rating;
                stallRatings.put(stallKey, (double) rating);
                updateStars(stars, rating);
                updateButtonText();
            });
            stars[i].setOnMouseEntered(event -> updateStars(stars, rating));
            stars[i].setOnMouseExited(event -> updateStars(stars, (int) userRating[0]));
            starBox.getChildren().add(stars[i]);
        }
    }

    private void updateStars(Text[] stars, int rating) {
        for (int i = 0; i < 5; i++) {
            stars[i].setText(i < rating ? "★" : "☆");
            stars[i].setFill(i < rating ? Color.YELLOW : Color.WHITE); // Filled stars yellow, unfilled stars white
        }
    }

    private void updateButtonText() {
        if (stallRatings.isEmpty()) {
            feedbackNext.setText("Skip");
            // Apply red border when the button text is "Skip"
            feedbackNext.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50px;");
            System.out.println("No ratings provided, button set to 'Skip' with red border.");
        } else {
            feedbackNext.setText("Next");
            // Revert to default style (yellow border from CSS) when the button text is "Next"
            feedbackNext.setStyle(""); // Clear inline style to let CSS take over
            System.out.println("Ratings provided, button set to 'Next' with default border.");
        }
    }

    @FXML
    public void handleFeedbackNext() throws IOException {
        System.out.println("handleFeedbackNext called. stallData: " + (stallData != null ? "Not null" : "Null"));
        if (stallData == null) {
            System.out.println("Error: stallData is null, cannot proceed with feedback. Proceeding to Credits.");
            switchToCredits();
            return;
        }

        System.out.println("Before update:");
        logStallData();

        if (stallRatings.isEmpty()) {
            System.out.println("No ratings provided, skipping to Credits.");
            switchToCredits();
            return;
        }

        if (combination != null) {
            String mainStall = combination.getMainCourse().getStall();
            String dessertStall = combination.getDessert().getStall();
            String drinkStall = combination.getDrink().getStall();

            if (stallRatings.containsKey("main") && mainStall != null) {
                System.out.println("Updating rating for Main Stall (" + mainStall + "): " + stallRatings.get("main"));
                updateRating(mainStall, stallRatings.get("main"));
            }
            if (stallRatings.containsKey("dessert") && dessertStall != null) {
                System.out.println("Updating rating for Dessert Stall (" + dessertStall + "): " + stallRatings.get("dessert"));
                updateRating(dessertStall, stallRatings.get("dessert"));
            }
            if (stallRatings.containsKey("drink") && drinkStall != null) {
                System.out.println("Updating rating for Drink Stall (" + drinkStall + "): " + stallRatings.get("drink"));
                updateRating(drinkStall, stallRatings.get("drink"));
            }

            System.out.println("After update:");
            logStallData();

            updateCSV();
        } else {
            System.out.println("No combination available, cannot update ratings.");
        }

        switchToCredits();
    }

    private void switchToCredits() throws IOException {
        System.out.println("Switching to Credits scene.");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Credits.fxml"));
        Parent root = loader.load();
        CreditsController controller = loader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("file:src/main/resources/css/base.css");
        scene.getStylesheets().add("file:src/main/resources/css/Credits.css");
        stage.setScene(scene);
    }

    private List<String[]> readCSV() {
        List<String[]> stalls = new ArrayList<>();
        File file = new File(csvFilePath);
        System.out.println("Attempting to read CSV file from: " + csvFilePath);

        if (!file.exists()) {
            System.out.println("CSV file does not exist at " + csvFilePath + ". Initializing with empty data.");
            stalls.add(new String[]{"Food/Drink Stall", "Ratings (out of 5.0):"});
            System.out.println("Initialized empty CSV data:");
            logStallData(stalls);

            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                System.out.println("Parent directory does not exist, attempting to create: " + parentDir.getAbsolutePath());
                if (!parentDir.mkdirs()) {
                    System.err.println("Error: Could not create parent directory " + parentDir.getAbsolutePath());
                    return stalls;
                }
            }

            if (!file.canWrite()) {
                System.err.println("Error: No write permission for " + csvFilePath + ". Using in-memory data only.");
                return stalls;
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath))) {
                for (String[] stall : stalls) {
                    bw.write(String.join(",", stall));
                    bw.newLine();
                }
                System.out.println("Successfully wrote empty CSV data to " + csvFilePath);
            } catch (IOException e) {
                System.err.println("Error writing empty CSV: " + e.getMessage());
                e.printStackTrace();
                return stalls;
            }
        } else {
            if (!file.canRead()) {
                System.err.println("Error: No read permission for " + csvFilePath + ". Using empty in-memory data.");
                stalls.add(new String[]{"Food/Drink Stall", "Ratings (out of 5.0):"});
                System.out.println("Initialized empty CSV data (due to read permission issue):");
                logStallData(stalls);
                return stalls;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] row = line.split(",");
                    if (row.length == 2) {
                        stalls.add(row);
                    } else {
                        System.err.println("Skipping invalid CSV row: " + line);
                    }
                }
                System.out.println("Loaded CSV data:");
                logStallData(stalls);
            } catch (IOException e) {
                System.err.println("Error reading CSV: " + e.getMessage());
                e.printStackTrace();
                stalls.clear();
                stalls.add(new String[]{"Food/Drink Stall", "Ratings (out of 5.0):"});
                System.out.println("Initialized empty CSV data (due to read error):");
                logStallData(stalls);
            }
        }
        return stalls;
    }

    private void updateRating(String stallName, double userRating) {
        boolean stallFound = false;
        for (int i = 1; i < stallData.size(); i++) {
            if (stallData.get(i)[0].trim().equals(stallName)) {
                try {
                    double currentRating = Double.parseDouble(stallData.get(i)[1]);
                    double newRating = (currentRating + userRating) / 2;
                    stallData.get(i)[1] = String.format("%.2f", newRating);
                    stallFound = true;
                    break;
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing rating for stall " + stallName + ": " + stallData.get(i)[1]);
                    double newRating = userRating / 2;
                    stallData.get(i)[1] = String.format("%.2f", newRating);
                    stallFound = true;
                    break;
                }
            }
        }
        if (!stallFound) {
            stallData.add(new String[]{stallName, String.format("%.2f", userRating)});
            System.out.println("Added new stall to CSV: " + stallName + " with initial rating " + userRating);
        }
    }

    private void updateCSV() {
        if (stallData == null) {
            System.out.println("Error: stallData is null, cannot update CSV.");
            return;
        }

        File file = new File(csvFilePath);
        if (!file.canWrite() && file.exists()) {
            System.err.println("Error: No write permission for " + csvFilePath + ". Changes will not be saved to disk.");
            return;
        }

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            System.out.println("Parent directory does not exist, attempting to create: " + parentDir.getAbsolutePath());
            if (!parentDir.mkdirs()) {
                System.err.println("Error: Could not create parent directory " + parentDir.getAbsolutePath() + ". Changes will not be saved to disk.");
                return;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath))) {
            for (String[] stall : stallData) {
                bw.write(String.join(",", stall));
                bw.newLine();
            }
            System.out.println("Saved CSV data to " + csvFilePath + ":");
            logStallData();
        } catch (IOException e) {
            System.err.println("Error updating CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logStallData() {
        logStallData(stallData);
    }

    private void logStallData(List<String[]> data) {
        if (data == null) {
            System.out.println("No data to log (data is null).");
            return;
        }
        for (String[] row : data) {
            System.out.println(String.join(",", row));
        }
    }
}