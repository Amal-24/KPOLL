package com.kpollman.team7;

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

public class LiveResultsDashboard extends JPanel {
    private JTable trendsTable;
    private DefaultTableModel tableModel;

    public LiveResultsDashboard() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Live Election Results 2026");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Results");
        refreshBtn.addActionListener(e -> refreshLiveData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Constituency", "Leading Candidate", "Party", "Votes", "Margin", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        trendsTable = new JTable(tableModel);
        trendsTable.setFont(ModernUI.MAIN_FONT);
        trendsTable.setRowHeight(40);
        trendsTable.setShowVerticalLines(false);
        trendsTable.setGridColor(ModernUI.BORDER_COLOR);
        trendsTable.setSelectionBackground(new Color(241, 245, 249));
        
        JTableHeader header = trendsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(trendsTable);
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
        
        ModernUI.ModernButton tallyBtn = new ModernUI.ModernButton("Seat Tally");
        tallyBtn.addActionListener(e -> MainDashboard.showView(new PartySeatTallyScreen()));
        
        ModernUI.ModernButton mapBtn = new ModernUI.ModernButton("Visualization");
        mapBtn.setBackground(ModernUI.ACCENT_COLOR);
        mapBtn.addActionListener(e -> MainDashboard.showView(new ResultMapVisualizationScreen()));

        actionPanel.add(tallyBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(mapBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshLiveData();
    }

    private void refreshLiveData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_name, can.candidate_name, can.party_name, fr.total_votes, fr.is_winner " +
                           "FROM FinalResults fr " +
                           "JOIN Candidates can ON fr.candidate_id = can.candidate_id " +
                           "JOIN Constituencies c ON fr.constituency_id = c.constituency_id " +
                           "WHERE fr.total_votes = (SELECT MAX(total_votes) FROM FinalResults WHERE constituency_id = c.constituency_id)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("constituency_name"),
                    rs.getString("candidate_name"),
                    rs.getString("party_name"),
                    String.format("%,d", rs.getInt("total_votes")),
                    "Calculating...",
                    rs.getBoolean("is_winner") ? "WINNER DECLARED" : "LEADING"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching live results: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
