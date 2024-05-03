package assets;

import java.util.HashMap;
import java.util.Map;

public class CityMap {
    private Map<String, Station> stations;

    public CityMap() {
        stations = new HashMap<>();
        // Original station setup
        stations.put("SP", new Station("SP", 250, 250));
        stations.put("H1", new Station("H1", 243, 435));
        stations.put("H2", new Station("H2", 21, 347));
        stations.put("H3", new Station("H3", 172, 227));
        stations.put("H4", new Station("H4", 90, 245));
        stations.put("H5", new Station("H5", 110, 183));
        stations.put("H6", new Station("H6", 284, 444));
        stations.put("H7", new Station("H7", 205, 324));
        stations.put("F1", new Station("F1", 172, 110));
        stations.put("F2", new Station("F2", 356, 486));
        stations.put("F3", new Station("F3", 510, 450));
        stations.put("A1", new Station("A1", 324, 271));
        stations.put("A2", new Station("A2", 453, 112));
    }

    public void connectStations(String station1Name, String station2Name, int distance) {
        Station station1 = stations.get(station1Name);
        Station station2 = stations.get(station2Name);
        if (station1 != null && station2 != null) {
            station1.connectStation(station2, distance);
            station2.connectStation(station1, distance); 
        }
    }

    public Map<String, Station> getStations() {
        return stations;
    }
    
    public int calculateRoundTripDistance(String start, String end) {
        Station startStation = stations.get(start);
        Station endStation = stations.get(end);
        if (startStation != null && endStation != null) {
            Integer distanceToEnd = startStation.getConnections().get(endStation);
            Integer distanceToStart = endStation.getConnections().get(startStation);
            if (distanceToEnd != null && distanceToStart != null) {
                return distanceToEnd + distanceToStart;
            }
        }
        return -1; 
    }

}
