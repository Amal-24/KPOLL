package com.kpollman.team3;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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

        // Status color renderer
        issueTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(new Color(173, 216, 230)); // Light blue for selected row
                } else {
                    String status = (String) table.getValueAt(row, 4);

                    if ("Open".equals(status))
                        c.setBackground(new Color(254, 202, 202));
                    else if ("In-Progress".equals(status))
                        c.setBackground(new Color(254, 249, 195));
                    else if ("Resolved".equals(status))
                        c.setBackground(new Color(187, 247, 208));
                    else
                        c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

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
        reportNewButton.addActionListener(e ->
                MainDashboard.showView(new IssueCategorySelectionScreen()));

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

        System.out.println("=== REFRESHING DASHBOARD ===");
        System.out.println("Current Session Role: " + Session.role);
        System.out.println("Current Session UserId: " + Session.userId);

        try (Connection conn = DatabaseHelper.getConnection()) {

            String query;
            PreparedStatement pstmt;

            if ("OFFICIAL".equals(Session.role)) {
                query = "SELECT * FROM PollingIssues ORDER BY reported_at DESC";
                pstmt = conn.prepareStatement(query);
                System.out.println("🔵 Official mode - fetching ALL issues");
            } else if ("VOTER".equals(Session.role)) {
                query = "SELECT * FROM PollingIssues WHERE reported_by = ? ORDER BY reported_at DESC";
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, Session.userId);
                System.out.println("🟢 Voter mode - fetching ONLY issues for user: " + Session.userId);
            } else {
                System.out.println("⚠ Unknown role: " + Session.role);
                return;
            }

            ResultSet rs = pstmt.executeQuery();
            int rowCount = 0;

            while (rs.next()) {
                int reportedBy = rs.getInt("reported_by");
                
                // Extra safety check
                if ("VOTER".equals(Session.role) && reportedBy != Session.userId) {
                    System.out.println("⚠ SKIPPING issue " + rs.getInt("issue_id") + 
                                       " (belongs to user " + reportedBy + ")");
                    continue;
                }
                
                tableModel.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getString("category"),
                        rs.getInt("booth_id"),
                        rs.getString("urgency_level"),
                        rs.getString("status"),
                        rs.getTimestamp("reported_at")
                });
                rowCount++;
            }
            
            System.out.println("✅ Loaded " + rowCount + " issues");
            System.out.println("================================");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching issues: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}