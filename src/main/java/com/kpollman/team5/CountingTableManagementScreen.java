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

public class CountingTableManagementScreen extends JPanel {
    private int centerId;
    private JTable tableTable;
    private DefaultTableModel tableModel;

    public CountingTableManagementScreen(int centerId) {
        this.centerId = centerId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Table Management (Center: " + centerId + ")");
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
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Tables");
        refreshBtn.addActionListener(e -> refreshTables());
        
        ModernUI.ModernButton addTableBtn = new ModernUI.ModernButton("Add New Table");
        addTableBtn.setBackground(ModernUI.ACCENT_COLOR);
        addTableBtn.addActionListener(e -> addNewTable());

        ModernUI.ModernButton resultBtn = new ModernUI.ModernButton("Enter Results");
        resultBtn.setBackground(ModernUI.PRIMARY_COLOR);
        resultBtn.addActionListener(e -> handleResultEntry());

        actionPanel.add(refreshBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(addTableBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(resultBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshTables();
    }

    private void handleResultEntry() {
        int selectedRow = tableTable.getSelectedRow();
        if (selectedRow >= 0) {
            int tableId = (int) tableModel.getValueAt(selectedRow, 0);
            MainDashboard.showView(new RoundWiseResultEntryScreen(tableId));
        } else {
            JOptionPane.showMessageDialog(this, "Please select a table for result entry");
        }
    }

    private void refreshTables() {
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
            JOptionPane.showMessageDialog(this, "Error fetching tables: " + ex.getMessage());
        }
    }

    private void addNewTable() {
        String supervisor = JOptionPane.showInputDialog(this, "Enter Supervisor Name:");
        String assistant = JOptionPane.showInputDialog(this, "Enter Assistant Name:");
        if (supervisor != null && assistant != null) {
            try (Connection conn = DatabaseHelper.getConnection()) {
                String insertQuery = "INSERT INTO CountingTables (center_id, supervisor_name, assistant_name) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setInt(1, centerId);
                pstmt.setString(2, supervisor);
                pstmt.setString(3, assistant);
                pstmt.executeUpdate();
                refreshTables();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding table: " + ex.getMessage());
            }
        }
    }
}
