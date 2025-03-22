package com.example.daaproject;

import java.util.*;

public class DistanceMatrixInitializer {
    private Map<String, Integer> stallToIndex;
    private int[][] distanceMatrix;

    public DistanceMatrixInitializer() {
        stallToIndex = new HashMap<>();
        initializeDistanceMatrix();
    }

    private void initializeDistanceMatrix() {
        List<String> stalls = Arrays.asList(
                "Starting Point", "Allami Shawarma", "Allami Pasta", "Home of Rice Bowl Toppings",
                "Siomai Rice Food Hub", "K-Corner Eumsik", "Dreamy Rice Meals", "Mrs. Katsu-Good",
                "Allami-King Overload", "The Red Meat", "CID Sisig", "Gustatory Delights",
                "House of Sisig", "TeaXplode", "Caleigha's", "Shawarma Dealicious", "Dr. Lemon",
                "39ers", "Savor & Sip", "Ka-Pangga", "JM Cabuhat Fresh Buko", "Silogang", "Just Taste It"
        );
        for (int i = 0; i < stalls.size(); i++) {
            stallToIndex.put(stalls.get(i), i);
        }
        distanceMatrix = new int[stalls.size()][stalls.size()];
        for (int[] row : distanceMatrix) {
            Arrays.fill(row, 1000); // Default large distance
        }
        for (int i = 0; i < stalls.size(); i++) {
            distanceMatrix[i][i] = 0; // Distance to self is 0
        }
        //starting point
            setDistance("Starting Point", "Allami Shawarma", 8);
            setDistance("Starting Point", "Allami Pasta", 13);
            setDistance("Starting Point", "Home of Rice Bowl Toppings", 28);
            setDistance("Starting Point", "Siomai Rice Food Hub", 34);
            setDistance("Starting Point", "K-Corner Eumsik", 39);
            setDistance("Starting Point", "Dreamy Rice Meals", 46);
            setDistance("Starting Point", "Mrs. Katsu-Good", 56);
            setDistance("Starting Point", "Allami-King Overload", 56);
            setDistance("Starting Point", "The Red Meat", 46);
            setDistance("Starting Point", "CID Sisig", 39);
            setDistance("Starting Point", "Gustatory Delights", 34);
            setDistance("Starting Point", "House of Sisig", 23);
            setDistance("Starting Point", "TeaXplode", 13);
            setDistance("Starting Point", "Caleigha's", 10);
            setDistance("Starting Point", "Shawarma Dealicious", 7);
            setDistance("Starting Point", "Dr. Lemon", 23);
            setDistance("Starting Point", "39ers", 51);
            setDistance("Starting Point", "Ka-Pangga", 18);
            setDistance("Starting Point", "JM Cabuhat Fresh Buko", 23);
            setDistance("Starting Point", "Silogang", 51);
            setDistance("Starting Point", "Just Taste It", 31);
            setDistance("Starting Point", "Savor & Sip", 43);

            //To dessert
            setDistance("Allami Shawarma", "CID Sisig", 27);
            setDistance("Allami Pasta", "CID Sisig", 23);
            setDistance("Home of Rice Bowl Toppings", "CID Sisig", 15);
            setDistance("Siomai Rice Food Hub", "CID Sisig", 12);
            setDistance("K-Corner Eumsik", "CID Sisig", 9);
            setDistance("Dreamy Rice Meals", "CID Sisig", 12);
            setDistance("Silogang", "CID Sisig", 15);
            setDistance("Mrs. Katsu-Good", "CID Sisig", 18);
            setDistance("Allami-King Overload", "CID Sisig", 16);
            setDistance("The Red Meat", "CID Sisig", 5);
            setDistance("CID Sisig", "CID Sisig", 0);
            setDistance("Gustatory Delights", "CID Sisig", 5);
            setDistance("House of Sisig", "CID Sisig", 15);
            setDistance("TeaXplode", "CID Sisig", 25);
            setDistance("Caleigha's", "CID Sisig", 30);
            setDistance("Shawarma Dealicious", "CID Sisig", 35);
            setDistance("Just Taste It", "CID Sisig", 15);

            setDistance("Allami Shawarma", "Gustatory Delights", 23);
            setDistance("Allami Pasta", "Gustatory Delights", 19);
            setDistance("Home of Rice Bowl Toppings", "Gustatory Delights", 12);
            setDistance("Siomai Rice Food Hub", "Gustatory Delights", 9);
            setDistance("K-Corner Eumsik", "Gustatory Delights", 12);
            setDistance("Dreamy Rice Meals", "Gustatory Delights", 15);
            setDistance("Silogang", "Gustatory Delights", 18);
            setDistance("Mrs. Katsu-Good", "Gustatory Delights", 21);
            setDistance("Allami-King Overload", "Gustatory Delights", 20);
            setDistance("The Red Meat", "Gustatory Delights", 10);
            setDistance("CID Sisig", "Gustatory Delights", 5);
            setDistance("Gustatory Delights", "Gustatory Delights", 0);
            setDistance("House of Sisig", "Gustatory Delights", 10);
            setDistance("TeaXplode", "Gustatory Delights", 20);
            setDistance("Caleigha's", "Gustatory Delights", 25);
            setDistance("Shawarma Dealicious", "Gustatory Delights", 30);
            setDistance("Just Taste It", "Gustatory Delights", 12);

            setDistance("Allami Shawarma", "Savor & Sip", 19);
            setDistance("Allami Pasta", "Savor & Sip", 15);
            setDistance("Home of Rice Bowl Toppings", "Savor & Sip", 9);
            setDistance("Siomai Rice Food Hub", "Savor & Sip", 12);
            setDistance("K-Corner Eumsik", "Savor & Sip", 15);
            setDistance("Dreamy Rice Meals", "Savor & Sip", 18);
            setDistance("Silogang", "Savor & Sip", 21);
            setDistance("Mrs. Katsu-Good", "Savor & Sip", 24);
            setDistance("Allami-King Overload", "Savor & Sip", 25);
            setDistance("The Red Meat", "Savor & Sip", 15);
            setDistance("CID Sisig", "Savor & Sip", 10);
            setDistance("Gustatory Delights", "Savor & Sip", 5);
            setDistance("House of Sisig", "Savor & Sip", 5);
            setDistance("TeaXplode", "Savor & Sip", 15);
            setDistance("Caleigha's", "Savor & Sip", 20);
            setDistance("Shawarma Dealicious", "Savor & Sip", 25);
            setDistance("Just Taste It", "Savor & Sip", 9);
            //Dessert to Drinks
            setDistance("CID Sisig", "Dr. Lemon", 17);
            setDistance("CID Sisig", "TeaXplode", 25);
            setDistance("CID Sisig", "39ers", 10);
            setDistance("CID Sisig", "Gustatory Delights", 5);
            setDistance("CID Sisig", "Savor & Sip", 10);
            setDistance("CID Sisig", "CID Sisig", 0);
            setDistance("CID Sisig", "Siomai Rice Food Hub", 12);
            setDistance("CID Sisig", "Ka-Pangga", 19);
            setDistance("CID Sisig", "JM Cabuhat Fresh Buko", 20);
            setDistance("Savor & Sip", "Dr. Lemon", 12);
            setDistance("Savor & Sip", "TeaXplode", 15);
            setDistance("Savor & Sip", "39ers", 20);
            setDistance("Savor & Sip", "Gustatory Delights", 5);
            setDistance("Savor & Sip", "Savor & Sip", 0);
            setDistance("Savor & Sip", "CID Sisig", 10);
            setDistance("Savor & Sip", "Siomai Rice Food Hub", 12);
            setDistance("Savor & Sip", "Ka-Pangga", 12);
            setDistance("Savor & Sip", "JM Cabuhat Fresh Buko", 10);
            setDistance("Gustatory Delights", "Dr. Lemon", 14);
            setDistance("Gustatory Delights", "TeaXplode", 20);
            setDistance("Gustatory Delights", "39ers", 15);
            setDistance("Gustatory Delights", "Gustatory Delights", 0);
            setDistance("Gustatory Delights", "Savor & Sip", 5);
            setDistance("Gustatory Delights", "CID Sisig", 5);
            setDistance("Gustatory Delights", "Siomai Rice Food Hub", 9);
            setDistance("Gustatory Delights", "Ka-Pangga", 15);
            setDistance("Gustatory Delights", "JM Cabuhat Fresh Buko", 15);
            setDistance("CID Sisig", "Caleigha's", 16); // Added
            setDistance("CID Sisig", "Shawarma Dealicious", 19); // Added

            // Optional: Add distances between drink stalls for a complete matrix
            setDistance("Dr. Lemon", "TeaXplode", 55);
            setDistance("Dr. Lemon", "39ers", 58);
            setDistance("Dr. Lemon", "Ka-Pangga", 3);
            setDistance("Dr. Lemon", "JM Cabuhat Fresh Buko", 52);
            setDistance("Dr. Lemon", "Caleigha's", 58);
            setDistance("Dr. Lemon", "Shawarma Dealicious", 60);
            setDistance("TeaXplode", "39ers", 20);
            setDistance("TeaXplode", "Ka-Pangga", 55);
            setDistance("TeaXplode", "JM Cabuhat Fresh Buko", 3);
            setDistance("TeaXplode", "Caleigha's", 3);
            setDistance("TeaXplode", "Shawarma Dealicious", 5);
            setDistance("39ers", "Ka-Pangga", 58);
            setDistance("39ers", "JM Cabuhat Fresh Buko", 17);
            setDistance("39ers", "Caleigha's", 22);
            setDistance("39ers", "Shawarma Dealicious", 25);
            setDistance("Ka-Pangga", "JM Cabuhat Fresh Buko", 55);
            setDistance("Ka-Pangga", "Caleigha's", 58);
            setDistance("Ka-Pangga", "Shawarma Dealicious", 60);
            setDistance("JM Cabuhat Fresh Buko", "Caleigha's", 5);
            setDistance("JM Cabuhat Fresh Buko", "Shawarma Dealicious", 8);
            setDistance("Caleigha's", "Shawarma Dealicious", 3);
    }

    private void setDistance(String stall1, String stall2, int distance) {
        int i = stallToIndex.get(stall1);
        int j = stallToIndex.get(stall2);
        distanceMatrix[i][j] = distance;
        distanceMatrix[j][i] = distance; // Symmetric
    }

    public Map<String, Integer> getStallToIndex() {
        return stallToIndex;
    }

    public int[][] getDistanceMatrix() {
        return distanceMatrix;
    }
}