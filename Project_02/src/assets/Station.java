package assets;

import java.util.HashMap;
import java.util.Map;

public class Station {
    private String name;
    private int x, y;
    private Map<Station, Integer> connectedStations; 

    public Station(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.connectedStations = new HashMap<>();
    }

    // Method to connect this station to another station with a specified distance
    public void connectStation(Station station, int distance) {
        connectedStations.put(station, distance);
    }

    // Getter for the name of the station
    public String getName() {
        return name;
    }

    // Getter for the x-coordinate of the station
    public int getX() {
        return x;
    }

    // Getter for the y-coordinate of the station
    public int getY() {
        return y;
    }

    // Getter for the connected stations with their respective distances
    public Map<Station, Integer> getConnections() {
        return connectedStations;
    }

    // Override of the toString method for better readability when printing a station
    public String toString() {
        return name + " (" + x + ", " + y + ")";
    }
}