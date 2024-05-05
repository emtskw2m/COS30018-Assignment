package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DeliveringAgent extends Agent {
    private int maxCapacity;
    private int currentLoad = 0;
    private int status; // 0 for busy, 1 for available

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            maxCapacity = Integer.parseInt(args[0].toString());
            status = Integer.parseInt(args[1].toString());
        } else {
            maxCapacity = 100; // Default capacity
            status = 1; // Default status: available
        }

        System.out.println(getLocalName() + " with capacity " + maxCapacity + " and status " + getStatusString() + " is ready.");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    if ("RequestCapacity".equals(msg.getContent())) {
                        // Respond with current capacity and status
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Capacity:" + maxCapacity + ";Status:" + getStatusString());
                        send(reply);
                        System.out.println(getLocalName() + " replied with capacity " + maxCapacity + " and status " + getStatusString());
                    } else {
                        int quantity = Integer.parseInt(msg.getContent());
                        handlePackageRequest(msg, quantity);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void handlePackageRequest(ACLMessage msg, int quantity) {
        ACLMessage reply = msg.createReply();
        if (status == 0) {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Agent is busy.");
            System.out.println(getLocalName() + " is busy.");
        } else if (quantity <= maxCapacity - currentLoad) {
            currentLoad += quantity;
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent("Package received by " + getLocalName() + ", delivery start.");
            System.out.println("Package received by " + getLocalName() + ", delivery start.");

            // Reset the current load to 0 after confirming the package receipt and start of delivery
            currentLoad = 0;  // Resetting the capacity as the package is delivered instantly
            System.out.println("Capacity of " + getLocalName() + " reset.");

        } else {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Capacity exceeded. Current load: " + currentLoad);
            System.out.println(getLocalName() + " not able to handle the package. Current load: " + currentLoad);
        }
        send(reply);
    }

    private String getStatusString() {
        return status == 0 ? "busy" : "available";
    }
}