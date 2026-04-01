package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RoundWiseResultEntryScreen extends JPanel {
    private int tableId;
    private JComboBox<Integer> roundDropdown;
    private JComboBox<String> candidateDropdown;
    private ModernUI.ModernTextField votesField;
    private ModernUI.ModernButton submitButton;

    public RoundWiseResultEntryScreen(int tableId) {
        this.tableId = tableId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Round-wise Result Entry (Table " + tableId + ")");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Tables");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingTableManagementScreen(0))); // Fixed: need a valid centerId if possible, or just back
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
        card.add(new JLabel("Round Number"), gbc);
        roundDropdown = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(roundDropdown, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Candidate"), gbc);
        candidateDropdown = new JComboBox<>();
        loadCandidates();
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(candidateDropdown, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Votes Counted"), gbc);
        votesField = new ModernUI.ModernTextField("0");
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(votesField, gbc);

        submitButton = new ModernUI.ModernButton("Save Round Result");
        submitButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 6;
        card.add(submitButton, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        submitButton.addActionListener(e -> submitResult());
    }

    private void loadCandidates() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT candidate_id, candidate_name FROM Candidates";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                candidateDropdown.addItem(rs.getInt("candidate_id") + ": " + rs.getString("candidate_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitResult() {
        try {
            int round = (int) roundDropdown.getSelectedItem();
            String candidateStr = (String) candidateDropdown.getSelectedItem();
            int candidateId = Integer.parseInt(candidateStr.split(":")[0]);
            int votes = Integer.parseInt(votesField.getText());

            try (Connection conn = DatabaseHelper.getConnection()) {
                String insertQuery = "INSERT INTO RoundResults (round_no, table_id, candidate_id, votes_counted) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setInt(1, round);
                pstmt.setInt(2, tableId);
                pstmt.setInt(3, candidateId);
                pstmt.setInt(4, votes);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Result saved successfully!");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
