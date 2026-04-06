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
 * Historical Comparison Screen to compare current turnout with 2021 and 2016 elections.
 */
public class HistoricalComparisonScreen extends JPanel {
    private JTable comparisonTable;
    private DefaultTableModel tableModel;
    private AnalyticsEngine engine;

    public HistoricalComparisonScreen() {
        this.engine = new KeralaElectionAnalytics();
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Historical Turnout Comparison (2026 vs 2021 vs 2016)");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new TurnoutDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        String[] columns = {"Constituency", "2026 (Current) %", "2021 (Prev) %", "2016 (Old) %", "Growth %"};
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
        refreshBtn.addActionListener(e -> fetchHistoricalData());
        footer.add(refreshBtn);
        add(footer, BorderLayout.SOUTH);

        fetchHistoricalData();
    }

    private void fetchHistoricalData() {
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
                double currentPercentage = engine.calculateTurnoutPercentage(totalVoted, totalVoters);
                
                // Mock historical data (usually this would be in the database)
                double prevPercentage = 74.5 + (Math.random() * 5 - 2.5); // Randomly mock 2021
                double oldPercentage = 71.2 + (Math.random() * 5 - 2.5); // Randomly mock 2016
                double growth = currentPercentage - prevPercentage;

                tableModel.addRow(new Object[]{
                    name, 
                    String.format("%.2f%%", currentPercentage), 
                    String.format("%.2f%%", prevPercentage), 
                    String.format("%.2f%%", oldPercentage),
                    String.format("%.2f%%", growth)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
