package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Round-wise Result Entry Screen to enter votes per candidate per round.
 */
public class RoundWiseResultEntryScreen extends JPanel {
    private JComboBox<String> constituencyBox;
    private JComboBox<Integer> roundBox;
    private JComboBox<String> candidateBox;
    private ModernUI.ModernTextField votesField;

    public RoundWiseResultEntryScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Round-wise Result Entry");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Form Content
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.gridx = 0;

        gbc.gridy = 0;
        card.add(new JLabel("Select Constituency"), gbc);
        constituencyBox = new JComboBox<>();
        loadConstituencies();
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(constituencyBox, gbc);
        constituencyBox.addActionListener(e -> loadCandidates());

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Select Round Number"), gbc);
        roundBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20});
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(roundBox, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Select Candidate"), gbc);
        candidateBox = new JComboBox<>();
        loadCandidates();
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(candidateBox, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Enter Votes Counted"), gbc);
        votesField = new ModernUI.ModernTextField("0");
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(votesField, gbc);

        ModernUI.ModernButton saveBtn = new ModernUI.ModernButton("Save Round Result");
        saveBtn.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 8;
        card.add(saveBtn, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        saveBtn.addActionListener(e -> saveResult());
    }

    private void loadConstituencies() {
        constituencyBox.removeAllItems();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT constituency_id, constituency_name FROM Constituencies";
            PreparedStatement pstmt = conn.prepareStatement(query);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                constituencyBox.addItem(rs.getInt("constituency_id") + " - " + rs.getString("constituency_name"));
            }
        } catch (Exception e) {
            System.err.println("Error loading constituencies: " + e.getMessage());
        }
    }

    private void loadCandidates() {
        candidateBox.removeAllItems();
        if (constituencyBox.getSelectedIndex() < 0) return;
        
        try {
            String selectedItem = (String) constituencyBox.getSelectedItem();
            int constituencyId = Integer.parseInt(selectedItem.split(" - ")[0]);
            
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "SELECT candidate_id, candidate_name FROM Candidates WHERE constituency_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, constituencyId);
                java.sql.ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    candidateBox.addItem(rs.getInt("candidate_id") + " - " + rs.getString("candidate_name"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading candidates: " + e.getMessage());
        }
    }

    private void saveResult() {
        try {
            int votes = Integer.parseInt(votesField.getText());
            if (votes < 0) throw new NumberFormatException();

            String constituencyItem = (String) constituencyBox.getSelectedItem();
            String candidateItem = (String) candidateBox.getSelectedItem();
            
            if (constituencyItem == null || candidateItem == null) {
                JOptionPane.showMessageDialog(this, "Please select both constituency and candidate.");
                return;
            }
            
            int constituencyId = Integer.parseInt(constituencyItem.split(" - ")[0]);
            int candidateId = Integer.parseInt(candidateItem.split(" - ")[0]);
            int round = (int) roundBox.getSelectedItem();

            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "INSERT INTO RoundResults (table_id, round_no, candidate_id, votes_counted) " +
                               "VALUES ((SELECT table_id FROM CountingTables WHERE constituency_id = ? LIMIT 1), ?, ?, ?) " +
                               "ON DUPLICATE KEY UPDATE votes_counted = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, constituencyId);
                pstmt.setInt(2, round);
                pstmt.setInt(3, candidateId);
                pstmt.setInt(4, votes);
                pstmt.setInt(5, votes);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Result saved successfully for Round " + round);
                    MainDashboard.showView(new CountingProgressTrackerScreen());
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number for votes.");
        } catch (Exception ex) {
            System.err.println("Result entry error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving result: " + ex.getMessage());
        }
        }
    }
}
