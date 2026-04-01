package com.kpollman.team1;

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

public class VotedStatusManagementScreen extends JPanel {
    private int boothId;
    private JTable votedTable;
    private DefaultTableModel tableModel;

    public VotedStatusManagementScreen(int boothId) {
        this.boothId = boothId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Voted Status Management");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new BoothDashboard(boothId, "Current Booth", 0)));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"EPIC No", "Name", "Serial No", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        votedTable = new JTable(tableModel);
        votedTable.setFont(ModernUI.MAIN_FONT);
        votedTable.setRowHeight(40);
        votedTable.setShowVerticalLines(false);
        votedTable.setGridColor(ModernUI.BORDER_COLOR);
        
        JTableHeader header = votedTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(votedTable);
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
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Voted List");
        refreshBtn.addActionListener(e -> refreshVotedList());
        
        actionPanel.add(refreshBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshVotedList();
    }

    private void refreshVotedList() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT * FROM Voters WHERE booth_id = ? AND voted_status = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, boothId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("epic_no"),
                    rs.getString("voter_name"),
                    rs.getInt("serial_no"),
                    "VOTED"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
