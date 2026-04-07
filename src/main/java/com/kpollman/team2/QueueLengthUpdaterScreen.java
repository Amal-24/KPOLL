package com.kpollman.team2;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueueLengthUpdaterScreen extends JPanel {
    private int boothId;
    private ModernUI.ModernTextField queueLengthField;
    private ModernUI.ModernTextField activeStationsField;
    private JComboBox<String> algorithmBox;
    private ModernUI.ModernButton updateButton;
    private QueueAlgorithm algorithm;

    public QueueLengthUpdaterScreen(int boothId) {
        this.boothId = boothId;
        this.algorithm = new StandardWaitTimeAlgorithm();

        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Booth Official: Update Queue Status (Booth " + boothId + ")");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new QueueStatusDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.gridx = 0;

        gbc.gridy = 0;
        card.add(new JLabel("Current Queue Length (People)"), gbc);
        queueLengthField = new ModernUI.ModernTextField("0");
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(queueLengthField, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Active Polling Stations"), gbc);
        activeStationsField = new ModernUI.ModernTextField("1");
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(activeStationsField, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Wait Time Calculation Algorithm"), gbc);
        algorithmBox = new JComboBox<>(new String[]{"Standard (5m/person)", "Peak Hour (7m/person)"});
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(algorithmBox, gbc);

        updateButton = new ModernUI.ModernButton("Update Real-Time Data");
        updateButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 6;
        card.add(updateButton, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        updateButton.addActionListener(e -> updateQueue());
        fetchCurrentData();
    }

    private void fetchCurrentData() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT COALESCE(q.current_queue_length, 0) as current_queue_length, " +
                          "COALESCE(q.active_stations, 1) as active_stations " +
                          "FROM Booths b LEFT JOIN QueueStatus q ON b.booth_id = q.booth_id WHERE b.booth_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, boothId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                queueLengthField.setText(String.valueOf(rs.getInt("current_queue_length")));
                activeStationsField.setText(String.valueOf(rs.getInt("active_stations")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateQueue() {
        try {
            int queueLength = Integer.parseInt(queueLengthField.getText());
            int activeStations = Integer.parseInt(activeStationsField.getText());

            if (queueLength < 0 || activeStations <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid non-negative values.");
                return;
            }

            // Polymorphic algorithm selection
            if (algorithmBox.getSelectedIndex() == 0) {
                algorithm = new StandardWaitTimeAlgorithm();
            } else {
                algorithm = new PeakHourWaitTimeAlgorithm();
            }

            int waitTime = algorithm.calculateWaitTime(queueLength, activeStations);

            try (Connection conn = DatabaseHelper.getConnection()) {
                String updateQuery = "INSERT INTO QueueStatus (booth_id, current_queue_length, avg_wait_time_mins, active_stations, last_updated) " +
                                   "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                                   "ON DUPLICATE KEY UPDATE current_queue_length = VALUES(current_queue_length), " +
                                   "avg_wait_time_mins = VALUES(avg_wait_time_mins), " +
                                   "active_stations = VALUES(active_stations), " +
                                   "last_updated = CURRENT_TIMESTAMP";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setInt(1, boothId);
                pstmt.setInt(2, queueLength);
                pstmt.setInt(3, waitTime);
                pstmt.setInt(4, activeStations);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Queue status updated successfully!\nEstimated wait: " + waitTime + " mins");
                    MainDashboard.showView(new QueueStatusDashboard());
                } else {
                    JOptionPane.showMessageDialog(this, "No changes were made to the database.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating queue: " + ex.getMessage());
        }
    }
}
