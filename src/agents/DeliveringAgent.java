package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Random;

public class DeliveringAgent extends Agent {
    private int maxCapacity;
    private int currentLoad = 0;
    private String status;  

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            maxCapacity = Integer.parseInt(args[0].toString());
        } else {
            maxCapacity = 100; // Default capacity
        }

        // Randomly decide if the agent is busy or available at setup
        status = new Random().nextBoolean() ? "busy" : "available";

        System.out.println(getLocalName() + " with capacity " + maxCapacity + " and status " + status + " is ready.");

        addBehaviour(new CyclicBehaviour(this) {
        	public void action() {
        	    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        	    ACLMessage msg = myAgent.receive(mt);
        	    if (msg != null) {
        	        if ("RequestCapacity".equals(msg.getContent())) {
        	            sendCapacityUpdate();
        	        } else {
        	            handlePackageRequest(msg, msg.getContent());
        	        }
        	    } else {
        	        block();
        	    }
        	}
        });
    }

    private void sendCapacityUpdate() {
        ACLMessage capacityUpdate = new ACLMessage(ACLMessage.INFORM);
        capacityUpdate.addReceiver(new AID("master", AID.ISLOCALNAME));
        capacityUpdate.setContent("Capacity:" + (maxCapacity - currentLoad) + ";Status:" + status);
        send(capacityUpdate);
    }

    private void handlePackageRequest(ACLMessage msg, String content) {
        try {
            String[] parts = content.split("; ");
            int quantity = Integer.parseInt(parts[0].split(" ")[0]);
            String route = parts[1].substring(6); 

            if (quantity + currentLoad <= maxCapacity && "available".equals(status)) {
                currentLoad += quantity;
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("Package(" + quantity + ") received by " + getLocalName() + ", following route " + route + ", delivery start.");
                send(reply);
                System.out.println("Package(" + quantity + ") received by " + getLocalName() + ", following route " + route + ", delivery start.");
                // Simulate delivery completion
                currentLoad -= quantity;  // Assume instant delivery for simplicity
                sendCapacityUpdate();
            } else {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("Capacity exceeded or not available. Current load: " + currentLoad);
                send(reply);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing package quantity: " + e.getMessage());
            e.printStackTrace();
        }
    }

}