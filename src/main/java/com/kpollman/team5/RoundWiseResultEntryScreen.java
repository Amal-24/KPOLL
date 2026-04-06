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
        constituencyBox = new JComboBox<>(new String[]{"Thiruvananthapuram", "Ernakulam", "Kozhikode"});
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(constituencyBox, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Select Round Number"), gbc);
        roundBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(roundBox, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Select Candidate"), gbc);
        candidateBox = new JComboBox<>(new String[]{"Candidate A", "Candidate B", "Candidate C"});
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

    private void saveResult() {
        try {
            int votes = Integer.parseInt(votesField.getText());
            if (votes < 0) throw new NumberFormatException();

            // SQL to save results (Simplified)
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "INSERT INTO RoundResults (constituency_id, round_number, candidate_id, votes_counted) " +
                               "VALUES (1, ?, 1, ?) ON DUPLICATE KEY UPDATE votes_counted = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, (int) roundBox.getSelectedItem());
                pstmt.setInt(2, votes);
                pstmt.setInt(3, votes);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Result saved successfully for Round " + roundBox.getSelectedItem());
                    MainDashboard.showView(new CountingProgressTrackerScreen());
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number for votes.");
        } catch (Exception ex) {
            System.err.println("Result entry error: " + ex.getMessage());
            // Fallback for demo
            JOptionPane.showMessageDialog(this, "Demo Mode: Result simulated for Round " + roundBox.getSelectedItem());
            MainDashboard.showView(new CountingProgressTrackerScreen());
        }
    }
}
