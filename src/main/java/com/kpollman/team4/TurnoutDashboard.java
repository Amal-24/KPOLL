package com.kpollman.team4;

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

public class TurnoutDashboard extends JPanel {
    private JTable turnoutTable;
    private DefaultTableModel tableModel;
    private AnalyticsEngine engine;

    public TurnoutDashboard() {
        this.engine = new KeralaElectionAnalytics();
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Turnout Analytics Dashboard");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Stats");
        refreshBtn.addActionListener(e -> refreshTurnoutStats());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Constituency Name", "Male", "Female", "TG", "Total", "Turnout %"};
        tableModel = new DefaultTableModel(columns, 0);
        turnoutTable = new JTable(tableModel);
        turnoutTable.setFont(ModernUI.MAIN_FONT);
        turnoutTable.setRowHeight(40);
        turnoutTable.setShowVerticalLines(false);
        turnoutTable.setGridColor(ModernUI.BORDER_COLOR);
        turnoutTable.setSelectionBackground(new Color(241, 245, 249));
        
        JTableHeader header = turnoutTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(turnoutTable);
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
        
        ModernUI.ModernButton genderBtn = new ModernUI.ModernButton("Gender Analytics");
        genderBtn.addActionListener(e -> MainDashboard.showView(new GenderAnalyticsScreen()));
        
        ModernUI.ModernButton reportBtn = new ModernUI.ModernButton("Generate Report");
        reportBtn.setBackground(ModernUI.ACCENT_COLOR);
        reportBtn.addActionListener(e -> MainDashboard.showView(new TurnoutReportGeneratorScreen()));

        actionPanel.add(genderBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(reportBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshTurnoutStats();
    }

    private void refreshTurnoutStats() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_id, c.constituency_name, c.total_voters, " +
                           "SUM(ht.male_votes) as male, SUM(ht.female_votes) as female, SUM(ht.third_gender_votes) as third " +
                           "FROM Constituencies c " +
                           "LEFT JOIN Booths b ON c.constituency_id = b.constituency_id " +
                           "LEFT JOIN HourlyTurnout ht ON b.booth_id = ht.booth_id " +
                           "GROUP BY c.constituency_id, c.constituency_name, c.total_voters";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("constituency_id");
                String name = rs.getString("constituency_name");
                int male = rs.getInt("male");
                int female = rs.getInt("female");
                int third = rs.getInt("third");
                int totalVoted = male + female + third;
                int totalRegistered = rs.getInt("total_voters");

                double percentage = engine.calculateTurnoutPercentage(totalVoted, totalRegistered);

                tableModel.addRow(new Object[]{
                    id, name, String.format("%,d", male), String.format("%,d", female), 
                    String.format("%,d", third), String.format("%,d", totalVoted),
                    String.format("%.2f%%", percentage)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching turnout: " + ex.getMessage());
        }
    }
}
