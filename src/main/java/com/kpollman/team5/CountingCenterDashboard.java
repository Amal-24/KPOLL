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
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        centersTable = new JTable(tableModel);
        centersTable.setFont(ModernUI.MAIN_FONT);
        centersTable.setRowHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(centersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setOpaque(false);

        ModernUI.ModernButton addCenterBtn = new ModernUI.ModernButton("Add Center");
        addCenterBtn.setBackground(ModernUI.PRIMARY_COLOR);
        addCenterBtn.addActionListener(e -> addCenter());

        ModernUI.ModernButton editCenterBtn = new ModernUI.ModernButton("Edit Center");
        editCenterBtn.addActionListener(e -> editSelectedCenter());

        ModernUI.ModernButton deleteCenterBtn = new ModernUI.ModernButton("Delete Center");
        deleteCenterBtn.setBackground(new Color(239, 68, 68));
        deleteCenterBtn.addActionListener(e -> deleteSelectedCenter());
        
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

        actionPanel.add(addCenterBtn);
        actionPanel.add(editCenterBtn);
        actionPanel.add(deleteCenterBtn);
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
            String query = "SELECT cc.center_id, cc.center_name, cc.location, " +
                           "COUNT(DISTINCT ct.table_id) as tables_count, " +
                           "COUNT(DISTINCT CONCAT(ct.table_id, '-', rr.round_no)) as total_rounds " +
                           "FROM CountingCenters cc " +
                           "LEFT JOIN CountingTables ct ON ct.center_id = cc.center_id " +
                           "LEFT JOIN RoundResults rr ON rr.table_id = ct.table_id " +
                           "GROUP BY cc.center_id, cc.center_name, cc.location " +
                           "ORDER BY cc.center_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("center_id"),
                    rs.getString("center_name"),
                    rs.getString("location"),
                    rs.getInt("tables_count"),
                    rs.getInt("total_rounds")
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Counting centers fetch error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to load centers: " + ex.getMessage());
        }

        if (!dataFound) {
            // Keep table empty instead of showing a fake row.
            // This avoids looking like hardcoded/unresponsive data.
        }
        tableModel.fireTableDataChanged();
    }

    private void addCenter() {
        JTextField centerNameField = new JTextField();
        JTextField locationField = new JTextField();
        Object[] form = {
            "Center Name", centerNameField,
            "Location", locationField
        };

        int choice = JOptionPane.showConfirmDialog(this, form, "Add Counting Center", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        String centerName = centerNameField.getText() == null ? "" : centerNameField.getText().trim();
        String location = locationField.getText() == null ? "" : locationField.getText().trim();
        if (centerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Center name is required.");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "INSERT INTO CountingCenters (center_name, location) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, centerName);
            pstmt.setString(2, location.isEmpty() ? null : location);
            pstmt.executeUpdate();
            refreshDashboard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to add center: " + ex.getMessage());
        }
    }

    private void editSelectedCenter() {
        int row = centersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a center to edit.");
            return;
        }

        int centerId = (int) tableModel.getValueAt(row, 0);
        String currentName = String.valueOf(tableModel.getValueAt(row, 1));
        String currentLocation = String.valueOf(tableModel.getValueAt(row, 2));
        if ("null".equalsIgnoreCase(currentLocation) || "-".equals(currentLocation)) currentLocation = "";

        JTextField centerNameField = new JTextField(currentName);
        JTextField locationField = new JTextField(currentLocation);
        Object[] form = {
            "Center Name", centerNameField,
            "Location", locationField
        };

        int choice = JOptionPane.showConfirmDialog(this, form, "Edit Counting Center", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        String centerName = centerNameField.getText() == null ? "" : centerNameField.getText().trim();
        String location = locationField.getText() == null ? "" : locationField.getText().trim();
        if (centerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Center name is required.");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "UPDATE CountingCenters SET center_name = ?, location = ? WHERE center_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, centerName);
            pstmt.setString(2, location.isEmpty() ? null : location);
            pstmt.setInt(3, centerId);
            pstmt.executeUpdate();
            refreshDashboard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update center: " + ex.getMessage());
        }
    }

    private void deleteSelectedCenter() {
        int row = centersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a center to delete.");
            return;
        }

        int centerId = (int) tableModel.getValueAt(row, 0);
        String centerName = String.valueOf(tableModel.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete center '" + centerName + "' (ID: " + centerId + ")?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "DELETE FROM CountingCenters WHERE center_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, centerId);
            pstmt.executeUpdate();
            refreshDashboard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to delete center. Remove dependent tables first.\n" + ex.getMessage());
        }
    }
}
