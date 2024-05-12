package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import assets.CityMap;
import assets.MapFrame;

public class MasterRoutingAgent extends Agent {
    private Map<AID, Integer> availableAgentCapacities = new HashMap<>();
    private AtomicInteger receivedCapacities;
    private Queue<PackageDetail> undeliveredPackages = new LinkedList<>();
    private int expectedNumberOfDeliveringAgents = 4; // Adjust based on the number of DeliveringAgents

    protected void setup() {
        System.out.println("MasterRoutingAgent is ready.");
        receivedCapacities = new AtomicInteger(0);

        SwingUtilities.invokeLater(() -> {
            CityMap cityMap = new CityMap();
            // Connect various stations as per the city map configuration
            new MapFrame(cityMap, this);
        });

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    handleMessages(msg);
                } else {
                    block();
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 10000) { // Check every 10 seconds
            protected void onTick() {
                if (!undeliveredPackages.isEmpty() && !availableAgentCapacities.isEmpty()) {
                    handlePackageAssignment(undeliveredPackages.poll());
                }
            }
        });
    }

    private void handleMessages(ACLMessage msg) {
        switch (msg.getPerformative()) {
            case ACLMessage.INFORM:
                if (msg.getContent().startsWith("Capacity:")) {
                    handleCapacityUpdate(msg);
                } else if (msg.getContent().startsWith("PackageQuantity:")) {
                    String[] parts = msg.getContent().split(";");
                    int quantity = Integer.parseInt(parts[0].substring(16));
                    String route = parts[1].substring(6);
                    requestAgentCapacities();
                    undeliveredPackages.add(new PackageDetail(quantity, route));
                    System.out.println("Received order for " + quantity + " packages to " + route);
                }
                break;
            case ACLMessage.REQUEST:
                // Additional requests handling
                break;
            default:
                // Handle other messages
                break;
        }
    }

    private void handleCapacityUpdate(ACLMessage msg) {
        String[] parts = msg.getContent().split(";");
        int capacity = Integer.parseInt(parts[0].substring(9));
        String status = parts[1].substring(7);
        if ("available".equals(status)) {
            availableAgentCapacities.put(msg.getSender(), capacity);
            if (!undeliveredPackages.isEmpty()) {
                handlePackageAssignment(undeliveredPackages.poll());
            }
        } else {
            availableAgentCapacities.remove(msg.getSender());
        }
    }

    private void requestAgentCapacities() {
        ACLMessage capacityRequest = new ACLMessage(ACLMessage.REQUEST);
        for (int i = 1; i <= expectedNumberOfDeliveringAgents; i++) {
            capacityRequest.addReceiver(new AID("deliver" + i, AID.ISLOCALNAME));
        }
        capacityRequest.setContent("RequestCapacity");
        send(capacityRequest);
        System.out.println("Requested capacities from all DeliveringAgents.");
    }

    private void handlePackageAssignment(PackageDetail packageDetail) {
        int quantity = packageDetail.getQuantity();
        String route = packageDetail.getRoute();

        while (quantity > 0 && !availableAgentCapacities.isEmpty()) {
            Optional<Map.Entry<AID, Integer>> optAgent = availableAgentCapacities.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .findFirst();

            if (optAgent.isPresent()) {
                Map.Entry<AID, Integer> agent = optAgent.get();
                int assignableQuantity = Math.min(quantity, agent.getValue());
                assignPackage(agent.getKey(), assignableQuantity, route);
                quantity -= assignableQuantity;
                int newCapacity = availableAgentCapacities.get(agent.getKey()) - assignableQuantity;
                availableAgentCapacities.put(agent.getKey(), newCapacity);
                System.out.println("Assigned " + assignableQuantity + " packages to " + agent.getKey().getLocalName() + " following " + route);
            } else {
                System.out.println("Remaining package of size: " + quantity);
                undeliveredPackages.add(new PackageDetail(quantity, route));
                break;
            }
        }
    }

    private void assignPackage(AID agentAID, int quantity, String route) {
        ACLMessage assignMsg = new ACLMessage(ACLMessage.REQUEST);
        assignMsg.addReceiver(agentAID);
        assignMsg.setContent(quantity + " packages assigned; Route: " + route);
        send(assignMsg);
    }
}

class PackageDetail {
    private int quantity;
    private String route;

    public PackageDetail(int quantity, String route) {
        this.quantity = quantity;
        this.route = route;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getRoute() {
        return route;
    }
}
