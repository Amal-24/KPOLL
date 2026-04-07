package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Counting Center Dashboard providing an overview of all counting centers.
 */
public class CountingCenterDashboard extends JPanel {
    private JTable centersTable;
    private DefaultTableModel tableModel;

    public CountingCenterDashboard() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Counting Center Management Dashboard");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Centers");
        refreshBtn.addActionListener(e -> refreshDashboard());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Center Name", "Location", "Tables Count", "Total Rounds"};
        tableModel = new DefaultTableModel(columns, 0);
        centersTable = new JTable(tableModel);
        centersTable.setFont(ModernUI.MAIN_FONT);
        centersTable.setRowHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(centersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton tableMgmtBtn = new ModernUI.ModernButton("Table Management");
        tableMgmtBtn.addActionListener(e -> MainDashboard.showView(new CountingTableManagementScreen()));
        
        ModernUI.ModernButton resultEntryBtn = new ModernUI.ModernButton("Result Entry");
        resultEntryBtn.addActionListener(e -> MainDashboard.showView(new RoundWiseResultEntryScreen()));

        ModernUI.ModernButton progressBtn = new ModernUI.ModernButton("Progress Tracker");
        progressBtn.setBackground(ModernUI.ACCENT_COLOR);
        progressBtn.addActionListener(e -> MainDashboard.showView(new CountingProgressTrackerScreen()));

        ModernUI.ModernButton verifyBtn = new ModernUI.ModernButton("Result Verification");
        verifyBtn.addActionListener(e -> MainDashboard.showView(new ResultVerificationScreen()));

        ModernUI.ModernButton staffBtn = new ModernUI.ModernButton("Staff Assignment");
        staffBtn.addActionListener(e -> MainDashboard.showView(new CountingStaffAssignmentScreen()));

        actionPanel.add(tableMgmtBtn);
        actionPanel.add(resultEntryBtn);
        actionPanel.add(progressBtn);
        actionPanel.add(verifyBtn);
        actionPanel.add(staffBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshDashboard();
    }

    private void refreshDashboard() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT cc.*, (SELECT COUNT(*) FROM CountingTables WHERE center_id = cc.center_id) as tables_count " +
                           "FROM CountingCenters cc";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("center_id"),
                    rs.getString("center_name"),
                    rs.getString("location"),
                    rs.getInt("tables_count"),
                    "Pending" // Rounds logic can be added later
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Counting centers fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{0, "No counting centers configured", "-", 0, "0"});
        }
    }
}
