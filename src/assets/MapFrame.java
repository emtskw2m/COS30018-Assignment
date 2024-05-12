package assets; 

import javax.swing.*;  
import java.awt.*; 
import jade.core.Agent;  
import jade.core.AID;  
import jade.lang.acl.ACLMessage;  
import ACO.AntColonyOptimization;  
import java.util.List; 


public class MapFrame extends JFrame {
    private JTextField packageInput;
    private JButton submitButton;
    private MapPanel mapPanel;
    private Agent ownerAgent;

    public MapFrame(CityMap cityMap, Agent owner) {
        super("City Map Visualization");
        this.ownerAgent = owner;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mapPanel = new MapPanel(cityMap);
        add(mapPanel, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        packageInput = new JTextField(10);
        submitButton = new JButton("Start Optimization");

        inputPanel.add(new JLabel("Enter the quantity of packages:"));
        inputPanel.add(packageInput);
        inputPanel.add(submitButton);

        add(inputPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> startOptimization());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startOptimization() {
        try {
            int quantity = Integer.parseInt(packageInput.getText());
            List<String> selectedStationNames = mapPanel.getSelectedStations();
            if (selectedStationNames.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one station.", "No Stations Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Initiate ACO
            CityMap cityMap = mapPanel.getCityMap();
            AntColonyOptimization aco = new AntColonyOptimization(cityMap, selectedStationNames);
            aco.startAntOptimization();

            // Send package info and route to MasterRoutingAgent
            ACLMessage deliveryRequest = new ACLMessage(ACLMessage.INFORM);
            deliveryRequest.addReceiver(new AID("master", AID.ISLOCALNAME));
            String route = String.join(", ", selectedStationNames);
            deliveryRequest.setContent("PackageQuantity:" + quantity + ";Route:" + route);
            ownerAgent.send(deliveryRequest);

            JOptionPane.showMessageDialog(this, "Optimization started for " + quantity + " packages to " + route);
            packageInput.setText(""); 
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for package quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}