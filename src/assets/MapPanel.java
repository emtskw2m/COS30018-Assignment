package assets;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MapPanel extends JPanel {
    private final CityMap cityMap;

    public MapPanel(CityMap cityMap) {
        this.cityMap = cityMap;
        setPreferredSize(new Dimension(600, 600)); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g; // Use Graphics2D for better control over graphics
        g2.setStroke(new BasicStroke(2)); // Set the stroke of the graphics to make the lines visible enough

        // Draw all stations and connections between them
        for (Station station : cityMap.getStations().values()) {
            int x1 = station.getX();
            int y1 = station.getY();
            g2.fillOval(x1 - 5, y1 - 5, 10, 10); // Draw the station as a circle
            g2.drawString(station.getName(), x1 + 10, y1 - 10); // Label the station

            // Draw lines for all connections from this station
            for (Map.Entry<Station, Integer> connection : station.getConnections().entrySet()) {
                Station connectedStation = connection.getKey();
                int x2 = connectedStation.getX();
                int y2 = connectedStation.getY();
                g2.drawLine(x1, y1, x2, y2); // Draw a line to the connected station
                // Draw the distance near the midpoint of the line
                g2.drawString(connection.getValue().toString(), (x1 + x2) / 2, (y1 + y2) / 2);
            }
        }
    }
}