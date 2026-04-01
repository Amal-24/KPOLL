package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CountingCenterDashboard extends JPanel {
    private JTable centerTable;
    private DefaultTableModel tableModel;

    public CountingCenterDashboard() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Counting Center Management");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Centers");
        refreshBtn.addActionListener(e -> refreshCenters());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Center ID", "Center Name", "Location", "Total Tables"};
        tableModel = new DefaultTableModel(columns, 0);
        centerTable = new JTable(tableModel);
        centerTable.setFont(ModernUI.MAIN_FONT);
        centerTable.setRowHeight(40);
        centerTable.setShowVerticalLines(false);
        centerTable.setGridColor(ModernUI.BORDER_COLOR);
        centerTable.setSelectionBackground(new Color(241, 245, 249));
        
        JTableHeader header = centerTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(centerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        tableContainer.add(scrollPane);
        add(tableContainer, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton manageBtn = new ModernUI.ModernButton("Manage Tables");
        manageBtn.addActionListener(e -> handleManageTables());
        
        ModernUI.ModernButton staffBtn = new ModernUI.ModernButton("Staff Assignment");
        staffBtn.setBackground(ModernUI.ACCENT_COLOR);
        staffBtn.addActionListener(e -> handleStaffAssignment());

        ModernUI.ModernButton progressBtn = new ModernUI.ModernButton("Progress Tracker");
        progressBtn.setBackground(ModernUI.PRIMARY_COLOR);
        progressBtn.addActionListener(e -> MainDashboard.showView(new CountingProgressTrackerScreen()));

        actionPanel.add(manageBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(staffBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(progressBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshCenters();
    }

    private void handleManageTables() {
        int selectedRow = centerTable.getSelectedRow();
        if (selectedRow >= 0) {
            int centerId = (int) tableModel.getValueAt(selectedRow, 0);
            MainDashboard.showView(new CountingTableManagementScreen(centerId));
        } else {
            JOptionPane.showMessageDialog(this, "Please select a center to manage tables");
        }
    }

    private void handleStaffAssignment() {
        int selectedRow = centerTable.getSelectedRow();
        if (selectedRow >= 0) {
            int centerId = (int) tableModel.getValueAt(selectedRow, 0);
            MainDashboard.showView(new CountingStaffAssignmentScreen(centerId));
        } else {
            JOptionPane.showMessageDialog(this, "Please select a center for staff assignment");
        }
    }

    private void refreshCenters() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT cc.*, COUNT(ct.table_id) as total_tables FROM CountingCenters cc LEFT JOIN CountingTables ct ON cc.center_id = ct.center_id GROUP BY cc.center_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("center_id"),
                    rs.getString("center_name"),
                    rs.getString("location"),
                    rs.getInt("total_tables")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching centers: " + ex.getMessage());
        }
    }
}
