package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DeliveringAgent extends Agent {
    private int maxCapacity;
    private int currentLoad = 0;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            maxCapacity = Integer.parseInt(args[0].toString());
        } else {
            maxCapacity = 100; // Default capacity
        }

        System.out.println(getLocalName() + " with capacity " + maxCapacity + " is ready.");

        ACLMessage capacityMsg = new ACLMessage(ACLMessage.INFORM);
        capacityMsg.addReceiver(new AID("master", AID.ISLOCALNAME));
        capacityMsg.setContent("Capacity:" + maxCapacity);
        send(capacityMsg);

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    int quantity = Integer.parseInt(msg.getContent());
                    ACLMessage reply = msg.createReply();

                    if (currentLoad + quantity <= maxCapacity) {
                        currentLoad += quantity;
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Package received by " + getLocalName() + ", delivery start.");
                        System.out.println("Package received by " + getLocalName() + ", delivery start.");
                        // Reset current load immediately after confirming delivery
                        currentLoad = 0;
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Capacity exceeded. Current load: " + currentLoad);
                        System.out.println(getLocalName() + " not able to handle the package. Current load: " + currentLoad);
                    }
                    send(reply);
                } else {
                    block();
                }
            }
        });
    }
}
