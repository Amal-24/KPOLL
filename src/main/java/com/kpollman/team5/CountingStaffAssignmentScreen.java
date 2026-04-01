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

public class CountingStaffAssignmentScreen extends JPanel {
    private int centerId;
    private JTable tableTable;
    private DefaultTableModel tableModel;

    public CountingStaffAssignmentScreen(int centerId) {
        this.centerId = centerId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Staff Assignment (Center: " + centerId + ")");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Table ID", "Constituency", "Supervisor", "Assistant"};
        tableModel = new DefaultTableModel(columns, 0);
        tableTable = new JTable(tableModel);
        tableTable.setFont(ModernUI.MAIN_FONT);
        tableTable.setRowHeight(40);
        tableTable.setShowVerticalLines(false);
        tableTable.setGridColor(ModernUI.BORDER_COLOR);
        tableTable.setSelectionBackground(new Color(241, 245, 249));
        
        JTableHeader header = tableTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(tableTable);
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
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Staff List");
        refreshBtn.addActionListener(e -> refreshStaff());
        
        ModernUI.ModernButton assignSupBtn = new ModernUI.ModernButton("Assign Supervisor");
        assignSupBtn.setBackground(ModernUI.ACCENT_COLOR);
        assignSupBtn.addActionListener(e -> assignStaff("supervisor_name"));

        ModernUI.ModernButton assignAsstBtn = new ModernUI.ModernButton("Assign Assistant");
        assignAsstBtn.setBackground(ModernUI.PRIMARY_COLOR);
        assignAsstBtn.addActionListener(e -> assignStaff("assistant_name"));

        actionPanel.add(refreshBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(assignSupBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(assignAsstBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshStaff();
    }

    private void refreshStaff() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT ct.*, c.constituency_name FROM CountingTables ct LEFT JOIN Constituencies c ON ct.constituency_id = c.constituency_id WHERE ct.center_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, centerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("table_id"),
                    rs.getString("constituency_name"),
                    rs.getString("supervisor_name"),
                    rs.getString("assistant_name")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching staff: " + ex.getMessage());
        }
    }

    private void assignStaff(String column) {
        int selectedRow = tableTable.getSelectedRow();
        if (selectedRow >= 0) {
            int tableId = (int) tableModel.getValueAt(selectedRow, 0);
            String label = column.contains("supervisor") ? "Supervisor" : "Assistant";
            String name = JOptionPane.showInputDialog(this, "Enter Name for " + label + ":");
            if (name != null && !name.isEmpty()) {
                try (Connection conn = DatabaseHelper.getConnection()) {
                    String updateQuery = "UPDATE CountingTables SET " + column + " = ? WHERE table_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setString(1, name);
                    pstmt.setInt(2, tableId);
                    pstmt.executeUpdate();
                    refreshStaff();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error assigning staff: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a table to assign staff");
        }
    }
}
