package com.kpollman.team2;

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

public class QueueStatusDashboard extends JPanel {
    private JTable queueTable;
    private DefaultTableModel tableModel;

    public QueueStatusDashboard() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Real-Time Queue Status");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Data");
        refreshBtn.addActionListener(e -> refreshQueueData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Booth ID", "Booth Name", "Queue Length", "Avg Wait (min)", "Stations", "Last Updated"};
        tableModel = new DefaultTableModel(columns, 0);
        queueTable = new JTable(tableModel);
        queueTable.setFont(ModernUI.MAIN_FONT);
        queueTable.setRowHeight(40);
        queueTable.setShowVerticalLines(false);
        queueTable.setGridColor(ModernUI.BORDER_COLOR);
        queueTable.setSelectionBackground(new Color(241, 245, 249));
        queueTable.setSelectionForeground(ModernUI.TEXT_COLOR_DARK);
        
        JTableHeader header = queueTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(queueTable);
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
        
        ModernUI.ModernButton updateBtn = new ModernUI.ModernButton("Update Queue");
        updateBtn.addActionListener(e -> handleUpdate());
        
        ModernUI.ModernButton viewBtn = new ModernUI.ModernButton("Voter View");
        viewBtn.setBackground(ModernUI.ACCENT_COLOR);
        viewBtn.addActionListener(e -> handleView());

        actionPanel.add(updateBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(viewBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshQueueData();
    }

    private void handleUpdate() {
        String boothIdInput = JOptionPane.showInputDialog(this, "Enter Booth ID to update:");
        if (boothIdInput != null && !boothIdInput.isEmpty()) {
            try {
                int boothId = Integer.parseInt(boothIdInput);
                MainDashboard.showView(new QueueLengthUpdaterScreen(boothId));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Booth ID");
            }
        }
    }

    private void handleView() {
        String boothIdInput = JOptionPane.showInputDialog(this, "Enter Booth ID to view:");
        if (boothIdInput != null && !boothIdInput.isEmpty()) {
            try {
                int boothId = Integer.parseInt(boothIdInput);
                MainDashboard.showView(new VoterQueueViewScreen(boothId));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Booth ID");
            }
        }
    }

    private void refreshQueueData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT q.*, b.booth_name FROM QueueStatus q JOIN Booths b ON q.booth_id = b.booth_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("booth_id"),
                    rs.getString("booth_name"),
                    rs.getInt("current_queue_length"),
                    rs.getInt("avg_wait_time_mins"),
                    rs.getInt("active_stations"),
                    rs.getTimestamp("last_updated")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching queue data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
