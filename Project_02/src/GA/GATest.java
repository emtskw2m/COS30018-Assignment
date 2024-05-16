package GA;

public class GATest {
    public static void main(String[] args) {
        // Define a sample travel prices matrix (symmetric for simplicity)
        int numberOfCities = 5;
        int[][] travelPrices = new int[][] {
            { 0, 2, 9, 10, 7 },
            { 1, 0, 6, 4, 3 },
            { 15, 7, 0, 8, 5 },
            { 6, 3, 12, 0, 10 },
            { 8, 5, 11, 7, 0 }
        };

        // Instantiate the genetic algorithm with the sample data
        UberSalesmensch ga = new UberSalesmensch(numberOfCities, travelPrices);

        // Run the optimization to get the best route
        SalesmanGenome bestRoute = ga.optimize();

        // Print the best route and its fitness
        System.out.println("Best route found by GA:");
        System.out.println(bestRoute);
    }
}
