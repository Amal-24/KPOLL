package com.kpollman.team6;

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

public class ResultAggregationScreen extends JPanel {
    private JTable aggregationTable;
    private DefaultTableModel tableModel;

    public ResultAggregationScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Result Aggregation Dashboard");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Data");
        refreshBtn.addActionListener(e -> refreshAggregation());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Candidate Name", "Constituency", "Aggregated Votes"};
        tableModel = new DefaultTableModel(columns, 0);
        aggregationTable = new JTable(tableModel);
        aggregationTable.setFont(ModernUI.MAIN_FONT);
        aggregationTable.setRowHeight(40);
        aggregationTable.setShowVerticalLines(false);
        aggregationTable.setGridColor(ModernUI.BORDER_COLOR);
        aggregationTable.setSelectionBackground(new Color(241, 245, 249));
        
        JTableHeader header = aggregationTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(aggregationTable);
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
        
        ModernUI.ModernButton aggregateBtn = new ModernUI.ModernButton("Aggregate All Results");
        aggregateBtn.addActionListener(e -> aggregateResults());
        
        ModernUI.ModernButton winnerBtn = new ModernUI.ModernButton("Declare Winners");
        winnerBtn.setBackground(ModernUI.ACCENT_COLOR);
        winnerBtn.addActionListener(e -> MainDashboard.showView(new WinnerDeclarationScreen()));

        ModernUI.ModernButton marginBtn = new ModernUI.ModernButton("Margin Calculation");
        marginBtn.setBackground(ModernUI.PRIMARY_COLOR);
        marginBtn.addActionListener(e -> MainDashboard.showView(new MarginCalculationScreen()));

        actionPanel.add(aggregateBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(winnerBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(marginBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshAggregation();
    }

    private void refreshAggregation() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.candidate_id, c.candidate_name, con.constituency_name, SUM(rr.votes_counted) as total_votes " +
                           "FROM Candidates c " +
                           "JOIN Constituencies con ON c.constituency_id = con.constituency_id " +
                           "JOIN RoundResults rr ON c.candidate_id = rr.candidate_id " +
                           "WHERE rr.is_verified = TRUE " +
                           "GROUP BY c.candidate_id, c.candidate_name, con.constituency_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("candidate_id"),
                    rs.getString("candidate_name"),
                    rs.getString("constituency_name"),
                    String.format("%,d", rs.getInt("total_votes"))
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching results: " + ex.getMessage());
        }
    }

    private void aggregateResults() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String aggregateQuery = "INSERT INTO FinalResults (candidate_id, constituency_id, total_votes) " +
                                    "SELECT c.candidate_id, c.constituency_id, SUM(rr.votes_counted) " +
                                    "FROM Candidates c JOIN RoundResults rr ON c.candidate_id = rr.candidate_id " +
                                    "WHERE rr.is_verified = TRUE GROUP BY c.candidate_id, c.constituency_id " +
                                    "ON DUPLICATE KEY UPDATE total_votes = VALUES(total_votes)";
            PreparedStatement pstmt = conn.prepareStatement(aggregateQuery);
            pstmt.executeUpdate();
            refreshAggregation();
            JOptionPane.showMessageDialog(this, "Successfully aggregated results!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Aggregation Error: " + ex.getMessage());
        }
    }
}
