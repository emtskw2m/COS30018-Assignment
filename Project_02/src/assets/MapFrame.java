package assets;

import javax.swing.*;
import java.awt.*;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class MapFrame extends JFrame {
    private JTextField packageInput;
    private JButton submitButton;
    private CityMap cityMap;
    private Agent ownerAgent;

    public MapFrame(CityMap cityMap, Agent owner) {
        super("City Map Visualization");
        this.cityMap = cityMap;
        this.ownerAgent = owner;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        MapPanel mapPanel = new MapPanel(cityMap);
        add(mapPanel, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        packageInput = new JTextField(10);
        submitButton = new JButton("Submit");
        inputPanel.add(new JLabel("Enter the quantity of packages:"));
        inputPanel.add(packageInput);
        inputPanel.add(submitButton);

        add(inputPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submitPackageQuantity());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void submitPackageQuantity() {
        try {
            int quantity = Integer.parseInt(packageInput.getText());
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("master", AID.ISLOCALNAME));
            msg.setContent("PackageQuantity:" + quantity);
            ownerAgent.send(msg);

            packageInput.setText(""); // Clear the input field
            JOptionPane.showMessageDialog(this, "Quantity submitted: " + quantity);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}