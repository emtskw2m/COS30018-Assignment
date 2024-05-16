package GA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SalesmanGenome implements Comparable<SalesmanGenome> {
    public List<Integer> genome; // Represents a route as a list of indices corresponding to city positions
    int[][] travelPrices; // The travel cost matrix
    public int fitness;

    public SalesmanGenome(int numberOfCities, int[][] travelPrices, List<Integer> initialRoute) {
        this.travelPrices = travelPrices;
        if (initialRoute == null) {
            this.genome = randomSalesman(numberOfCities);
        } else {
            this.genome = new ArrayList<>(initialRoute); // Use given route
        }
        this.fitness = calculateFitness();
    }

    private List<Integer> randomSalesman(int numberOfCities) {
        List<Integer> route = new ArrayList<>();
        for (int i = 0; i < numberOfCities; i++) {
            route.add(i);
        }
        Collections.shuffle(route);
        return route;
    }

    public int calculateFitness() {
        int totalTravelCost = 0;
        for (int i = 0; i < genome.size() - 1; i++) {
            totalTravelCost += travelPrices[genome.get(i)][genome.get(i + 1)];
        }
        return totalTravelCost;
    }

    @Override
    public int compareTo(SalesmanGenome other) {
        return Integer.compare(this.fitness, other.fitness);
    }

    public String toString() {
        return "Route: " + genome + " | Fitness: " + fitness;
    }
}

