package com.kpollman.team2;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Wait Time Calculator Screen.
 */
public class WaitTimeCalculatorScreen extends JPanel {
    private ModernUI.ModernTextField queueLengthField;
    private ModernUI.ModernTextField activeStationsField;
    private JComboBox<String> boothBox;
    private JComboBox<String> algorithmBox;
    private JLabel resultLabel;
    private QueueAlgorithm algorithm;

    public WaitTimeCalculatorScreen() {
        this.algorithm = new StandardWaitTimeAlgorithm();

        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Estimated Wait Time Calculator");
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
        card.add(new JLabel("Select Booth (Optional)"), gbc);
        boothBox = new JComboBox<>();
        boothBox.addItem("Manual Entry");
        loadBooths();
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(boothBox, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Queue Length (Voters)"), gbc);
        queueLengthField = new ModernUI.ModernTextField("0");
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(queueLengthField, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Active Stations"), gbc);
        activeStationsField = new ModernUI.ModernTextField("1");
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(activeStationsField, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Algorithm"), gbc);
        algorithmBox = new JComboBox<>(new String[]{"Standard (5m/person)", "Peak Hour (7m/person)"});
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(algorithmBox, gbc);

        ModernUI.ModernButton calculateBtn = new ModernUI.ModernButton("Calculate Wait Time");
        calculateBtn.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 8;
        card.add(calculateBtn, gbc);

        resultLabel = new JLabel("Estimated Wait: 0 mins", JLabel.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        resultLabel.setForeground(ModernUI.ACCENT_COLOR);
        gbc.gridy = 9; gbc.insets = new Insets(20, 0, 0, 0);
        card.add(resultLabel, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        boothBox.addActionListener(e -> loadBoothData());
        calculateBtn.addActionListener(e -> calculateWaitTime());
    }

    private void loadBooths() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT booth_id, booth_name FROM Booths ORDER BY booth_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                boothBox.addItem(rs.getInt("booth_id") + " - " + rs.getString("booth_name"));
            }
        } catch (Exception e) {
            System.err.println("Error loading booths: " + e.getMessage());
        }
    }

    private void loadBoothData() {
        if (boothBox.getSelectedIndex() == 0) return; // Manual entry

        try {
            String selectedItem = (String) boothBox.getSelectedItem();
            int boothId = Integer.parseInt(selectedItem.split(" - ")[0]);

            Connection conn = DatabaseHelper.getConnection();
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
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading booth data: " + e.getMessage());
        }
    }

    private void calculateWaitTime() {
        try {
            int qLen = Integer.parseInt(queueLengthField.getText());
            int stations = Integer.parseInt(activeStationsField.getText());
            
            // Select algorithm
            if (algorithmBox.getSelectedIndex() == 0) {
                algorithm = new StandardWaitTimeAlgorithm();
            } else {
                algorithm = new PeakHourWaitTimeAlgorithm();
            }
            
            int waitTime = algorithm.calculateWaitTime(qLen, stations);
            resultLabel.setText("Estimated Wait: " + waitTime + " mins");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid integers");
        }
    }
}
