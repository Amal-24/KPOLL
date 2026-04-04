package com.kpollman.team3;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class IssueDetailsScreen extends JPanel {

    private int issueId;

    public IssueDetailsScreen(int issueId) {
        this.issueId = issueId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Issue Details (ID: " + issueId + ")");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to List");
        backBtn.addActionListener(e -> MainDashboard.showView(new IssueTrackingDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Details Card
        ModernUI.RoundedPanel detailsCard = new ModernUI.RoundedPanel(20, ModernUI.CARD_BACKGROUND);
        detailsCard.setLayout(new GridBagLayout());
        detailsCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        add(detailsCard, BorderLayout.CENTER);

        // Footer
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionPanel.setOpaque(false);

        if (!"VOTER".equals(Session.role)) {
            ModernUI.ModernButton assignPriorityButton = new ModernUI.ModernButton("Assign Priority");
            ModernUI.ModernButton resolutionButton = new ModernUI.ModernButton("Resolution Management");
            resolutionButton.setBackground(ModernUI.ACCENT_COLOR);

            actionPanel.add(assignPriorityButton);
            actionPanel.add(Box.createHorizontalStrut(20));
            actionPanel.add(resolutionButton);

            assignPriorityButton.addActionListener(e ->
                    MainDashboard.showView(new PriorityAssignmentScreen(issueId)));

            resolutionButton.addActionListener(e ->
                    MainDashboard.showView(new ResolutionManagementScreen(issueId)));
        }

        add(actionPanel, BorderLayout.SOUTH);

        fetchIssueDetails(detailsCard);
    }

    private void fetchIssueDetails(JPanel panel) {
        panel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 30);

        try (Connection conn = DatabaseHelper.getConnection()) {

            String query = "SELECT * FROM PollingIssues WHERE issue_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, issueId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                String category = rs.getString("category");
                String description = rs.getString("description");
                int boothId = rs.getInt("booth_id");
                String urgency = rs.getString("urgency_level");
                String status = rs.getString("status");

                String assigned = rs.getString("assigned_official");
                String notes = rs.getString("resolution_notes");

                Timestamp reported = rs.getTimestamp("reported_at");
                Timestamp resolved = rs.getTimestamp("resolved_at");

                // Basic Details
                addDetailRow(panel, "Category:", category, 0, gbc);
                addDetailRow(panel, "Booth ID:", String.valueOf(boothId), 1, gbc);
                addDetailRow(panel, "Urgency:", urgency, 2, gbc);
                addDetailRow(panel, "Status:", status, 3, gbc);

                // Additional Details
                addDetailRow(panel, "Assigned To:", 
                        assigned != null ? assigned : "Not Assigned", 4, gbc);

                addDetailRow(panel, "Reported At:", 
                        reported != null ? reported.toString() : "-", 5, gbc);

                addDetailRow(panel, "Resolved At:", 
                        resolved != null ? resolved.toString() : "Pending", 6, gbc);

                addDetailRow(panel, "Notes:", 
                        notes != null ? notes : "No notes", 7, gbc);

                // Resolution Time
                if (resolved != null && reported != null) {
                    long diff = resolved.getTime() - reported.getTime();
                    long hours = diff / (1000 * 60 * 60);
                    addDetailRow(panel, "Resolution Time:", hours + " hours", 8, gbc);
                }

                // Description Section (FULL WIDTH)
                gbc.gridy = 9;
                gbc.gridx = 0;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;

                JTextArea desc = new JTextArea(description);
                desc.setFont(ModernUI.MAIN_FONT);
                desc.setEditable(false);
                desc.setLineWrap(true);
                desc.setWrapStyleWord(true);
                desc.setBackground(new Color(248, 250, 252));
                desc.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                panel.add(new JScrollPane(desc), gbc);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        panel.revalidate();
        panel.repaint();
    }

    private void addDetailRow(JPanel panel, String label, String value, int row, GridBagConstraints gbc) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, gbc);

        gbc.gridx = 1;

        JLabel val = new JLabel(value);
        val.setFont(ModernUI.MAIN_FONT);
        panel.add(val, gbc);
    }
}