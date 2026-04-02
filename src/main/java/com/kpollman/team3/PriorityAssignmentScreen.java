package com.kpollman.team3;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PriorityAssignmentScreen extends JPanel {
    private int issueId;
    private JComboBox<String> urgencyDropdown;
    private ModernUI.ModernTextField officialField;
    private ModernUI.ModernButton updateButton;

    public PriorityAssignmentScreen(int issueId) {
        this.issueId = issueId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Assign Priority & Official (ID: " + issueId + ")");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Details");
        backBtn.addActionListener(e -> MainDashboard.showView(new IssueDetailsScreen(issueId)));
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
        card.add(new JLabel("Urgency Level"), gbc);
        urgencyDropdown = new JComboBox<>(new String[]{"Low", "Medium", "High", "Critical"});
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(urgencyDropdown, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Assigned Official"), gbc);
        officialField = new ModernUI.ModernTextField("");
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(officialField, gbc);

        updateButton = new ModernUI.ModernButton("Update Assignment");
        updateButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 4;
        card.add(updateButton, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        updateButton.addActionListener(e -> updatePriority());
    }

    private void updatePriority() {
        String urgency = (String) urgencyDropdown.getSelectedItem();
        String official = officialField.getText().trim();

        if (official.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please assign an official");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String updateQuery = "UPDATE PollingIssues SET urgency_level = ?, assigned_official = ? WHERE issue_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, urgency);
            pstmt.setString(2, official);
            pstmt.setInt(3, issueId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Assignment updated successfully!");
                MainDashboard.showView(new IssueDetailsScreen(issueId));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
