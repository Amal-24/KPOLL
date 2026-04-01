package com.kpollman.team3;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class IssueTrackingDashboard extends JPanel {

    private JTable issueTable;
    private DefaultTableModel tableModel;

    public IssueTrackingDashboard() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        String titleText = ("VOTER".equals(Session.role))
                ? "My Reported Issues"
                : "Polling Booth Issues Dashboard";

        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        ModernUI.ModernButton refreshButton = new ModernUI.ModernButton("Refresh");
        refreshButton.addActionListener(e -> refreshIssues());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Issue ID", "Category", "Booth ID", "Urgency", "Status", "Reported At"};
        tableModel = new DefaultTableModel(columns, 0);
        issueTable = new JTable(tableModel);
        issueTable.setFont(ModernUI.MAIN_FONT);
        issueTable.setRowHeight(40);
        issueTable.setShowVerticalLines(false);
        issueTable.setGridColor(ModernUI.BORDER_COLOR);
        issueTable.setSelectionBackground(new Color(241, 245, 249));

        JTableHeader header = issueTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(issueTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        tableContainer.add(scrollPane);
        add(tableContainer, BorderLayout.CENTER);

        // Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionPanel.setOpaque(false);

        ModernUI.ModernButton viewDetailsButton = new ModernUI.ModernButton("View Details");
        ModernUI.ModernButton reportNewButton = new ModernUI.ModernButton("Report Issue");
        
        reportNewButton.setBackground(ModernUI.PRIMARY_COLOR);

        if ("OFFICIAL".equals(Session.role)) {
            ModernUI.ModernButton reportButton = new ModernUI.ModernButton("Reports");
            reportButton.addActionListener(e -> MainDashboard.showView(new IssueReportScreen()));
            actionPanel.add(reportButton);
            actionPanel.add(Box.createHorizontalStrut(20));
        }

        actionPanel.add(viewDetailsButton);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(reportNewButton);

        add(actionPanel, BorderLayout.SOUTH);

        // Actions
        reportNewButton.addActionListener(e -> MainDashboard.showView(new IssueCategorySelectionScreen()));

        viewDetailsButton.addActionListener(e -> {
            int row = issueTable.getSelectedRow();
            if (row >= 0) {
                int issueId = (int) tableModel.getValueAt(row, 0);
                MainDashboard.showView(new IssueDetailsScreen(issueId));
            } else {
                JOptionPane.showMessageDialog(this, "Select an issue first");
            }
        });

        refreshIssues();
    }

    private void refreshIssues() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query;
            if ("OFFICIAL".equals(Session.role)) {
                query = "SELECT * FROM PollingIssues ORDER BY reported_at DESC";
            } else {
                query = "SELECT * FROM PollingIssues WHERE voter_id = ? ORDER BY reported_at DESC";
            }
            PreparedStatement pstmt = conn.prepareStatement(query);
            if (!"OFFICIAL".equals(Session.role)) {
                pstmt.setInt(1, Session.userId);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("issue_id"),
                    rs.getString("category"),
                    rs.getInt("booth_id"),
                    rs.getString("urgency_level"),
                    rs.getString("status"),
                    rs.getTimestamp("reported_at")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching issues: " + ex.getMessage());
        }
    }
}
