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
 * Counting Table Management Screen to add, edit, and assign tables.
 */
public class CountingTableManagementScreen extends JPanel {
    private JTable tablesTable;
    private DefaultTableModel tableModel;

    public CountingTableManagementScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Counting Table Management");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Table ID", "Center", "Table Number", "Supervisor", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        tablesTable = new JTable(tableModel);
        tablesTable.setFont(ModernUI.MAIN_FONT);
        tablesTable.setRowHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(tablesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton addTableBtn = new ModernUI.ModernButton("Add New Table");
        addTableBtn.setBackground(ModernUI.PRIMARY_COLOR);
        addTableBtn.addActionListener(e -> addNewTable());
        
        ModernUI.ModernButton assignStaffBtn = new ModernUI.ModernButton("Assign Staff");
        assignStaffBtn.addActionListener(e -> MainDashboard.showView(new CountingStaffAssignmentScreen()));

        actionPanel.add(addTableBtn);
        actionPanel.add(assignStaffBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshTables();
    }

    private void addNewTable() {
        // Implementation for adding a new table (simplified)
        JOptionPane.showMessageDialog(this, "Add New Table Dialog - Logic to be implemented.");
    }

    private void refreshTables() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT ct.*, cc.center_name FROM CountingTables ct " +
                           "JOIN CountingCenters cc ON ct.center_id = cc.center_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("table_id"),
                    rs.getString("center_name"),
                    rs.getInt("table_number"),
                    rs.getString("supervisor_name"),
                    rs.getString("status")
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Counting tables fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            // Mock data for preview
            tableModel.addRow(new Object[]{1, "Trivandrum Central", 1, "Officer A", "Active"});
            tableModel.addRow(new Object[]{2, "Trivandrum Central", 2, "Officer B", "Active"});
            tableModel.addRow(new Object[]{3, "Kochi Collectorate", 1, "Officer C", "Inactive"});
        }
    }
}
