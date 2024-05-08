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
    private Queue<Integer> undeliveredPackages = new LinkedList<>();
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
                    int quantity = Integer.parseInt(msg.getContent().substring(16));
                    requestAgentCapacities();
                    undeliveredPackages.add(quantity);
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

    private void handlePackageAssignment(int quantity) {
        while (quantity > 0 && !availableAgentCapacities.isEmpty()) {
            Optional<Map.Entry<AID, Integer>> optAgent = availableAgentCapacities.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .findFirst();

            if (optAgent.isPresent()) {
                Map.Entry<AID, Integer> agent = optAgent.get();
                int assignableQuantity = Math.min(quantity, agent.getValue());
                assignPackage(agent.getKey(), assignableQuantity);
                quantity -= assignableQuantity;
                int newCapacity = availableAgentCapacities.get(agent.getKey()) - assignableQuantity;
                availableAgentCapacities.put(agent.getKey(), newCapacity);
                System.out.println("Assigned " + assignableQuantity + " packages to " + agent.getKey().getLocalName());
            } else {
                System.out.println("Remaining package of size: " + quantity);
                undeliveredPackages.add(quantity);
                break;
            }
        }
    }

    private void assignPackage(AID agentAID, int quantity) {
        ACLMessage assignMsg = new ACLMessage(ACLMessage.REQUEST);
        assignMsg.addReceiver(agentAID);
        assignMsg.setContent(String.valueOf(quantity));
        send(assignMsg);
    }
}
