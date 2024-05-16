package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

import assets.CityMap;
import assets.MapFrame;
import GA.SalesmanGenome;
import GA.UberSalesmensch;

public class MasterRoutingAgent extends Agent {
    private Map<AID, Integer> availableAgentCapacities = new HashMap<>();
    private AtomicInteger receivedCapacities;
    private int expectedNumberOfDeliveringAgents = 2; // Adjust based on the number of DeliveringAgents
    private int storedPackageQuantity = 0;
    private CityMap cityMap;

    protected void setup() {
        System.out.println("MasterRoutingAgent is ready.");

        // Launch the map visualization in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            cityMap = new CityMap();
            initializeCityMap();
            new MapFrame(cityMap, this);
        });

        receivedCapacities = new AtomicInteger(0);

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Capacity:")) {
                        String[] parts = msg.getContent().split(";");
                        int capacity = Integer.parseInt(parts[0].substring(9));
                        String status = parts[1].substring(7);

                        if (status.equals("available")) {
                            availableAgentCapacities.put(msg.getSender(), capacity);
                        } else {
                            System.out.println(msg.getSender().getLocalName() + " is busy and will not be considered for this assignment.");
                            availableAgentCapacities.remove(msg.getSender());
                        }

                        receivedCapacities.incrementAndGet();

                        // When all responses have been received, assign the package
                        if (receivedCapacities.get() == expectedNumberOfDeliveringAgents) {
                            handlePackageAssignment(storedPackageQuantity);
                        }
                    } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("PackageQuantity:")) {
                        int quantity = Integer.parseInt(msg.getContent().substring(16));
                        storedPackageQuantity = quantity;
                        requestAgentCapacities();
                        

                    }
                } else {
                    block();
                }
            }
        });
    }

    private void initializeCityMap() {
        cityMap.connectStations("SP", "A1", 120);
        cityMap.connectStations("SP", "H4", 180);
        cityMap.connectStations("SP", "H3", 120);
        cityMap.connectStations("SP", "H7", 120);
        cityMap.connectStations("H1", "H7", 200);
        cityMap.connectStations("F1", "H3", 180);
        cityMap.connectStations("A1", "A2", 150);
        cityMap.connectStations("H6", "F2", 250);
        cityMap.connectStations("SP", "F3", 300);
        cityMap.connectStations("H1", "H2", 180);
        cityMap.connectStations("H1", "H6", 50);
        cityMap.connectStations("F2", "F3", 120);
        cityMap.connectStations("H4", "H5", 120);
        cityMap.connectStations("H5", "F1", 100);
        cityMap.connectStations("H3", "H5", 40);
    }

    private void requestAgentCapacities() {
        ACLMessage capacityRequest = new ACLMessage(ACLMessage.REQUEST);
        capacityRequest.addReceiver(new AID("deliver1", AID.ISLOCALNAME));
        capacityRequest.addReceiver(new AID("deliver2", AID.ISLOCALNAME));
        capacityRequest.addReceiver(new AID("deliver3", AID.ISLOCALNAME));
        capacityRequest.addReceiver(new AID("deliver4", AID.ISLOCALNAME));
        capacityRequest.setContent("RequestCapacity");
        send(capacityRequest);
        System.out.println("MasterRoutingAgent requested capacities from all DeliveringAgents.");
    }

    private void handlePackageAssignment(int quantity) {
        int[][] travelPrices = createTravelPriceMatrix();

        UberSalesmensch ga = new UberSalesmensch(cityMap.getStations().size(), travelPrices);
        SalesmanGenome bestRoute = ga.optimize();

        System.out.println("Optimal Route: " + bestRoute.genome + " with travel cost: " + bestRoute.fitness);

        // Assuming only one agent is used for simplicity, distribute the quantity to the optimal route
        assignPackage(bestRoute.genome, quantity);

        // Reset capacities tracking for future assignments
        receivedCapacities.set(0);
        availableAgentCapacities.clear();
    }

    private int[][] createTravelPriceMatrix() {
        List<String> stationNames = new ArrayList<>(cityMap.getStations().keySet());
        int n = stationNames.size();
        int[][] matrix = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    String station1 = stationNames.get(i);
                    String station2 = stationNames.get(j);
                    int distance = cityMap.calculateRoundTripDistance(station1, station2);
                    matrix[i][j] = distance == -1 ? Integer.MAX_VALUE : distance;
                }
            }
        }
        return matrix;
    }

    private void assignPackage(List<Integer> route, int quantity) {
        // For simplicity, we assume the first available agent handles the delivery along the calculated route
        Optional<AID> availableAgent = availableAgentCapacities.keySet().stream().findFirst();

        if (availableAgent.isPresent()) {
            AID agentAID = availableAgent.get();
            ACLMessage assignMsg = new ACLMessage(ACLMessage.REQUEST);
            assignMsg.addReceiver(agentAID);
            assignMsg.setContent(String.valueOf(quantity));
            send(assignMsg);
            System.out.println("DeliveryAgent(" + agentAID.getLocalName() + ") assigned " + quantity + " packages on route: " + route);
        } else {
            System.out.println("No available agent to handle the delivery.");
        }
    }
}
