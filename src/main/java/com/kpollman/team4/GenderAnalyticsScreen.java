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

public class GenderAnalyticsScreen extends JPanel {
    private final DefaultTableModel tableModel;
    private final JTable genderTable;

    public GenderAnalyticsScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Constituency-wise Gender Analysis");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new TurnoutDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {
            "ID", "Constituency", "Male", "Female", "Third Gender",
            "Total Turnout", "Male %", "Female %", "Third %"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        genderTable = new JTable(tableModel);
        genderTable.setFont(ModernUI.MAIN_FONT);
        genderTable.setRowHeight(40);
        genderTable.setShowVerticalLines(false);
        genderTable.setGridColor(ModernUI.BORDER_COLOR);
        genderTable.setSelectionBackground(new Color(0, 120, 215));
        genderTable.setSelectionForeground(Color.WHITE);

        JTableHeader header = genderTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(genderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);

        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Analysis");
        refreshBtn.addActionListener(e -> fetchGenderStats());
        footer.add(refreshBtn);
        add(footer, BorderLayout.SOUTH);

        fetchGenderStats();
    }

    private void fetchGenderStats() {
        tableModel.setRowCount(0);
        boolean dataFound = false;

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_id, c.constituency_name, " +
                           "COALESCE(SUM(ht.male_votes), 0) as male, " +
                           "COALESCE(SUM(ht.female_votes), 0) as female, " +
                           "COALESCE(SUM(ht.third_gender_votes), 0) as third " +
                           "FROM Constituencies c " +
                           "LEFT JOIN Booths b ON c.constituency_id = b.constituency_id " +
                           "LEFT JOIN HourlyTurnout ht ON b.booth_id = ht.booth_id " +
                           "GROUP BY c.constituency_id, c.constituency_name " +
                           "ORDER BY c.constituency_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int male = rs.getInt("male");
                int female = rs.getInt("female");
                int third = rs.getInt("third");
                int total = male + female + third;

                tableModel.addRow(new Object[] {
                    rs.getInt("constituency_id"),
                    rs.getString("constituency_name"),
                    String.format("%,d", male),
                    String.format("%,d", female),
                    String.format("%,d", third),
                    String.format("%,d", total),
                    formatPercentage(male, total),
                    formatPercentage(female, total),
                    formatPercentage(third, total)
                });
                dataFound = true;
            }
        } catch (Exception e) {
            System.err.println("Gender stats fetch error: " + e.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[] {0, "No data available", "0", "0", "0", "0", "0.00%", "0.00%", "0.00%"});
        }
    }

    private String formatPercentage(int value, int total) {
        if (total <= 0) {
            return "0.00%";
        }
        return String.format("%.2f%%", (value * 100.0) / total);
    }
}
