package com.kpollman.team2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import com.kpollman.model.QueueData;

/**
 * Polymorphism for displaying queue status in different formats (graph, table, meter).
 */
public interface QueueDisplay {
    void displayQueueStatus(JPanel container, List<QueueData> queueDataList);
}

class TableQueueDisplay implements QueueDisplay {
    @Override
    public void displayQueueStatus(JPanel container, List<QueueData> queueDataList) {
        container.removeAll();
        container.setLayout(new BorderLayout());

        String[] columns = {"Booth ID", "Queue Length", "Avg Wait (min)", "Active Stations", "Last Updated"};
        Object[][] data = new Object[queueDataList.size()][5];

        for (int i = 0; i < queueDataList.size(); i++) {
            QueueData qd = queueDataList.get(i);
            data[i][0] = qd.getBoothId();
            data[i][1] = qd.getCurrentQueueLength();
            data[i][2] = qd.getAvgWaitTimeMins();
            data[i][3] = qd.getActiveStations();
            data[i][4] = qd.getLastUpdated() != null ? qd.getLastUpdated().toString() : "N/A";
        }

        JTable table = new JTable(data, columns);
        table.setDefaultEditor(Object.class, null);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(200, 200, 200));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        container.add(scrollPane, BorderLayout.CENTER);

        container.revalidate();
        container.repaint();
    }
}

class GraphQueueDisplay implements QueueDisplay {
    @Override
    public void displayQueueStatus(JPanel container, List<QueueData> queueDataList) {
        container.removeAll();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        for (QueueData qd : queueDataList) {
            JPanel barRow = new JPanel(new BorderLayout(10, 0));
            barRow.setOpaque(false);
            barRow.setMaximumSize(new Dimension(800, 40));

            JLabel nameLbl = new JLabel("Booth " + qd.getBoothId());
            nameLbl.setPreferredSize(new Dimension(100, 30));

            JProgressBar bar = new JProgressBar(0, 100);
            int percentage = Math.min(qd.getCurrentQueueLength() * 5, 100); // Assume max queue of 20 for 100%
            bar.setValue(percentage);
            bar.setStringPainted(true);
            bar.setString(qd.getCurrentQueueLength() + " People (" + qd.getAvgWaitTimeMins() + " min wait)");

            Color barColor = qd.getAvgWaitTimeMins() > 30 ? new Color(239, 68, 68) :
                            (qd.getAvgWaitTimeMins() > 15 ? new Color(245, 158, 11) : new Color(16, 185, 129));
            bar.setForeground(barColor);

            barRow.add(nameLbl, BorderLayout.WEST);
            barRow.add(bar, BorderLayout.CENTER);
            container.add(barRow);
            container.add(Box.createVerticalStrut(5));
        }

        container.revalidate();
        container.repaint();
    }
}

class MeterQueueDisplay implements QueueDisplay {
    @Override
    public void displayQueueStatus(JPanel container, List<QueueData> queueDataList) {
        container.removeAll();
        container.setLayout(new GridLayout(0, 2, 10, 10)); // 2 columns grid

        for (QueueData qd : queueDataList) {
            JPanel meterPanel = new JPanel(new BorderLayout());
            meterPanel.setBorder(BorderFactory.createTitledBorder("Booth " + qd.getBoothId()));
            meterPanel.setPreferredSize(new Dimension(200, 150));

            // Capacity meter (0-100%)
            JProgressBar capacityMeter = new JProgressBar(0, 100);
            int capacityPercent = Math.min((qd.getCurrentQueueLength() * 100) / (qd.getActiveStations() * 20), 100);
            capacityMeter.setValue(capacityPercent);
            capacityMeter.setStringPainted(true);
            capacityMeter.setString("Capacity: " + capacityPercent + "%");
            capacityMeter.setOrientation(SwingConstants.VERTICAL);

            Color meterColor = capacityPercent > 80 ? Color.RED :
                              (capacityPercent > 50 ? Color.ORANGE : Color.GREEN);
            capacityMeter.setForeground(meterColor);

            // Info labels
            JPanel infoPanel = new JPanel(new GridLayout(3, 1));
            infoPanel.add(new JLabel("Queue: " + qd.getCurrentQueueLength()));
            infoPanel.add(new JLabel("Wait: " + qd.getAvgWaitTimeMins() + " min"));
            infoPanel.add(new JLabel("Stations: " + qd.getActiveStations()));

            meterPanel.add(capacityMeter, BorderLayout.CENTER);
            meterPanel.add(infoPanel, BorderLayout.SOUTH);

            container.add(meterPanel);
        }

        container.revalidate();
        container.repaint();
    }
}
