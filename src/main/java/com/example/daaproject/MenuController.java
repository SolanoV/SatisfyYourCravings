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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MenuController extends BaseController implements Initializable {
    private Stage stage;
    @FXML private Button menuNext;

    @FXML private ChoiceBox<String> mainCourseChoice;
    @FXML private ChoiceBox<String> dessertChoice;
    @FXML private ChoiceBox<String> drinkChoice;
    @FXML private Button mainCourseToggle;
    @FXML private Button dessertToggle;
    @FXML private Button drinkToggle;
    @FXML private TableView<FoodItem> mainCourseTable;
    @FXML private TableView<FoodItem> dessertTable;
    @FXML private TableView<FoodItem> drinkTable;
    @FXML private Label mainCourseCount;
    @FXML private Label dessertCount;
    @FXML private Label drinkCount;
    @FXML private Label mainCourseError;
    @FXML private Label dessertError;
    @FXML private Label drinkError;

    private ScaleTransition pulse;
    private FadeTransition fade;
    private TranslateTransition bounce;
    private boolean isBouncing = false;
    private double originalY;
    private long lastHoverTime = 0;
    private static final long DEBOUNCE_TIME = 300;

    private String[] mainCourse = {"Chicken", "Pork", "Sausage", "Shawarma", "Beef", "Kimchi", "Fish", "Silog", "Noodles", "Pasta"};
    private String[] dessert = {"Ice Cream", "Graham"};
    private String[] drink = {"Lemonade", "Coffee", "Tea", "Float", "Juice", "Shake", "Yogurt"};
    private static final String DEFAULT_OPTION = "Choose your option...";

    private Map<String, List<FoodItem>> mainCourseOptions = new HashMap<>();
    private Map<String, List<FoodItem>> dessertOptions = new HashMap<>();
    private Map<String, List<FoodItem>> drinkOptions = new HashMap<>();
    private Map<String, Integer> stallToIndex;
    private int[][] distanceMatrix;
    private Map<String, Double> stallRatings = new HashMap<>();
    private static final String CSV_FILE = "src/main/resources/csv/FoodDrinkStallsRatings.csv";

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void switchToUnsortedCombinations() throws IOException {
        boolean canProceed = true;

        if (mainCourseChoice.getValue().equals(DEFAULT_OPTION)) {
            mainCourseError.setVisible(true);
            mainCourseError.setManaged(true);
            canProceed = false;
        } else {
            mainCourseError.setVisible(false);
            mainCourseError.setManaged(false);
        }

        if (dessertChoice.getValue().equals(DEFAULT_OPTION)) {
            dessertError.setVisible(true);
            dessertError.setManaged(true);
            canProceed = false;
        } else {
            dessertError.setVisible(false);
            dessertError.setManaged(false);
        }

        if (drinkChoice.getValue().equals(DEFAULT_OPTION)) {
            drinkError.setVisible(true);
            drinkError.setManaged(true);
            canProceed = false;
        } else {
            drinkError.setVisible(false);
            drinkError.setManaged(false);
        }

        if (canProceed) {
            List<FoodCombination> combinations = getAllCombinations(
                    mainCourseChoice.getValue().toLowerCase(),
                    dessertChoice.getValue().toLowerCase(),
                    drinkChoice.getValue().toLowerCase()
            );

            FXMLLoader loader = new FXMLLoader(getClass().getResource("unsortedCombinations.fxml"));
            Parent root = loader.load();
            unsortedController controller = loader.getController();
            controller.setStage(stage);
            controller.setCombinations(combinations, stallToIndex, distanceMatrix);
            Scene scene = new Scene(root);
            scene.getStylesheets().add("file:src/main/resources/css/base.css");
            scene.getStylesheets().add("file:src/main/resources/css/unsortedCombinations.css");
            stage.setScene(scene);
        }
    }

    @FXML
    private void toggleMainCourseList() {
        if (!mainCourseChoice.getValue().equals(DEFAULT_OPTION)) {
            boolean isVisible = mainCourseTable.isVisible();
            mainCourseTable.setVisible(!isVisible);
            mainCourseTable.setManaged(!isVisible);
            mainCourseToggle.setText(isVisible ? "+" : "-");
            mainCourseCount.setVisible(!isVisible && mainCourseOptions.get(mainCourseChoice.getValue().toLowerCase()) != null);
            mainCourseCount.setManaged(!isVisible);
        }
    }

    @FXML
    private void toggleDessertList() {
        if (!dessertChoice.getValue().equals(DEFAULT_OPTION)) {
            boolean isVisible = dessertTable.isVisible();
            dessertTable.setVisible(!isVisible);
            dessertTable.setManaged(!isVisible);
            dessertToggle.setText(isVisible ? "+" : "-");
            dessertCount.setVisible(!isVisible && dessertOptions.get(dessertChoice.getValue().toLowerCase()) != null);
            dessertCount.setManaged(!isVisible);
        }
    }

    @FXML
    private void toggleDrinkList() {
        if (!drinkChoice.getValue().equals(DEFAULT_OPTION)) {
            boolean isVisible = drinkTable.isVisible();
            drinkTable.setVisible(!isVisible);
            drinkTable.setManaged(!isVisible);
            drinkToggle.setText(isVisible ? "+" : "-");
            drinkCount.setVisible(!isVisible && drinkOptions.get(drinkChoice.getValue().toLowerCase()) != null);
            drinkCount.setManaged(!isVisible);
        }
    }

    private void updateTableView(TableView<FoodItem> tableView, Map<String, List<FoodItem>> options, String selectedCategory, Label countLabel, Button toggleButton) {
        tableView.getItems().clear();

        if (selectedCategory.equals(DEFAULT_OPTION)) {
            List<FoodItem> allItems = new ArrayList<>();
            for (List<FoodItem> items : options.values()) {
                allItems.addAll(items);
            }

            if (!allItems.isEmpty()) {
                tableView.getItems().addAll(allItems);
                tableView.setVisible(true);
                tableView.setManaged(true);
                countLabel.setText(allItems.size() + " Food/Drink Items Found");
                countLabel.setVisible(true);
                countLabel.setManaged(true);
                toggleButton.setText("-");
            }
        } else {
            String key = selectedCategory.toLowerCase();
            List<FoodItem> items = options.get(key);
            if (items != null && !items.isEmpty()) {
                tableView.getItems().addAll(items);
                tableView.setVisible(true);
                tableView.setManaged(true);
                countLabel.setText(items.size() + " Food/Drink Items Found");
                countLabel.setVisible(true);
                countLabel.setManaged(true);
                toggleButton.setText("-");
            } else {
                tableView.setVisible(false);
                tableView.setManaged(false);
                countLabel.setVisible(false);
                countLabel.setManaged(false);
                toggleButton.setText("+");
            }
        }
    }

    private void loadRatingsFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String stallName = data[0].trim();
                double rating = Double.parseDouble(data[1].trim());
                stallRatings.put(stallName, rating);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void initializeDistanceData() {
        DistanceMatrixInitializer distanceInitializer = new DistanceMatrixInitializer();
        stallToIndex = distanceInitializer.getStallToIndex();
        distanceMatrix = distanceInitializer.getDistanceMatrix();
    }

    private int calculateTotalDistance(String stall1, String stall2, String stall3) {
        Integer startIdx = stallToIndex.get("Starting Point");
        Integer index1 = stallToIndex.get(stall1);
        Integer index2 = stallToIndex.get(stall2);
        Integer index3 = stallToIndex.get(stall3);

        if (startIdx == null || index1 == null || index2 == null || index3 == null) {
            return 100;
        }
        int d1 = distanceMatrix[startIdx][index1];
        int d2 = distanceMatrix[index1][index2];
        int d3 = distanceMatrix[index2][index3];
        int d4 = distanceMatrix[index3][startIdx];
        return d1 + d2 + d3 + d4;
    }

    private List<FoodCombination> getAllCombinations(String mainCategory, String dessertCategory, String drinkCategory) {
        List<FoodItem> mainCourses = mainCourseOptions.getOrDefault(mainCategory, new ArrayList<>());
        List<FoodItem> desserts = dessertOptions.getOrDefault(dessertCategory, new ArrayList<>());
        List<FoodItem> drinks = drinkOptions.getOrDefault(drinkCategory, new ArrayList<>());

        List<FoodCombination> combinations = new ArrayList<>();
        if (mainCourses.isEmpty() || desserts.isEmpty() || drinks.isEmpty()) {
            return combinations;
        }

        for (FoodItem main : mainCourses) {
            for (FoodItem dessert : desserts) {
                for (FoodItem drink : drinks) {
                    double totalPrice = main.getPrice() + dessert.getPrice() + drink.getPrice();
                    int totalDistance = calculateTotalDistance(main.getStall(), dessert.getStall(), drink.getStall());
                    combinations.add(new FoodCombination(main, dessert, drink, totalPrice, totalDistance));
                }
            }
        }
        combinations.sort(Comparator.comparingDouble(FoodCombination::getTotalDistance)
                .thenComparingDouble(FoodCombination::getTotalRating)
                .thenComparingDouble(FoodCombination::getTotalPrice));
        return combinations;
    }

    private void populateFoodOptions() {
        // Your existing populateFoodOptions() method remains unchanged
        mainCourseOptions.put("chicken", Arrays.asList(
                new FoodItem("Chicken Karaage", "Home of Rice Bowl Toppings", 85.0, stallRatings.getOrDefault("Home of Rice Bowl Toppings", 0.0)),
                new FoodItem("Chicken Katsu", "Home of Rice Bowl Toppings", 75.0, stallRatings.getOrDefault("Home of Rice Bowl Toppings", 0.0)),
                new FoodItem("Chicken Poppers", "K-Corner Eumsik", 65.0, stallRatings.getOrDefault("K-Corner Eumsik", 0.0)),
                new FoodItem("Chicken Fillet", "Dreamy Rice Meals", 69.0, stallRatings.getOrDefault("Dreamy Rice Meals", 0.0)),
                new FoodItem("Chicken Cordon Bleu", "Dreamy Rice Meals", 89.0, stallRatings.getOrDefault("Dreamy Rice Meals", 0.0)),
                new FoodItem("Chicken Katsu", "Silogang", 69.0, stallRatings.getOrDefault("Silogang", 0.0)),
                new FoodItem("Chicken Karaage Bites", "Silogang", 75.0, stallRatings.getOrDefault("Silogang", 0.0)),
                new FoodItem("Chicken Fillet", "Mrs. Katsu-Good", 50.0, stallRatings.getOrDefault("Mrs. Katsu-Good", 0.0)),
                new FoodItem("Chicken Fingers", "Mrs. Katsu-Good", 50.0, stallRatings.getOrDefault("Mrs. Katsu-Good", 0.0)),
                new FoodItem("Chicken Nuggets", "Mrs. Katsu-Good", 50.0, stallRatings.getOrDefault("Mrs. Katsu-Good", 0.0)),
                new FoodItem("Chicken Cordon Bleu", "Gustatory Delights", 60.0, stallRatings.getOrDefault("Gustatory Delights", 0.0)),
                new FoodItem("Chicken Poppers", "Gustatory Delights", 60.0, stallRatings.getOrDefault("Gustatory Delights", 0.0)),
                new FoodItem("Chicken Fillet", "TeaXplode", 60.0, stallRatings.getOrDefault("TeaXplode", 0.0)),
                new FoodItem("Chicken Katsu", "Caleigha's", 60.0, stallRatings.getOrDefault("Caleigha's", 0.0)),
                new FoodItem("Chicken Karaage", "Caleigha's", 50.0, stallRatings.getOrDefault("Caleigha's", 0.0)),
                new FoodItem("Chicken Fillet", "Allami-King Overload", 60.0, stallRatings.getOrDefault("Allami-King Overload", 0.0)),
                new FoodItem("Chicken Pastil", "Allami-King Overload", 50.0, stallRatings.getOrDefault("Allami-King Overload", 0.0))
        ));

        mainCourseOptions.put("pork", Arrays.asList(
                new FoodItem("Pork Tonkatsu", "Home of Rice Bowl Toppings", 75.0, stallRatings.getOrDefault("Home of Rice Bowl Toppings", 0.0)),
                new FoodItem("Pork Katsu", "Silogang", 69.0, stallRatings.getOrDefault("Silogang", 0.0)),
                new FoodItem("Pork Belly", "Mrs. Katsu-Good", 50.0, stallRatings.getOrDefault("Mrs. Katsu-Good", 0.0)),
                new FoodItem("Pork Shanghai", "Mrs. Katsu-Good", 50.0, stallRatings.getOrDefault("Mrs. Katsu-Good", 0.0)),
                new FoodItem("Pork Sisig", "Just Taste It", 49.0, stallRatings.getOrDefault("Just Taste It", 0.0)),
                new FoodItem("Pork Siomai", "Just Taste It", 30.0, stallRatings.getOrDefault("Just Taste It", 0.0)),
                new FoodItem("Pork Sisig", "CID Sisig", 59.0, stallRatings.getOrDefault("CID Sisig", 0.0)),
                new FoodItem("Pork Sisig", "TeaXplode", 55.0, stallRatings.getOrDefault("TeaXplode", 0.0)),
                new FoodItem("Pork Fillet", "TeaXplode", 60.0, stallRatings.getOrDefault("TeaXplode", 0.0)),
                new FoodItem("Pork Sisig", "House of Sisig", 60.0, stallRatings.getOrDefault("House of Sisig", 0.0))
        ));

        mainCourseOptions.put("sausage", Arrays.asList(
                new FoodItem("Hungarian Sausage", "Home of Rice Bowl Toppings", 60.0, stallRatings.getOrDefault("Home of Rice Bowl Toppings", 0.0)),
                new FoodItem("Hungarian Sausage", "Gustatory Delights", 75.0, stallRatings.getOrDefault("Gustatory Delights", 0.0)),
                new FoodItem("Sausage", "Gustatory Delights", 50.0, stallRatings.getOrDefault("Gustatory Delights", 0.0))
        ));

        mainCourseOptions.put("shawarma", Arrays.asList(
                new FoodItem("Shawarma Wrap", "The Red Meat", 50.0, stallRatings.getOrDefault("The Red Meat", 0.0)),
                new FoodItem("Shawarma Rice", "The Red Meat", 60.0, stallRatings.getOrDefault("The Red Meat", 0.0)),
                new FoodItem("Shawarma Wrap", "Shawarma Dealicious", 69.0, stallRatings.getOrDefault("Shawarma Dealicious", 0.0)),
                new FoodItem("Shawarma Rice", "Shawarma Dealicious", 79.0, stallRatings.getOrDefault("Shawarma Dealicious", 0.0)),
                new FoodItem("Shawarma Wrap", "Allami Shawarma", 60.0, stallRatings.getOrDefault("Allami Shawarma", 0.0)),
                new FoodItem("Shawarma Rice", "Allami Shawarma", 75.0, stallRatings.getOrDefault("Allami Shawarma", 0.0))
        ));

        mainCourseOptions.put("beef", Arrays.asList(
                new FoodItem("Beef Pepper Steak", "Home of Rice Bowl Toppings", 85.0, stallRatings.getOrDefault("Home of Rice Bowl Toppings", 0.0)),
                new FoodItem("Beef Burger Steak", "Dreamy Rice Meals", 59.0, stallRatings.getOrDefault("Dreamy Rice Meals", 0.0)),
                new FoodItem("Beef Burger", "TeaXplode", 70.0, stallRatings.getOrDefault("TeaXplode", 0.0))
        ));

        mainCourseOptions.put("kimchi", Arrays.asList(
                new FoodItem("Kimchi Rice Bowl Meal", "Caleigha's", 50.0, stallRatings.getOrDefault("Caleigha's", 0.0))
        ));

        mainCourseOptions.put("fish", Arrays.asList(
                new FoodItem("Fish Fillet", "Mrs. Katsu-Good", 50.0, stallRatings.getOrDefault("Mrs. Katsu-Good", 0.0))
        ));

        mainCourseOptions.put("silog", Arrays.asList(
                new FoodItem("ShangSilog", "Siomai Rice Food Hub", 45.0, stallRatings.getOrDefault("Siomai Rice Food Hub", 0.0)),
                new FoodItem("HotSilog", "Siomai Rice Food Hub", 50.0, stallRatings.getOrDefault("Siomai Rice Food Hub", 0.0)),
                new FoodItem("SioSilog", "Siomai Rice Food Hub", 45.0, stallRatings.getOrDefault("Siomai Rice Food Hub", 0.0)),
                new FoodItem("SpamSilog", "Siomai Rice Food Hub", 55.0, stallRatings.getOrDefault("Siomai Rice Food Hub", 0.0)),
                new FoodItem("Tosilog", "Silogang", 65.0, stallRatings.getOrDefault("Silogang", 0.0)),
                new FoodItem("Tapsilog", "Silogang", 85.0, stallRatings.getOrDefault("Silogang", 0.0)),
                new FoodItem("SpamSilog", "Silogang", 75.0, stallRatings.getOrDefault("Silogang", 0.0))
        ));

        mainCourseOptions.put("noodles", Arrays.asList(
                new FoodItem("Buldak Noodles", "K-Corner Eumsik", 80.0, stallRatings.getOrDefault("K-Corner Eumsik", 0.0)),
                new FoodItem("Shin Ramyun Noodles", "K-Corner Eumsik", 85.0, stallRatings.getOrDefault("K-Corner Eumsik", 0.0))
        ));

        mainCourseOptions.put("pasta", Arrays.asList(
                new FoodItem("Spiral Pasta", "Allami Pasta", 50.0, stallRatings.getOrDefault("Allami Pasta", 0.0)),
                new FoodItem("Fettucine Pasta", "Allami Pasta", 50.0, stallRatings.getOrDefault("Allami Pasta", 0.0)),
                new FoodItem("Bowtie Pasta", "Allami Pasta", 50.0, stallRatings.getOrDefault("Allami Pasta", 0.0)),
                new FoodItem("Penne Pasta", "Allami Pasta", 50.0, stallRatings.getOrDefault("Allami Pasta", 0.0))
        ));

        dessertOptions.put("ice cream", Arrays.asList(
                new FoodItem("Ice Cream Cone", "CID Sisig", 35.0, stallRatings.getOrDefault("CID Sisig", 0.0)),
                new FoodItem("Ice Cream Sundae", "CID Sisig", 59.0, stallRatings.getOrDefault("CID Sisig", 0.0))
        ));

        dessertOptions.put("graham", Arrays.asList(
                new FoodItem("Graham Bar", "Gustatory Delights", 65.0, stallRatings.getOrDefault("Gustatory Delights", 0.0)),
                new FoodItem("Mango Graham", "Savor & Sip", 60.0, stallRatings.getOrDefault("Savor & Sip", 0.0))
        ));

        drinkOptions.put("lemonade", Arrays.asList(
                new FoodItem("Lemonade", "Dr. Lemon", 50.0, stallRatings.getOrDefault("Dr. Lemon", 0.0)),
                new FoodItem("Lemonade", "TeaXplode", 40.0, stallRatings.getOrDefault("TeaXplode", 0.0))
        ));

        drinkOptions.put("coffee", Arrays.asList(
                new FoodItem("Coffee", "Dr. Lemon", 50.0, stallRatings.getOrDefault("Dr. Lemon", 0.0)),
                new FoodItem("Coffee", "39ers", 49.0, stallRatings.getOrDefault("39ers", 0.0)),
                new FoodItem("Coffee", "Gustatory Delights", 20.0, stallRatings.getOrDefault("Gustatory Delights", 0.0))
        ));

        drinkOptions.put("tea", Arrays.asList(
                new FoodItem("Fruit Tea", "39ers", 39.0, stallRatings.getOrDefault("39ers", 0.0)),
                new FoodItem("Milktea", "39ers", 39.0, stallRatings.getOrDefault("39ers", 0.0)),
                new FoodItem("Fruit Tea", "Savor & Sip", 55.0, stallRatings.getOrDefault("Savor & Sip", 0.0)),
                new FoodItem("Milktea", "TeaXplode", 80.0, stallRatings.getOrDefault("TeaXplode", 0.0))
        ));

        drinkOptions.put("float", Arrays.asList(
                new FoodItem("Coke Float", "CID Sisig", 35.0, stallRatings.getOrDefault("CID Sisig", 0.0))
        ));

        drinkOptions.put("juice", Arrays.asList(
                new FoodItem("Buko Juice", "Siomai Rice Food Hub", 35.0, stallRatings.getOrDefault("Siomai Rice Food Hub", 0.0)),
                new FoodItem("Flavored Juice", "Ka-Pangga", 30.0, stallRatings.getOrDefault("Ka-Pangga", 0.0)),
                new FoodItem("Buko Juice", "JM Cabuhat Fresh Buko", 30.0, stallRatings.getOrDefault("JM Cabuhat Fresh Buko", 0.0))
        ));

        drinkOptions.put("shake", Arrays.asList(
                new FoodItem("Shake", "CID Sisig", 60.0, stallRatings.getOrDefault("CID Sisig", 0.0)),
                new FoodItem("Shake", "Savor & Sip", 50.0, stallRatings.getOrDefault("Savor & Sip", 0.0))
        ));

        drinkOptions.put("yogurt", Arrays.asList(
                new FoodItem("Yogurt Drink", "39ers", 49.0, stallRatings.getOrDefault("39ers", 0.0)),
                new FoodItem("Yogurt Drink", "Savor & Sip", 55.0, stallRatings.getOrDefault("Savor & Sip", 0.0))
        ));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBackgroundAnimation();  // Ensure the animated background is set up

        loadRatingsFromCSV();
        initializeDistanceData();
        populateFoodOptions();

        mainCourseChoice.getItems().add(DEFAULT_OPTION);
        for (String item : mainCourse) {
            mainCourseChoice.getItems().add(capitalize(item));
        }
        dessertChoice.getItems().add(DEFAULT_OPTION);
        for (String item : dessert) {
            dessertChoice.getItems().add(capitalize(item));
        }
        drinkChoice.getItems().add(DEFAULT_OPTION);
        for (String item : drink) {
            drinkChoice.getItems().add(capitalize(item));
        }

        mainCourseChoice.getSelectionModel().select(DEFAULT_OPTION);
        dessertChoice.getSelectionModel().select(DEFAULT_OPTION);
        drinkChoice.getSelectionModel().select(DEFAULT_OPTION);

        updateTableView(mainCourseTable, mainCourseOptions, DEFAULT_OPTION, mainCourseCount, mainCourseToggle);
        updateTableView(dessertTable, dessertOptions, DEFAULT_OPTION, dessertCount, dessertToggle);
        updateTableView(drinkTable, drinkOptions, DEFAULT_OPTION, drinkCount, drinkToggle);

        mainCourseChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateTableView(mainCourseTable, mainCourseOptions, newValue, mainCourseCount, mainCourseToggle);
            mainCourseToggle.setDisable(false);
        });

        dessertChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateTableView(dessertTable, dessertOptions, newValue, dessertCount, dessertToggle);
            dessertToggle.setDisable(false);
        });

        drinkChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateTableView(drinkTable, drinkOptions, newValue, drinkCount, drinkToggle);
            drinkToggle.setDisable(false);
        });
        // Store the original Y position of the button
        originalY = menuNext.getLayoutY();

        // Pulsing animation
        pulse = new ScaleTransition(Duration.millis(1000), menuNext);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Fade animation
        fade = new FadeTransition(Duration.millis(1500), menuNext);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Bounce animation on hover
        bounce = new TranslateTransition(Duration.millis(300), menuNext);
        bounce.setByY(-10);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(event -> {
            menuNext.setLayoutY(originalY);
            isBouncing = false;
            pulse.play();
            fade.play();
        });

        // Play bounce animation on hover with debounce
        menuNext.setOnMouseEntered(event -> {
            long currentTime = System.currentTimeMillis();
            if (!isBouncing && (currentTime - lastHoverTime) >= DEBOUNCE_TIME) {
                isBouncing = true;
                lastHoverTime = currentTime;
                pulse.stop();
                fade.stop();
                bounce.stop();
                menuNext.setLayoutY(originalY);
                bounce.playFromStart();
            }
        });

        // Resume pulsing and fading after mouse exits
        menuNext.setOnMouseExited(event -> {
            if (!isBouncing) {
                pulse.play();
                fade.play();
            }
        });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static class FoodItem {
        private String name;
        private String stall;
        private double price;
        private double rating;

        public FoodItem(String name, String stall, double price, double rating) {
            this.name = name;
            this.stall = stall;
            this.price = price;
            this.rating = rating;
        }

        public String getName() { return name; }
        public String getStall() { return stall; }
        public double getPrice() { return price; }
        public double getRating() { return rating; }
    }

    public static class FoodCombination {
        private FoodItem mainCourse;
        private FoodItem dessert;
        private FoodItem drink;
        private double totalPrice;
        private int totalDistance;

        public FoodCombination(FoodItem mainCourse, FoodItem dessert, FoodItem drink, double totalPrice, int totalDistance) {
            this.mainCourse = mainCourse;
            this.dessert = dessert;
            this.drink = drink;
            this.totalPrice = totalPrice;
            this.totalDistance = totalDistance;
        }

        public FoodItem getMainCourse() { return mainCourse; }
        public FoodItem getDessert() { return dessert; }
        public FoodItem getDrink() { return drink; }
        public double getTotalPrice() { return totalPrice; }
        public int getTotalDistance() { return totalDistance; }
        public double getTotalRating() {
            return (mainCourse.getRating() + dessert.getRating() + drink.getRating()) / 3.0;
        }

        @Override
        public String toString() {
            return String.format("Main: %s (Stall: %s, Rating: %.1f), Dessert: %s (Stall: %s, Rating: %.1f), Drink: %s (Stall: %s, Rating: %.1f), Total Price: ₱ %.2f, Total Distance: %d steps, Total Rating: %.1f",
                    mainCourse.getName(), mainCourse.getStall(), mainCourse.getRating(),
                    dessert.getName(), dessert.getStall(), dessert.getRating(),
                    drink.getName(), drink.getStall(), drink.getRating(),
                    totalPrice, totalDistance, getTotalRating());
        }
    }
}