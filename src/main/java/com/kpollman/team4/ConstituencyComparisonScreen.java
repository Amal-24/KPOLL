package com.kpollman.team4;

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
 * Constituency-wise Comparison Screen to compare turnout across constituencies.
 */
public class ConstituencyComparisonScreen extends JPanel {
    private JTable comparisonTable;
    private DefaultTableModel tableModel;
    private AnalyticsEngine engine;

    public ConstituencyComparisonScreen() {
        this.engine = new KeralaElectionAnalytics();
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Constituency-wise Turnout Comparison");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new TurnoutDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        String[] columns = {"Constituency", "Total Voters", "Voted Count", "Turnout %", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        comparisonTable = new JTable(tableModel);
        comparisonTable.setFont(ModernUI.MAIN_FONT);
        comparisonTable.setRowHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(comparisonTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Comparison");
        refreshBtn.addActionListener(e -> fetchComparisonData());
        footer.add(refreshBtn);
        add(footer, BorderLayout.SOUTH);

        fetchComparisonData();
    }

    private void fetchComparisonData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_name, c.total_voters, " +
                           "SUM(ht.male_votes + ht.female_votes + ht.third_gender_votes) as total_voted " +
                           "FROM Constituencies c " +
                           "LEFT JOIN Booths b ON c.constituency_id = b.constituency_id " +
                           "LEFT JOIN HourlyTurnout ht ON b.booth_id = ht.booth_id " +
                           "GROUP BY c.constituency_name, c.total_voters";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("constituency_name");
                int totalVoters = rs.getInt("total_voters");
                int totalVoted = rs.getInt("total_voted");
                double percentage = engine.calculateTurnoutPercentage(totalVoted, totalVoters);
                
                String status = percentage > 75 ? "High" : (percentage > 50 ? "Medium" : "Low");
                
                tableModel.addRow(new Object[]{
                    name, 
                    String.format("%,d", totalVoters), 
                    String.format("%,d", totalVoted), 
                    String.format("%.2f%%", percentage),
                    status
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
