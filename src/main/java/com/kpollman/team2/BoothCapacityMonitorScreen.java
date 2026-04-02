package com.kpollman.team2;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Booth Capacity Monitor Screen.
 */
public class BoothCapacityMonitorScreen extends JFrame {
    private int boothId;
    private JProgressBar capacityBar;
    private JLabel statusLabel;

    public BoothCapacityMonitorScreen(int boothId) {
        this.boothId = boothId;
        setTitle("K-PollMan 2026 - Booth Capacity Monitor (Booth " + boothId + ")");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(230, 240, 255));
        JLabel titleLabel = new JLabel("Booth Capacity Monitor", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        statusLabel = new JLabel("Capacity Status: Loading...");
        contentPanel.add(statusLabel);

        capacityBar = new JProgressBar(0, 100);
        capacityBar.setStringPainted(true);
        contentPanel.add(capacityBar);

        ModernUI.ModernButton refreshButton = new ModernUI.ModernButton("Refresh Capacity");
        contentPanel.add(refreshButton);
        add(contentPanel, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> refreshCapacity());
        refreshCapacity();
    }

    private void refreshCapacity() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT current_queue_length, active_stations FROM QueueStatus WHERE booth_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, boothId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int queueLength = rs.getInt("current_queue_length");
                int activeStations = rs.getInt("active_stations");
                
                // Assume 100 is max capacity for calculation
                int capacityPercentage = (queueLength * 100) / (activeStations * 20); // Each station can handle 20 people in queue comfortably
                if (capacityPercentage > 100) capacityPercentage = 100;
                
                capacityBar.setValue(capacityPercentage);
                if (capacityPercentage < 50) {
                    statusLabel.setText("Capacity Status: LOW (Good Flow)");
                    capacityBar.setForeground(Color.GREEN);
                } else if (capacityPercentage < 80) {
                    statusLabel.setText("Capacity Status: MODERATE (Normal Flow)");
                    capacityBar.setForeground(Color.BLUE);
                } else {
                    statusLabel.setText("Capacity Status: HIGH (Heavy Flow)");
                    capacityBar.setForeground(Color.RED);
                }
            } else {
                statusLabel.setText("Capacity Status: No Data");
                capacityBar.setValue(0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching capacity: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
