package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import assets.CityMap;
import assets.MapFrame;

public class MasterRoutingAgent extends Agent {
    private Map<AID, Integer> agentCapacities = new HashMap<>();
    private AtomicInteger receivedCapacities;

    protected void setup() {
        System.out.println("MasterRoutingAgent is ready.");

        // Launch the map visualization in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            CityMap cityMap = new CityMap();
            // Re-add the connections
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
            new MapFrame(cityMap, this); // Make sure to pass 'this' reference to MapFrame
        });

        receivedCapacities = new AtomicInteger(0);

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        if (msg.getContent().startsWith("Capacity:")) {
                            int capacity = Integer.parseInt(msg.getContent().substring(9));
                            agentCapacities.put(msg.getSender(), capacity);
                            System.out.println("Capacity received from " + msg.getSender().getLocalName() + ": " + capacity);
                        } else if (msg.getContent().startsWith("PackageQuantity:")) {
                            int quantity = Integer.parseInt(msg.getContent().substring(16));
                            handlePackageAssignment(quantity);
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void handlePackageAssignment(int quantity) {
        Optional<AID> suitableAgent = agentCapacities.entrySet().stream()
            .filter(entry -> entry.getValue() >= quantity)
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey);

        if (suitableAgent.isPresent()) {
            ACLMessage assignMsg = new ACLMessage(ACLMessage.REQUEST);
            assignMsg.addReceiver(suitableAgent.get());
            assignMsg.setContent(String.valueOf(quantity));
            send(assignMsg);
            System.out.println("DeliveryAgent(" + suitableAgent.get().getLocalName() + ") assigned the packages");
        } else {
            System.out.println("No Agent able to take the package");
        }
    }
}