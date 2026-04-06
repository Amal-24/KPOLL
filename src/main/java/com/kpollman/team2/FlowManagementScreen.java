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
 * Flow Management Screen for voter flow optimization suggestions.
 */
public class FlowManagementScreen extends JPanel {
    private JPanel suggestionsPanel;

    public FlowManagementScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Voter Flow Management & Optimization");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new QueueStatusDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        suggestionsPanel = new JPanel();
        suggestionsPanel.setLayout(new BoxLayout(suggestionsPanel, BoxLayout.Y_AXIS));
        suggestionsPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(suggestionsPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        generateSuggestions();
    }

    private void generateSuggestions() {
        suggestionsPanel.removeAll();
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT q.*, b.booth_name FROM QueueStatus q JOIN Booths b ON q.booth_id = b.booth_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String boothName = rs.getString("booth_name");
                int qLen = rs.getInt("current_queue_length");
                int stations = rs.getInt("active_stations");
                int waitTime = rs.getInt("avg_wait_time_mins");

                processSuggestion(boothName, qLen, stations, waitTime);
                dataFound = true;
            }
        } catch (Exception e) {
            System.err.println("Flow data fetch error: " + e.getMessage());
        }

        if (!dataFound) {
            // Mock suggestions
            processSuggestion("Booth A (Mock)", 12, 2, 8);
            processSuggestion("Booth B (Mock)", 55, 1, 52);
            processSuggestion("Booth C (Mock)", 32, 2, 34);
        }

        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }

    private void processSuggestion(String boothName, int qLen, int stations, int waitTime) {
        String suggestion;
        Color suggestionColor;
        if (waitTime > 45) {
            suggestion = "CRITICAL: Redirect voters or open additional stations immediately!";
            suggestionColor = new Color(239, 68, 68);
        } else if (waitTime > 30) {
            suggestion = "WARNING: High wait time. Consider suggesting voters to visit later.";
            suggestionColor = new Color(245, 158, 11);
        } else if (waitTime < 10) {
            suggestion = "GOOD: Efficient flow. Maintain current operations.";
            suggestionColor = new Color(16, 185, 129);
        } else {
            suggestion = "NORMAL: Stable flow. Monitor for any sudden spikes.";
            suggestionColor = ModernUI.PRIMARY_COLOR;
        }
        addSuggestionCard(boothName, qLen, stations, waitTime, suggestion, suggestionColor);
    }

    private void addSuggestionCard(String boothName, int qLen, int stations, int waitTime, String suggestion, Color color) {
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout(15, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(800, 150));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(boothName + " - Queue: " + qLen + " | Stations: " + stations);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoPanel.add(nameLabel);
        
        JLabel waitLabel = new JLabel("Estimated Wait: " + waitTime + " mins");
        waitLabel.setFont(ModernUI.MAIN_FONT);
        infoPanel.add(waitLabel);
        card.add(infoPanel, BorderLayout.CENTER);

        JLabel suggestLabel = new JLabel(suggestion);
        suggestLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        suggestLabel.setForeground(color);
        card.add(suggestLabel, BorderLayout.SOUTH);

        suggestionsPanel.add(card);
        suggestionsPanel.add(Box.createVerticalStrut(20));
    }
}
