package com.example.daaproject;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class tspShowMapController extends BaseController implements Initializable {
    private Stage stage;
    private MenuController.FoodCombination bestCombination;
    private Map<String, Integer> stallToIndex;
    private int[][] distanceMatrix;
    private ResultsController resultsController;

    @FXML private ImageView mapImageView;
    @FXML private Canvas pathCanvas;
    @FXML private Button backButton;

    private final Map<String, double[]> stallCoordinates = new HashMap<>();
    private final Map<String, Double> stallRatings = new HashMap<>();
    private final Set<String> visitedNodes = new HashSet<>();
    private final Map<String, String> stallNameMapping = new HashMap<>();

    private ScaleTransition pulse;
    private FadeTransition fade;
    private TranslateTransition bounce;
    private boolean isBouncing = false;
    private double originalY;
    private long lastHoverTime = 0;
    private static final long DEBOUNCE_TIME = 300;

    public void setStage(Stage stage) {
        this.stage = stage;
        System.out.println("Stage set successfully.");
    }

    public void setData(MenuController.FoodCombination bestCombination, Map<String, Integer> stallToIndex, int[][] distanceMatrix, ResultsController resultsController) {
        System.out.println("setData called.");
        this.bestCombination = bestCombination;
        this.stallToIndex = stallToIndex;
        this.distanceMatrix = distanceMatrix;
        this.resultsController = resultsController;

        if (bestCombination == null) {
            System.out.println("Error: bestCombination is null.");
            return;
        }
        if (stallToIndex == null) {
            System.out.println("Error: stallToIndex is null.");
            return;
        } else {
            System.out.println("stallToIndex contents: " + stallToIndex);
        }
        if (distanceMatrix == null) {
            System.out.println("Error: distanceMatrix is null.");
            return;
        } else {
            System.out.println("distanceMatrix dimensions: " + distanceMatrix.length + "x" + (distanceMatrix.length > 0 ? distanceMatrix[0].length : 0));
        }
        if (resultsController == null) {
            System.out.println("Warning: resultsController is null.");
        }

        List<String> missingStalls = new ArrayList<>();
        String[] stallsToCheck = {
                "Starting Point",
                bestCombination.getMainCourse().getStall(),
                bestCombination.getDessert().getStall(),
                bestCombination.getDrink().getStall()
        };
        for (String stall : stallsToCheck) {
            String normalizedStall = normalizeStallName(stall);
            if (!stallCoordinates.containsKey(normalizedStall)) {
                missingStalls.add(normalizedStall + " (in stallCoordinates)");
            }
            if (!stallToIndex.containsKey(stall)) {
                missingStalls.add(stall + " (in stallToIndex)");
            }
        }
        if (!missingStalls.isEmpty()) {
            System.out.println("Warning: Missing data for stalls: " + missingStalls);
        } else {
            System.out.println("All required stalls found in coordinates and indices.");
        }

        initializeRatings();
        drawPath();
    }

    @FXML
    public void goBack() throws IOException {
        System.out.println("goBack called.");
        if (resultsController != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("results.fxml"));
            Parent root = loader.load();
            ResultsController controller = loader.getController();
            controller.setStage(stage);
            // Pass the combinations, stallToIndex, and distanceMatrix
            controller.setCombinations(resultsController.getCombinations(), stallToIndex, distanceMatrix);
            // Restore the currentIndex to retain the selected combination
            controller.setCurrentIndex(resultsController.getCurrentIndex());
            Scene scene = new Scene(root);
            scene.getStylesheets().add("file:src/main/resources/css/base.css");
            scene.getStylesheets().add("file:src/main/resources/css/Results.css");
            stage.setScene(scene);
            stage.show();
        } else {
            System.out.println("ResultsController is null, cannot navigate back.");
        }
    }

    private void initializeCoordinates() {
        System.out.println("Initializing coordinates...");
        stallCoordinates.put("Starting Point", new double[]{554, 590});
        // Left column (x = 87)
        stallCoordinates.put("Mrs. Katsu-Good", new double[]{270, 80});
        stallCoordinates.put("Silogang", new double[]{87, 91});
        stallCoordinates.put("Dreamy Rice Meals", new double[]{87, 139});
        stallCoordinates.put("K-Corner Eumsik", new double[]{87, 187});
        stallCoordinates.put("Siomai Rice Food Hub", new double[]{87, 235});
        stallCoordinates.put("Just Taste It", new double[]{87, 283});
        stallCoordinates.put("Home of Rice Bowl Toppings", new double[]{87, 331});
        stallCoordinates.put("Dr. Lemon", new double[]{87, 379});
        stallCoordinates.put("Ka-Pangga Palamig", new double[]{120, 417});
        stallCoordinates.put("Allami Pasta", new double[]{87, 475});
        stallCoordinates.put("Allami Shawarma", new double[]{100, 503});
        // Right column (x = 1021)
        stallCoordinates.put("Allami-King Overload", new double[]{1021, 43});
        stallCoordinates.put("39ers", new double[]{1021, 91});
        stallCoordinates.put("The Red Meat", new double[]{1021, 139});
        stallCoordinates.put("CID Sisig", new double[]{1021, 187});
        stallCoordinates.put("Gustatory Delights", new double[]{1021, 235});
        stallCoordinates.put("Savor & Sip", new double[]{951, 290});
        stallCoordinates.put("House of Sisig", new double[]{1021, 331});
        stallCoordinates.put("JM Cabuhat Fresh Buko", new double[]{1021, 379});
        stallCoordinates.put("TeaXplode", new double[]{1021, 427});
        stallCoordinates.put("Caleigha's", new double[]{1021, 475});
        stallCoordinates.put("Shawarma Dealicious", new double[]{1021, 523});
        System.out.println("Coordinates initialized. Total stalls: " + stallCoordinates.size());
    }

    private void initializeStallNameMapping() {
        System.out.println("Initializing stall name mapping...");
        stallNameMapping.put("Ka-Pangga", "Ka-Pangga Palamig");
        System.out.println("Stall name mapping initialized with " + stallNameMapping.size() + " entries.");
    }

    private String normalizeStallName(String stallName) {
        String normalized = stallNameMapping.getOrDefault(stallName, stallName);
        if (!stallName.equals(normalized)) {
            System.out.println("Normalized stall name: '" + stallName + "' to '" + normalized + "'");
        }
        return normalized;
    }

    private void initializeRatings() {
        System.out.println("Initializing ratings...");
        if (bestCombination != null) {
            String mainStall = bestCombination.getMainCourse().getStall();
            String dessertStall = bestCombination.getDessert().getStall();
            String drinkStall = bestCombination.getDrink().getStall();
            String normalizedMainStall = normalizeStallName(mainStall);
            String normalizedDessertStall = normalizeStallName(dessertStall);
            String normalizedDrinkStall = normalizeStallName(drinkStall);

            System.out.println("Main Stall: " + mainStall + " (Normalized: " + normalizedMainStall + "), Rating: " + bestCombination.getMainCourse().getRating());
            System.out.println("Dessert Stall: " + dessertStall + " (Normalized: " + normalizedDessertStall + "), Rating: " + bestCombination.getDessert().getRating());
            System.out.println("Drink Stall: " + drinkStall + " (Normalized: " + normalizedDrinkStall + "), Rating: " + bestCombination.getDrink().getRating());

            stallRatings.put(normalizedMainStall, bestCombination.getMainCourse().getRating());
            stallRatings.put(normalizedDessertStall, bestCombination.getDessert().getRating());
            stallRatings.put(normalizedDrinkStall, bestCombination.getDrink().getRating());
            stallRatings.put("Starting Point", 0.0);
        } else {
            System.out.println("Error: bestCombination is null, cannot initialize ratings.");
        }
        visitedNodes.clear();
        visitedNodes.add("Starting Point");
        System.out.println("Ratings initialized with " + stallRatings.size() + " entries.");
    }

    private void drawPath() {
        System.out.println("drawPath called.");
        if (bestCombination == null || stallToIndex == null || distanceMatrix == null) {
            System.out.println("Cannot draw path: Missing data (bestCombination: " + (bestCombination != null) +
                    ", stallToIndex: " + (stallToIndex != null) +
                    ", distanceMatrix: " + (distanceMatrix != null) + ").");
            return;
        }

        GraphicsContext gc = pathCanvas.getGraphicsContext2D();
        if (gc == null) {
            System.out.println("Error: GraphicsContext is null.");
            return;
        }
        System.out.println("Canvas bounds: width=" + pathCanvas.getWidth() + ", height=" + pathCanvas.getHeight());
        gc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());

        String mainStall = bestCombination.getMainCourse().getStall();
        String dessertStall = bestCombination.getDessert().getStall();
        String drinkStall = bestCombination.getDrink().getStall();
        String normalizedMainStall = normalizeStallName(mainStall);
        String normalizedDessertStall = normalizeStallName(dessertStall);
        String normalizedDrinkStall = normalizeStallName(drinkStall);

        String[] path = new String[]{
                "Starting Point",
                mainStall,
                dessertStall,
                drinkStall,
                "Starting Point"
        };
        String[] normalizedPath = new String[]{
                "Starting Point",
                normalizedMainStall,
                normalizedDessertStall,
                normalizedDrinkStall,
                "Starting Point"
        };

        System.out.println("Path to animate:");
        for (int i = 0; i < path.length; i++) {
            System.out.println(" - " + path[i] + " (Normalized: " + normalizedPath[i] +
                    ", Coords: " + (stallCoordinates.containsKey(normalizedPath[i]) ? Arrays.toString(stallCoordinates.get(normalizedPath[i])) : "Missing") +
                    ", Index: " + stallToIndex.getOrDefault(path[i], -1) + ")");
        }

        redrawNodesAndRatings(normalizedPath);
        animatePath(path, normalizedPath);
    }

    private void animatePath(String[] path, String[] normalizedPath) {
        System.out.println("animatePath called with path length: " + path.length);
        GraphicsContext gc = pathCanvas.getGraphicsContext2D();
        if (gc == null) {
            System.out.println("Error: GraphicsContext is null in animatePath.");
            return;
        }

        Timeline timeline = new Timeline();
        double animationDuration = 1.0;

        for (int i = 0; i < path.length - 1; i++) {
            String startStall = path[i];
            String endStall = path[i + 1];
            String normalizedStartStall = normalizedPath[i];
            String normalizedEndStall = normalizedPath[i + 1];

            double[] startCoords = stallCoordinates.get(normalizedStartStall);
            double[] endCoords = stallCoordinates.get(normalizedEndStall);

            if (startCoords == null || endCoords == null) {
                System.out.println("Skipping segment: Missing coordinates for " + normalizedStartStall + " to " + normalizedEndStall);
                continue;
            }

            int startIdx = stallToIndex.getOrDefault(startStall, -1);
            int endIdx = stallToIndex.getOrDefault(endStall, -1);
            if (startIdx == -1 || endIdx == -1 || startIdx >= distanceMatrix.length || endIdx >= distanceMatrix[0].length) {
                System.out.println("Skipping segment: Invalid indices for " + startStall + " (" + startIdx +
                        ") to " + endStall + " (" + endIdx + ")");
                continue;
            }

            int distance = distanceMatrix[startIdx][endIdx];
            int steps = 20;
            double dx = (endCoords[0] - startCoords[0]) / steps;
            double dy = (endCoords[1] - startCoords[1]) / steps;
            double midX = (endCoords[0] + startCoords[0]) / 2;
            double midY = (endCoords[1] + startCoords[1]) / 2;

            System.out.println("Animating segment: " + startStall + " to " + endStall + ", distance: " + distance);

            for (int step = 0; step <= steps; step++) {
                final int currentStep = step;
                final int index = i;
                KeyFrame moveFrame = new KeyFrame(
                        Duration.seconds(animationDuration * i + (animationDuration / steps * step)),
                        event -> {
                            gc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
                            redrawNodesAndRatings(normalizedPath);

                            gc.setStroke(Color.BLUE);
                            gc.setLineWidth(2.0);
                            for (int j = 0; j < index; j++) {
                                double[] prevStart = stallCoordinates.get(normalizedPath[j]);
                                double[] prevEnd = stallCoordinates.get(normalizedPath[j + 1]);
                                if (prevStart != null && prevEnd != null) {
                                    int prevStartIdx = stallToIndex.getOrDefault(path[j], -1);
                                    int prevEndIdx = stallToIndex.getOrDefault(path[j + 1], -1);
                                    if (prevStartIdx != -1 && prevEndIdx != -1 &&
                                            prevStartIdx < distanceMatrix.length && prevEndIdx < distanceMatrix[0].length) {
                                        int prevDistance = distanceMatrix[prevStartIdx][prevEndIdx];
                                        drawArrow(gc, prevStart[0], prevStart[1], prevEnd[0], prevEnd[1]);
                                        double prevMidX = (prevStart[0] + prevEnd[0]) / 2;
                                        double prevMidY = (prevStart[1] + prevEnd[1]) / 2;
                                        // Draw distance with black outline and white fill
                                        String distanceText = prevDistance + " steps";
                                        double outlineOffset = 1.5; // Slightly larger offset for larger text
                                        gc.setFont(new Font("Arial", 18)); // Larger font size for distance
                                        gc.setFill(Color.rgb(0, 0, 0)); // Black outline
                                        gc.fillText(distanceText, prevMidX + 10 - outlineOffset, prevMidY - outlineOffset);
                                        gc.fillText(distanceText, prevMidX + 10 + outlineOffset, prevMidY - outlineOffset);
                                        gc.fillText(distanceText, prevMidX + 10 - outlineOffset, prevMidY + outlineOffset);
                                        gc.fillText(distanceText, prevMidX + 10 + outlineOffset, prevMidY + outlineOffset);
                                        gc.setFill(Color.rgb(255, 255, 255)); // White fill
                                        gc.fillText(distanceText, prevMidX + 10, prevMidY);
                                        gc.setFont(new Font("Arial", 12)); // Reset font size for other text
                                    }
                                }
                            }

                            gc.setStroke(Color.RED);
                            gc.setLineWidth(2.0);
                            double currentX = startCoords[0] + dx * currentStep;
                            double currentY = startCoords[1] + dy * currentStep;
                            drawArrow(gc, startCoords[0], startCoords[1], currentX, currentY);

                            // Draw the start node in green (visited) and the end node in blue (current)
                            gc.setFill(Color.rgb(0, 255, 0));
                            gc.fillOval(startCoords[0] - 5, startCoords[1] - 5, 10, 10);
                            gc.setFill(Color.BLUE);
                            gc.fillOval(currentX - 5, currentY - 5, 10, 10);

                            // Draw current segment distance with black outline and white fill
                            String distanceText = distance + " steps";
                            double outlineOffset = 1.5; // Slightly larger offset for larger text
                            gc.setFont(new Font("Arial", 18)); // Larger font size for distance
                            gc.setFill(Color.rgb(0, 0, 0)); // Black outline
                            gc.fillText(distanceText, midX + 10 - outlineOffset, midY - outlineOffset);
                            gc.fillText(distanceText, midX + 10 + outlineOffset, midY - outlineOffset);
                            gc.fillText(distanceText, midX + 10 - outlineOffset, midY + outlineOffset);
                            gc.fillText(distanceText, midX + 10 + outlineOffset, midY + outlineOffset);
                            gc.setFill(Color.rgb(255, 255, 255)); // White fill
                            gc.fillText(distanceText, midX + 10, midY);
                            gc.setFont(new Font("Arial", 12)); // Reset font size for other text

                            if (currentStep == steps) {
                                visitedNodes.add(normalizedEndStall);
                                gc.setStroke(Color.BLUE);
                                drawArrow(gc, startCoords[0], startCoords[1], endCoords[0], endCoords[1]);
                                // Redraw distance with black outline and white fill for completed segment
                                gc.setFont(new Font("Arial", 18)); // Larger font size for distance
                                gc.setFill(Color.rgb(0, 0, 0)); // Black outline
                                gc.fillText(distanceText, midX + 10 - outlineOffset, midY - outlineOffset);
                                gc.fillText(distanceText, midX + 10 + outlineOffset, midY - outlineOffset);
                                gc.fillText(distanceText, midX + 10 - outlineOffset, midY + outlineOffset);
                                gc.fillText(distanceText, midX + 10 + outlineOffset, midY + outlineOffset);
                                gc.setFill(Color.rgb(255, 255, 255)); // White fill
                                gc.fillText(distanceText, midX + 10, midY);
                                gc.setFont(new Font("Arial", 12)); // Reset font size for other text
                                System.out.println("Completed segment: " + startStall + " to " + endStall);
                            }
                        }
                );
                timeline.getKeyFrames().add(moveFrame);
            }
        }

        if (timeline.getKeyFrames().isEmpty()) {
            System.out.println("No valid segments to animate. Drawing static path instead.");
            gc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
            redrawNodesAndRatings(normalizedPath);
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2.0);
            for (int i = 0; i < path.length - 1; i++) {
                double[] start = stallCoordinates.get(normalizedPath[i]);
                double[] end = stallCoordinates.get(normalizedPath[i + 1]);
                if (start != null && end != null) {
                    int startIdx = stallToIndex.getOrDefault(path[i], -1);
                    int endIdx = stallToIndex.getOrDefault(path[i + 1], -1);
                    if (startIdx != -1 && endIdx != -1 &&
                            startIdx < distanceMatrix.length && endIdx < distanceMatrix[0].length) {
                        int distance = distanceMatrix[startIdx][endIdx];
                        drawArrow(gc, start[0], start[1], end[0], end[1]);
                        double midX = (start[0] + end[0]) / 2;
                        double midY = (start[1] + end[1]) / 2;
                        // Draw distance with black outline and white fill
                        String distanceText = distance + " steps";
                        double outlineOffset = 1.5; // Slightly larger offset for larger text
                        gc.setFont(new Font("Arial", 18)); // Larger font size for distance
                        gc.setFill(Color.rgb(0, 0, 0)); // Black outline
                        gc.fillText(distanceText, midX + 10 - outlineOffset, midY - outlineOffset);
                        gc.fillText(distanceText, midX + 10 + outlineOffset, midY - outlineOffset);
                        gc.fillText(distanceText, midX + 10 - outlineOffset, midY + outlineOffset);
                        gc.fillText(distanceText, midX + 10 + outlineOffset, midY + outlineOffset);
                        gc.setFill(Color.rgb(255, 255, 255)); // White fill
                        gc.fillText(distanceText, midX + 10, midY);
                        gc.setFont(new Font("Arial", 12)); // Reset font size for other text
                    }
                }
            }
        } else {
            System.out.println("Starting animation with " + timeline.getKeyFrames().size() + " keyframes.");
            timeline.setCycleCount(1);
            timeline.play();
        }
    }

    private void redrawNodesAndRatings(String[] normalizedPath) {
        GraphicsContext gc = pathCanvas.getGraphicsContext2D();
        if (gc == null) {
            System.out.println("Error: GraphicsContext is null in redrawNodesAndRatings.");
            return;
        }
        for (String stall : normalizedPath) {
            double[] coords = stallCoordinates.get(stall);
            if (coords != null) {
                // Draw the node: green if visited, red if unvisited
                gc.setFill(visitedNodes.contains(stall) ? Color.rgb(57, 255, 20) : Color.RED);
                gc.fillOval(coords[0] - 5, coords[1] - 5, 10, 10);

                // Draw the rating with a black outline and white fill, followed by a star
                Double rating = stallRatings.get(stall);
                if (rating != null && rating > 0) {
                    String ratingText = String.format("%.1f", rating);
                    String starText = "★"; // Unicode star symbol
                    double textX = coords[0] + 15;
                    double textY = coords[1];
                    double outlineOffset = 1.5; // Slightly larger offset for larger text

                    // Estimate the width of the rating text to position the star
                    double ratingTextWidth = ratingText.length() * 10; // Adjusted for larger font size
                    double starX = textX + ratingTextWidth + 2; // Small gap between rating and star

                    // Draw black outline for the rating text
                    gc.setFont(new Font("Arial", 18)); // Larger font size for rating
                    gc.setFill(Color.BLACK);
                    gc.fillText(ratingText, textX - outlineOffset, textY - outlineOffset);
                    gc.fillText(ratingText, textX + outlineOffset, textY - outlineOffset);
                    gc.fillText(ratingText, textX - outlineOffset, textY + outlineOffset);
                    gc.fillText(ratingText, textX + outlineOffset, textY + outlineOffset);

                    // Draw black outline for the star
                    gc.fillText(starText, starX - outlineOffset, textY - outlineOffset);
                    gc.fillText(starText, starX + outlineOffset, textY - outlineOffset);
                    gc.fillText(starText, starX - outlineOffset, textY + outlineOffset);
                    gc.fillText(starText, starX + outlineOffset, textY + outlineOffset);

                    // Draw white rating text and star in the center
                    gc.setFill(Color.rgb(57, 255, 20));
                    gc.fillText(ratingText, textX, textY);
                    gc.fillText(starText, starX, textY);
                    gc.setFont(new Font("Arial", 12)); // Reset font size for other text
                }
            } else {
                System.out.println("Node not drawn: Missing coordinates for " + stall);
            }
        }
    }

    private void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        double arrowLength = 10.0;
        double arrowAngle = Math.toRadians(30);

        gc.strokeLine(startX, startY, endX, endY);

        double dx = endX - startX;
        double dy = endY - startY;
        double angle = Math.atan2(dy, dx);

        double x1 = endX - arrowLength * Math.cos(angle - arrowAngle);
        double y1 = endY - arrowLength * Math.sin(angle - arrowAngle);
        double x2 = endX - arrowLength * Math.cos(angle + arrowAngle);
        double y2 = endY - arrowLength * Math.sin(angle + arrowAngle);

        gc.strokeLine(endX, endY, x1, y1);
        gc.strokeLine(endX, endY, x2, y2);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("initialize called.");
        setupBackgroundAnimation();  // Ensure the animated background is set up
        initializeCoordinates();
        initializeStallNameMapping();
        System.out.println("Canvas size: [" + pathCanvas.getWidth() + ", " + pathCanvas.getHeight() + "]");

        originalY = backButton.getLayoutY();

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), backButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), backButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), backButton);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            backButton.setLayoutY(originalY);
            isBouncing = false;
            pulse.play();
            fade.play();
        });

        // Play bounce animation on hover with debounce
        backButton.setOnMouseEntered(event -> {
            long currentTime = System.currentTimeMillis();
            if (!isBouncing && (currentTime - lastHoverTime) >= DEBOUNCE_TIME) {
                isBouncing = true;
                lastHoverTime = currentTime;
                pulse.stop();
                fade.stop();
                bounce.stop();
                backButton.setLayoutY(originalY);
                bounce.playFromStart();
            }
        });

        // Resume pulsing and fading after mouse exits
        backButton.setOnMouseExited(event -> {
            if (!isBouncing) {
                pulse.play();
                fade.play();
            }
        });
    }
}