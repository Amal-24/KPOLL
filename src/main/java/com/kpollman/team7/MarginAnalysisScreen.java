package com.kpollman.team7;

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
 * Margin Analysis Screen for winning margin distribution analysis.
 */
public class MarginAnalysisScreen extends JPanel {
    private JTable marginTable;
    private DefaultTableModel tableModel;

    public MarginAnalysisScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Winning Margin Distribution Analysis");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new LiveResultsDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        String[] columns = {"Margin Range", "Count of Constituencies", "Percentage (%)"};
        tableModel = new DefaultTableModel(columns, 0);
        marginTable = new JTable(tableModel);
        marginTable.setFont(ModernUI.MAIN_FONT);
        marginTable.setRowHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(marginTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1));
        
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        tableContainer.add(scrollPane);
        add(tableContainer, BorderLayout.CENTER);

        // Refresh Button
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Analysis");
        refreshBtn.addActionListener(e -> analyzeMargins());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(refreshBtn);
        add(footer, BorderLayout.SOUTH);

        analyzeMargins();
    }

    private void analyzeMargins() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "WITH Margins AS (" +
                           "  SELECT fr.constituency_id, " +
                           "  (MAX(fr.total_votes) - MIN(fr.total_votes)) as margin " +
                           "  FROM FinalResults fr " +
                           "  JOIN ( " +
                           "    SELECT constituency_id, total_votes, " +
                           "    ROW_NUMBER() OVER(PARTITION BY constituency_id ORDER BY total_votes DESC) as pos " +
                           "    FROM FinalResults " +
                           "  ) ranked ON fr.constituency_id = ranked.constituency_id " +
                           "  WHERE ranked.pos <= 2 " +
                           "  GROUP BY fr.constituency_id " +
                           "), " +
                           "Categories AS (" +
                           "  SELECT CASE " +
                           "    WHEN margin < 1000 THEN 'Under 1,000' " +
                           "    WHEN margin < 5000 THEN '1,000 - 5,000' " +
                           "    WHEN margin < 10000 THEN '5,000 - 10,000' " +
                           "    WHEN margin < 20000 THEN '10,000 - 20,000' " +
                           "    ELSE 'Over 20,000' " +
                           "  END as range_name, " +
                           "  COUNT(*) as constituency_count " +
                           "  FROM Margins " +
                           "  GROUP BY range_name " +
                           ") " +
                           "SELECT range_name, constituency_count, " +
                           "(constituency_count * 100.0 / (SELECT COUNT(*) FROM Margins)) as percentage " +
                           "FROM Categories";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("range_name"),
                    rs.getInt("constituency_count"),
                    String.format("%.2f%%", rs.getDouble("percentage"))
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Margin analysis error: " + ex.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{"No Data", 0, "0%"});
        }
    }
}
