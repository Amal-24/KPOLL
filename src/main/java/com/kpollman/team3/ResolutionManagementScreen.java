package com.kpollman.team3;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ResolutionManagementScreen extends JPanel {
    private int issueId;
    private JComboBox<String> statusDropdown;
    private JTextArea resolutionArea;
    private ModernUI.ModernButton resolveButton;

    public ResolutionManagementScreen(int issueId) {
        this.issueId = issueId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Resolution Management (ID: " + issueId + ")");
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
        card.add(new JLabel("Update Status"), gbc);
        statusDropdown = new JComboBox<>(new String[]{"In-Progress", "Resolved", "Closed"});
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(statusDropdown, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Resolution Notes"), gbc);
        resolutionArea = new JTextArea(5, 20);
        resolutionArea.setFont(ModernUI.MAIN_FONT);
        resolutionArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(resolutionArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(scrollPane, gbc);

        resolveButton = new ModernUI.ModernButton("Update Resolution Status");
        resolveButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 4;
        card.add(resolveButton, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        resolveButton.addActionListener(e -> updateResolution());
    }

    private void updateResolution() {
        String status = (String) statusDropdown.getSelectedItem();
        String notes = resolutionArea.getText().trim();

        if (notes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add resolution notes");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String updateQuery = "UPDATE PollingIssues SET status = ?, resolution_notes = ?, resolved_at = ? WHERE issue_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, status);
            pstmt.setString(2, notes);
            
            if ("Resolved".equals(status) || "Closed".equals(status)) {
                pstmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            } else {
                pstmt.setTimestamp(3, null);
            }
            
            pstmt.setInt(4, issueId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Resolution status updated successfully!");
                MainDashboard.showView(new IssueDetailsScreen(issueId));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
