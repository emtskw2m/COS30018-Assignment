package ACO;

import assets.CityMap;
import assets.Station;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Collectors;


public class AntColonyOptimization {

    private double c = 1.0;  // Initial trail value
    private double alpha = 1;  // Pheromone importance
    private double beta = 5;  // Distance priority
    private double evaporation = 0.5;  // Evaporation rate
    private double Q = 500;  // Pheromone left on the trail per ant
    private double antFactor = 0.8;  // Fraction of ants to places
    private double randomFactor = 0.01;  // Randomness factor

    private int maxIterations = 1000;
    private List<String> stationNames;  // Add this line
    private int numberOfCities;
    private int numberOfAnts;
    private double[][] graph;  // Distance between cities
    private double[][] trails;  // Pheromone on path
    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private double[] probabilities;

    private int currentIndex;

    private int[] bestTourOrder;
    private double bestTourLength;
    private int spIndex; // Store the index of 'SP'

    public AntColonyOptimization(CityMap cityMap, List<String> selectedStationNames) {
        if (!selectedStationNames.contains("SP")) {
            selectedStationNames.add(0, "SP");
        }
        this.stationNames = new ArrayList<>(selectedStationNames);
        this.spIndex = this.stationNames.indexOf("SP"); // Set the index of 'SP'

        this.numberOfCities = this.stationNames.size();
        this.graph = new double[numberOfCities][numberOfCities];
        this.initializeDistances(cityMap, this.stationNames);
        this.numberOfAnts = (int) (numberOfCities * antFactor);

        this.trails = new double[numberOfCities][numberOfCities];
        this.probabilities = new double[numberOfCities];
        for (int i = 0; i < numberOfAnts; i++) {
            ants.add(new Ant(numberOfCities));
        }
        this.bestTourOrder = new int[numberOfCities];
        this.bestTourLength = Double.MAX_VALUE;
    }

    private void initializeDistances(CityMap cityMap, List<String> selectedStationNames) {
        for (int i = 0; i < numberOfCities; i++) {
            for (int j = 0; j < numberOfCities; j++) {
                if (i == j) {
                    graph[i][j] = 0;
                } else {
                    Station station1 = cityMap.getStations().get(selectedStationNames.get(i));
                    Station station2 = cityMap.getStations().get(selectedStationNames.get(j));
                    graph[i][j] = calculateDistance(station1, station2);
                }
            }
        }
    }

    private double calculateDistance(Station station1, Station station2) {
        return Math.sqrt(Math.pow(station1.getX() - station2.getX(), 2) + Math.pow(station1.getY() - station2.getY(), 2));
    }

    public void startAntOptimization() {
        IntStream.rangeClosed(1, 3)
            .forEach(i -> {
                System.out.println("Attempt #" + i);
                solve();
            });
    }

    public void displayResults() {
        if (bestTourOrder != null) {
            List<String> tourNames = Arrays.stream(bestTourOrder)
                                           .mapToObj(index -> stationNames.get(index))
                                           .collect(Collectors.toList());
            System.out.println("Best tour length: " + bestTourLength);
            System.out.println("Best tour order: " + tourNames);
        } else {
            System.out.println("No valid tour found.");
        }
    }
    
    public void solve() {
        setupAnts();
        clearTrails();
        for (int i = 0; i < maxIterations; i++) {
            moveAnts();
            updateTrails();
            updateBest();
        }
        displayResults();  // Replace the previous print statements with this method call
    }

    private void setupAnts() {
        IntStream.range(0, numberOfAnts).forEach(i -> {
            ants.forEach(ant -> {
                ant.clear();
                ant.visitCity(-1, spIndex); // Start at 'SP'
            });
        });
        currentIndex = 0;
    }

    private void moveAnts() {
        IntStream.range(currentIndex, numberOfCities).forEach(i -> {
            ants.forEach(ant -> {
                if (currentIndex < numberOfCities - 1) {
                    ant.visitCity(currentIndex, selectNextCity(ant));
                } else {
                    // Ensure the last move is back to 'SP'
                    ant.visitCity(currentIndex, spIndex);
                }
            });
            currentIndex++;
        });
    }

    private int selectNextCity(Ant ant) {
        int t = random.nextInt(numberOfCities - currentIndex);
        if (random.nextDouble() < randomFactor) {
            return IntStream.range(0, numberOfCities)
                .filter(i -> i == t && !ant.visited(i))
                .findFirst().orElseThrow(() -> new RuntimeException("No unvisited cities left"));
        }
        calculateProbabilities(ant);
        double r = random.nextDouble();
        double total = 0;
        for (int i = 0; i < numberOfCities; i++) {
            total += probabilities[i];
            if (total >= r) {
                return i;
            }
        }
        throw new RuntimeException("There are no other cities");
    }

    private void calculateProbabilities(Ant ant) {
        int i = ant.trail[currentIndex];
        double pheromone = 0.0;
        for (int l = 0; l < numberOfCities; l++) {
            if (!ant.visited(l)) {
                pheromone += Math.pow(trails[i][l], alpha) * Math.pow(1.0 / graph[i][l], beta);
            }
        }
        for (int j = 0; j < numberOfCities; j++) {
            if (ant.visited(j)) {
                probabilities[j] = 0.0;
            } else {
                double numerator = Math.pow(trails[i][j], alpha) * Math.pow(1.0 / graph[i][j], beta);
                probabilities[j] = numerator / pheromone;
            }
        }
    }

    private void updateTrails() {
        for (int i = 0; i < numberOfCities; i++) {
            for (int j = 0; j < numberOfCities; j++) {
                trails[i][j] *= evaporation;
            }
        }
        ants.forEach(ant -> {
            double contribution = Q / ant.trailLength(graph);
            for (int i = 0; i < numberOfCities - 1; i++) {
                trails[ant.trail[i]][ant.trail[i + 1]] += contribution;
            }
            trails[ant.trail[numberOfCities - 1]][ant.trail[0]] += contribution;
        });
    }

    private void updateBest() {
        for (Ant a : ants) {
            double currentLength = a.trailLength(graph);
            if (bestTourOrder == null || currentLength < bestTourLength) {
                bestTourLength = currentLength;
                bestTourOrder = a.trail.clone();
                if (bestTourOrder[0] != spIndex) {
                    rotateTourToStartAtSP();
                }
            }
        }
    }
    
    private void rotateTourToStartAtSP() {
        int start = -1;
        for (int i = 0; i < bestTourOrder.length; i++) {
            if (bestTourOrder[i] == spIndex) {
                start = i;
                break;
            }
        }
        if (start > 0) {
            int[] newTour = new int[bestTourOrder.length];
            System.arraycopy(bestTourOrder, start, newTour, 0, bestTourOrder.length - start);
            System.arraycopy(bestTourOrder, 0, newTour, bestTourOrder.length - start, start);
            System.arraycopy(newTour, 0, bestTourOrder, 0, bestTourOrder.length);
        }
    }

    private void clearTrails() {
        for (int i = 0; i < numberOfCities; i++) {
            Arrays.fill(trails[i], c);
        }
    }
}
