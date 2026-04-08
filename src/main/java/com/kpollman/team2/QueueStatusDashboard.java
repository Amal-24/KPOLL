package com.kpollman.team2;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueueStatusDashboard extends JPanel {
    private JTable queueTable;
    private DefaultTableModel tableModel;
    private JPanel graphPanel;

    public QueueStatusDashboard() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Real-Time Queue Status Dashboard");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Dashboard");
        refreshBtn.addActionListener(e -> refreshQueueData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center split: Table and Graph
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Table
        String[] columns = {"Booth ID", "Booth Name", "Queue Length", "Avg Wait (min)", "Stations", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        queueTable = new JTable(tableModel);
        queueTable.setFont(ModernUI.MAIN_FONT);
        queueTable.setRowHeight(40);
        queueTable.setShowVerticalLines(false);
        queueTable.setGridColor(ModernUI.BORDER_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(queueTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1));
        splitPane.setTopComponent(scrollPane);

        // Graphical Panel (Bar representation)
        graphPanel = new JPanel();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR), "Queue Length Visualization (Graphical)"));
        
        JScrollPane graphScroll = new JScrollPane(graphPanel);
        graphScroll.setBorder(null);
        splitPane.setBottomComponent(graphScroll);

        add(splitPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton updateBtn = new ModernUI.ModernButton("Update Queue");
        updateBtn.addActionListener(e -> handleUpdate());
        
        ModernUI.ModernButton viewBtn = new ModernUI.ModernButton("Voter Queue View");
        viewBtn.setBackground(ModernUI.ACCENT_COLOR);
        viewBtn.addActionListener(e -> handleView());

        ModernUI.ModernButton calcBtn = new ModernUI.ModernButton("Wait Time Calculator");
        calcBtn.addActionListener(e -> MainDashboard.showView(new WaitTimeCalculatorScreen()));

        ModernUI.ModernButton flowBtn = new ModernUI.ModernButton("Flow Management");
        flowBtn.addActionListener(e -> MainDashboard.showView(new FlowManagementScreen()));

        ModernUI.ModernButton capacityBtn = new ModernUI.ModernButton("Capacity Monitor");
        capacityBtn.addActionListener(e -> handleCapacityMonitor());

        actionPanel.add(updateBtn);
        actionPanel.add(viewBtn);
        actionPanel.add(calcBtn);
        actionPanel.add(flowBtn);
        actionPanel.add(capacityBtn);
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

    private void handleCapacityMonitor() {
        String boothIdInput = JOptionPane.showInputDialog(this, "Enter Booth ID to monitor capacity:");
        if (boothIdInput != null && !boothIdInput.isEmpty()) {
            try {
                int boothId = Integer.parseInt(boothIdInput);
                new BoothCapacityMonitorScreen(boothId).setVisible(true);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Booth ID");
            }
        }
    }

    private void refreshQueueData() {
        tableModel.setRowCount(0);
        graphPanel.removeAll();
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT b.booth_id, b.booth_name, COALESCE(q.current_queue_length, 0) as current_queue_length, " +
                          "COALESCE(q.avg_wait_time_mins, 0) as avg_wait_time_mins, " +
                          "COALESCE(q.active_stations, 1) as active_stations " +
                          "FROM Booths b LEFT JOIN QueueStatus q ON b.booth_id = q.booth_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String boothName = rs.getString("booth_name");
                int qLen = rs.getInt("current_queue_length");
                int waitTime = rs.getInt("avg_wait_time_mins");
                int stations = rs.getInt("active_stations");
                int boothId = rs.getInt("booth_id");

                String status = waitTime > 30 ? "HEAVY" : (waitTime > 15 ? "MODERATE" : "NORMAL");
                tableModel.addRow(new Object[]{boothId, boothName, qLen, waitTime, stations, status});

                addGraphicalBar(boothName, qLen, status);
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Queue data fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            // No data available - show empty state
            tableModel.addRow(new Object[]{"No Data", "No booths configured", 0, 0, 0, "N/A"});
        }

        graphPanel.revalidate();
        graphPanel.repaint();
    }



    private void addGraphicalBar(String boothName, int qLen, String status) {
        JPanel barRow = new JPanel(new BorderLayout(10, 0));
        barRow.setOpaque(false);
        barRow.setMaximumSize(new Dimension(800, 40));
        JLabel nameLbl = new JLabel(boothName);
        nameLbl.setPreferredSize(new Dimension(200, 30));
        
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(qLen);
        bar.setStringPainted(true);
        bar.setString(qLen + " People in Queue");
        bar.setForeground(status.equals("HEAVY") ? new Color(239, 68, 68) : (status.equals("MODERATE") ? new Color(245, 158, 11) : new Color(16, 185, 129)));
        
        barRow.add(nameLbl, BorderLayout.WEST);
        barRow.add(bar, BorderLayout.CENTER);
        graphPanel.add(barRow);
        graphPanel.add(Box.createVerticalStrut(5));
    }
}
