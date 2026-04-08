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

/**
 * Constituency-wise Trends Screen displaying leading/trailing status.
 */
public class ConstituencyWiseTrendsScreen extends JPanel {
    private JTable trendsTable;
    private DefaultTableModel tableModel;

    public ConstituencyWiseTrendsScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Constituency-wise Election Trends");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new LiveResultsDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Constituency", "Leading Candidate", "Leading Party", "Trailing Candidate", "Trailing Party", "Margin", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        trendsTable = new JTable(tableModel);
        trendsTable.setFont(ModernUI.MAIN_FONT);
        trendsTable.setRowHeight(40);
        trendsTable.setShowVerticalLines(false);
        trendsTable.setGridColor(ModernUI.BORDER_COLOR);
        trendsTable.setSelectionBackground(new Color(0, 120, 215));
        trendsTable.setSelectionForeground(Color.WHITE);
        
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
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Trends");
        refreshBtn.addActionListener(e -> refreshTrends());
        
        actionPanel.add(refreshBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshTrends();
    }

    private void refreshTrends() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Logic to fetch leading and runner-up for each constituency
            String query = "WITH RankedResults AS (" +
                           "  SELECT c.constituency_name, can.candidate_name, can.party_name, fr.total_votes, " +
                           "  ROW_NUMBER() OVER(PARTITION BY fr.constituency_id ORDER BY fr.total_votes DESC) as pos " +
                           "  FROM FinalResults fr " +
                           "  JOIN Candidates can ON fr.candidate_id = can.candidate_id " +
                           "  JOIN Constituencies c ON fr.constituency_id = c.constituency_id" +
                           ") " +
                           "SELECT r1.constituency_name, r1.candidate_name as leading_name, r1.party_name as leading_party, " +
                           "r2.candidate_name as trailing_name, r2.party_name as trailing_party, " +
                           "(r1.total_votes - COALESCE(r2.total_votes, 0)) as margin " +
                           "FROM RankedResults r1 " +
                           "LEFT JOIN RankedResults r2 ON r1.constituency_name = r2.constituency_name AND r2.pos = 2 " +
                           "WHERE r1.pos = 1";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int margin = rs.getInt("margin");
                String status = margin > 10000 ? "SAFE LEAD" : (margin > 0 ? "CLOSE FIGHT" : "TIGHT CONTEST");
                tableModel.addRow(new Object[]{
                    rs.getString("constituency_name"),
                    rs.getString("leading_name"),
                    rs.getString("leading_party"),
                    rs.getString("trailing_name") != null ? rs.getString("trailing_name") : "N/A",
                    rs.getString("trailing_party") != null ? rs.getString("trailing_party") : "N/A",
                    String.format("%,d", margin),
                    status
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Trends data fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{"No Data", "-", "-", "-", "-", "0", "N/A"});
        }
    }
}
