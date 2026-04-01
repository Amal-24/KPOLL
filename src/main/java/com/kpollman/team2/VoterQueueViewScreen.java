package com.kpollman.team2;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VoterQueueViewScreen extends JPanel {
    private int boothId;
    private JLabel boothNameLabel;
    private JLabel queueLengthLabel;
    private JLabel waitTimeLabel;
    private JLabel suggestionLabel;

    public VoterQueueViewScreen(int boothId) {
        this.boothId = boothId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Check Queue Status (Booth " + boothId + ")");
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
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.WEST;

        addInfoRow(card, "Booth Name:", boothNameLabel = new JLabel("Fetching..."), 0, gbc);
        addInfoRow(card, "Current Queue Length:", queueLengthLabel = new JLabel("0"), 1, gbc);
        addInfoRow(card, "Estimated Wait Time:", waitTimeLabel = new JLabel("0 mins"), 2, gbc);
        
        suggestionLabel = new JLabel("Loading...");
        suggestionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        card.add(suggestionLabel, gbc);

        add(card, BorderLayout.CENTER);

        ModernUI.ModernButton refreshButton = new ModernUI.ModernButton("Refresh Status");
        refreshButton.setBackground(ModernUI.PRIMARY_COLOR);
        refreshButton.addActionListener(e -> fetchQueueData());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(refreshButton);
        add(footer, BorderLayout.SOUTH);

        fetchQueueData();
    }

    private void addInfoRow(JPanel panel, String label, JLabel valueLabel, int row, GridBagConstraints gbc) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        valueLabel.setFont(ModernUI.MAIN_FONT);
        panel.add(valueLabel, gbc);
    }

    private void fetchQueueData() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT q.*, b.booth_name FROM QueueStatus q JOIN Booths b ON q.booth_id = b.booth_id WHERE q.booth_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, boothId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boothNameLabel.setText(rs.getString("booth_name"));
                int queueLength = rs.getInt("current_queue_length");
                int waitTime = rs.getInt("avg_wait_time_mins");
                queueLengthLabel.setText(String.valueOf(queueLength));
                waitTimeLabel.setText(waitTime + " mins");
                
                if (waitTime < 15) {
                    suggestionLabel.setText("Optimal time to visit!");
                    suggestionLabel.setForeground(new Color(16, 185, 129));
                } else if (waitTime < 30) {
                    suggestionLabel.setText("Moderate queue.");
                    suggestionLabel.setForeground(new Color(59, 130, 246));
                } else {
                    suggestionLabel.setText("Heavy queue. Consider visiting later.");
                    suggestionLabel.setForeground(new Color(239, 68, 68));
                }
            } else {
                String boothQuery = "SELECT booth_name FROM Booths WHERE booth_id = ?";
                PreparedStatement bpstmt = conn.prepareStatement(boothQuery);
                bpstmt.setInt(1, boothId);
                ResultSet brs = bpstmt.executeQuery();
                if (brs.next()) {
                    boothNameLabel.setText(brs.getString("booth_name"));
                    queueLengthLabel.setText("No Data");
                    waitTimeLabel.setText("No Data");
                    suggestionLabel.setText("No current queue data available.");
                    suggestionLabel.setForeground(ModernUI.ACCENT_COLOR);
                } else {
                    JOptionPane.showMessageDialog(this, "Booth ID not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
