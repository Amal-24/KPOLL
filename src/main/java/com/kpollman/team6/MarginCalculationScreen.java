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

public class MarginCalculationScreen extends JPanel {
    private JTable marginTable;
    private DefaultTableModel tableModel;

    public MarginCalculationScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Margin and Vote Share Analysis");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new ResultAggregationScreen()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Constituency Name", "Winner", "Runner-up", "Margin", "Vote Share %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        marginTable = new JTable(tableModel);
        marginTable.setFont(ModernUI.MAIN_FONT);
        marginTable.setRowHeight(40);
        marginTable.setShowVerticalLines(false);
        marginTable.setGridColor(ModernUI.BORDER_COLOR);
        marginTable.setSelectionBackground(new Color(0, 120, 215));
        marginTable.setSelectionForeground(Color.WHITE);
        
        JTableHeader header = marginTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(marginTable);
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
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Recalculate Margins");
        refreshBtn.addActionListener(e -> calculateMargins());
        
        ModernUI.ModernButton exportBtn = new ModernUI.ModernButton("Export Analysis");
        exportBtn.setBackground(ModernUI.ACCENT_COLOR);

        actionPanel.add(refreshBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(exportBtn);
        add(actionPanel, BorderLayout.SOUTH);

        calculateMargins();
    }

    private void calculateMargins() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_id, c.constituency_name, " +
                           "r1.candidate_name as winner_name, " +
                           "r2.candidate_name as runner_up_name, " +
                           "(r1.total_votes - COALESCE(r2.total_votes, 0)) as margin, " +
                           "(r1.total_votes * 100.0 / (SELECT SUM(total_votes) FROM FinalResults WHERE constituency_id = c.constituency_id)) as vote_share " +
                           "FROM Constituencies c " +
                           "JOIN (SELECT fr.*, can.candidate_name, ROW_NUMBER() OVER(PARTITION BY constituency_id ORDER BY total_votes DESC) as pos FROM FinalResults fr JOIN Candidates can ON fr.candidate_id = can.candidate_id) r1 ON c.constituency_id = r1.constituency_id AND r1.pos = 1 " +
                           "LEFT JOIN (SELECT fr.*, can.candidate_name, ROW_NUMBER() OVER(PARTITION BY constituency_id ORDER BY total_votes DESC) as pos FROM FinalResults fr JOIN Candidates can ON fr.candidate_id = can.candidate_id) r2 ON c.constituency_id = r2.constituency_id AND r2.pos = 2";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("constituency_id"),
                    rs.getString("constituency_name"),
                    rs.getString("winner_name"),
                    rs.getString("runner_up_name") != null ? rs.getString("runner_up_name") : "N/A",
                    String.format("%,d", rs.getInt("margin")),
                    String.format("%.2f%%", rs.getDouble("vote_share"))
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Margin calculation error: " + ex.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{0, "No margin data available", "-", "-", "0", "0%"});
        }
    }
}
