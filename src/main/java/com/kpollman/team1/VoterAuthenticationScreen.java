package com.kpollman.team1;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.model.Voter;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VoterAuthenticationScreen extends JPanel {
    private int boothId;
    private ModernUI.ModernTextField epicField;
    private ModernUI.ModernButton searchButton;

    public VoterAuthenticationScreen(int boothId) {
        this.boothId = boothId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Voter Authentication");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new BoothDashboard(boothId, "Current Booth", 0)));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        JLabel label = new JLabel("Enter EPIC Number");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 0;
        card.add(label, gbc);

        epicField = new ModernUI.ModernTextField("e.g., KPL1234567");
        gbc.gridy = 1;
        card.add(epicField, gbc);

        searchButton = new ModernUI.ModernButton("Search and Authenticate");
        searchButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        card.add(searchButton, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        searchButton.addActionListener(e -> searchVoter());
    }

    private void searchVoter() {
        String epicNo = epicField.getText().trim();
        if (epicNo.isEmpty() || epicNo.startsWith("e.g.")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid EPIC number");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT * FROM Voters WHERE epic_no = ? AND booth_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, epicNo);
            pstmt.setInt(2, boothId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Voter voter = new Voter(
                    rs.getString("epic_no"),
                    rs.getString("voter_name"),
                    rs.getInt("age"),
                    rs.getString("photo_path"),
                    rs.getInt("serial_no"),
                    rs.getInt("booth_id"),
                    rs.getBoolean("voted_status")
                );
                // Create the panel first
                VoterDetailsDisplayScreen detailsPanel = new VoterDetailsDisplayScreen(voter, boothId);
                // Then show it
                MainDashboard.showView(detailsPanel);
            } else {
                JOptionPane.showMessageDialog(this, "Voter not found in this booth");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
