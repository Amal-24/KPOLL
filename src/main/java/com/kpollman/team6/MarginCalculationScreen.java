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
        tableModel = new DefaultTableModel(columns, 0);
        marginTable = new JTable(tableModel);
        marginTable.setFont(ModernUI.MAIN_FONT);
        marginTable.setRowHeight(40);
        marginTable.setShowVerticalLines(false);
        marginTable.setGridColor(ModernUI.BORDER_COLOR);
        marginTable.setSelectionBackground(new Color(241, 245, 249));
        
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
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_id, c.constituency_name, " +
                           "(SELECT can1.candidate_name FROM FinalResults fr1 JOIN Candidates can1 ON fr1.candidate_id = can1.candidate_id WHERE fr1.constituency_id = c.constituency_id ORDER BY fr1.total_votes DESC LIMIT 1) as winner_name, " +
                           "(SELECT can2.candidate_name FROM FinalResults fr2 JOIN Candidates can2 ON fr2.candidate_id = can2.candidate_id WHERE fr2.constituency_id = c.constituency_id ORDER BY fr2.total_votes DESC LIMIT 1 OFFSET 1) as runner_up_name, " +
                           "(SELECT MAX(total_votes) FROM FinalResults WHERE constituency_id = c.constituency_id) - (SELECT total_votes FROM FinalResults WHERE constituency_id = c.constituency_id ORDER BY total_votes DESC LIMIT 1 OFFSET 1) as margin, " +
                           "(SELECT MAX(total_votes) FROM FinalResults WHERE constituency_id = c.constituency_id) * 100.0 / (SELECT SUM(total_votes) FROM FinalResults WHERE constituency_id = c.constituency_id) as vote_share " +
                           "FROM Constituencies c";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("constituency_id"),
                    rs.getString("constituency_name"),
                    rs.getString("winner_name"),
                    rs.getString("runner_up_name"),
                    String.format("%,d", rs.getInt("margin")),
                    String.format("%.2f%%", rs.getDouble("vote_share"))
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error calculating margins: " + ex.getMessage());
        }
    }
}
