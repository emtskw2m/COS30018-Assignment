package GA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UberSalesmensch {
    private int numberOfCities;
    private int[][] travelPrices;
    private List<SalesmanGenome> population;
    private int populationSize = 50;
    private int generations = 100;

    public UberSalesmensch(int numberOfCities, int[][] travelPrices) {
        this.numberOfCities = numberOfCities;
        this.travelPrices = travelPrices;
        this.population = initialPopulation();
    }

    private List<SalesmanGenome> initialPopulation() {
        List<SalesmanGenome> initialPopulation = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            initialPopulation.add(new SalesmanGenome(numberOfCities, travelPrices, null));
        }
        return initialPopulation;
    }

    public SalesmanGenome optimize() {
        for (int generation = 0; generation < generations; generation++) {
            Collections.sort(population);
            population = generateNewPopulation();
        }
        return population.get(0); // Return the best genome
    }

    private List<SalesmanGenome> generateNewPopulation() {
        List<SalesmanGenome> newPopulation = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            SalesmanGenome parent1 = population.get(i);
            SalesmanGenome parent2 = population.get((i + 1) % populationSize);
            newPopulation.add(crossover(parent1, parent2));
        }
        return newPopulation;
    }

    private SalesmanGenome crossover(SalesmanGenome parent1, SalesmanGenome parent2) {
        List<Integer> newRoute = new ArrayList<>(parent1.genome.subList(0, numberOfCities / 2));
        newRoute.addAll(parent2.genome.subList(numberOfCities / 2, numberOfCities));
        Collections.shuffle(newRoute);
        return new SalesmanGenome(numberOfCities, travelPrices, newRoute);
    }
}
