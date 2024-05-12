package assets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class MapPanel extends JPanel {
    private final CityMap cityMap;
    private Set<String> selectedStations;

    public MapPanel(CityMap cityMap) {
        this.cityMap = cityMap;
        this.selectedStations = new HashSet<>();
        setPreferredSize(new Dimension(600, 600));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    private void handleMouseClick(int x, int y) {
        for (Station station : cityMap.getStations().values()) {
            if (Math.sqrt(Math.pow(x - station.getX(), 2) + Math.pow(y - station.getY(), 2)) <= 10) {
                toggleStationSelection(station.getName());
                repaint();
                return;
            }
        }
    }

    private void toggleStationSelection(String stationName) {
        if (selectedStations.contains(stationName)) {
            selectedStations.remove(stationName);
        } else {
            selectedStations.add(stationName);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        for (Station station : cityMap.getStations().values()) {
            int x = station.getX();
            int y = station.getY();
            g2.setColor(selectedStations.contains(station.getName()) ? Color.RED : Color.BLACK);
            g2.fillOval(x - 5, y - 5, 10, 10);
            g2.drawString(station.getName(), x + 10, y - 10);

            for (Map.Entry<Station, Integer> connection : station.getConnections().entrySet()) {
                Station connectedStation = connection.getKey();
                int x2 = connectedStation.getX();
                int y2 = connectedStation.getY();
                g2.drawLine(x, y, x2, y2);
                g2.drawString(connection.getValue().toString(), (x + x2) / 2, (y + y2) / 2);
            }
        }
    }

    public List<String> getSelectedStations() {
        return new ArrayList<>(selectedStations);
    }

    public CityMap getCityMap() {
        return this.cityMap;
    }
}
