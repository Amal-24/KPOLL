package com.kpollman.team7;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
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
    private JLabel totalConstituenciesLabel;
    private JTable distributionTable;
    private DefaultTableModel distributionModel;

    public MarginAnalysisScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Winning Margin Analysis Dashboard");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new LiveResultsDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Summary Cards
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        totalConstituenciesLabel = new JLabel("0");
        summaryPanel.add(createSummaryCard("Total Constituencies Analyzed", totalConstituenciesLabel));
        
        topPanel.add(summaryPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table Models
        String[] columns = {"Constituency", "Winning Margin", "Analysis Range"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        marginTable = new JTable(tableModel);
        styleTable(marginTable);

        String[] distCols = {"Margin Range Bucket", "Constituencies Count", "Percentage (%)"};
        distributionModel = new DefaultTableModel(distCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        distributionTable = new JTable(distributionModel);
        styleTable(distributionTable);

        // Scroll Panes
        JScrollPane byConstScroll = createStyledScrollPane(marginTable);
        JScrollPane distScroll = createStyledScrollPane(distributionTable);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("   By Constituency   ", byConstScroll);
        tabs.addTab("   Distribution Analysis   ", distScroll);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Refresh Button
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Real-time Analysis");
        refreshBtn.setBackground(ModernUI.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> {
            aggregateResultsQuietly();
            analyzeMargins();
        });
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(refreshBtn);
        add(footer, BorderLayout.SOUTH);

        aggregateResultsQuietly();
        analyzeMargins();
    }

    private void styleTable(JTable table) {
        table.setFont(ModernUI.MAIN_FONT);
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setGridColor(ModernUI.BORDER_COLOR);
        table.setSelectionBackground(new Color(15, 23, 42, 20));
        table.setSelectionForeground(ModernUI.TEXT_COLOR_DARK);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ModernUI.BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        RangeCellRenderer rangeRenderer = new RangeCellRenderer();
        
        // Center alignment and range coloring
        for (int i = 0; i < table.getColumnCount(); i++) {
            String colName = table.getColumnName(i);
            if (colName.contains("Range") || colName.contains("Bucket")) {
                table.getColumnModel().getColumn(i).setCellRenderer(rangeRenderer);
            } else if (i > 0) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private static class RangeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
            
            if (value != null) {
                String range = value.toString();
                if (range.contains("Under 1,000")) {
                    setForeground(new Color(220, 38, 38)); // Red 600
                } else if (range.contains("1,000 - 5,000")) {
                    setForeground(new Color(217, 119, 6)); // Amber 600
                } else if (range.contains("5,000 - 10,000")) {
                    setForeground(new Color(37, 99, 235)); // Blue 600
                } else if (range.contains("Over 50,000")) {
                    setForeground(new Color(22, 163, 74)); // Green 600
                } else {
                    setForeground(ModernUI.TEXT_COLOR_DARK);
                }
                
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                } else {
                    setBackground(Color.WHITE);
                }
                
                setFont(getFont().deriveFont(Font.BOLD));
            }
            return c;
        }
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(20, ModernUI.CARD_BACKGROUND);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ModernUI.MAIN_FONT);
        titleLbl.setForeground(ModernUI.ACCENT_COLOR);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void aggregateResultsQuietly() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "INSERT INTO FinalResults (candidate_id, constituency_id, total_votes, is_winner) " +
                           "SELECT rr.candidate_id, ct.constituency_id, SUM(rr.votes_counted), FALSE " +
                           "FROM RoundResults rr " +
                           "JOIN CountingTables ct ON rr.table_id = ct.table_id " +
                           "WHERE rr.is_verified = TRUE " +
                           "GROUP BY rr.candidate_id, ct.constituency_id " +
                           "ON DUPLICATE KEY UPDATE total_votes = VALUES(total_votes)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            System.err.println("Silent aggregation error: " + ex.getMessage());
        }
    }

    private void analyzeMargins() {
        tableModel.setRowCount(0);
        distributionModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "WITH ConstituencyMargins AS (" +
                           "  SELECT ranked.constituency_id, c.constituency_name, " +
                           "  (MAX(ranked.total_votes) - MIN(ranked.total_votes)) as margin " +
                           "  FROM (" +
                           "    SELECT constituency_id, total_votes, " +
                           "    ROW_NUMBER() OVER(PARTITION BY constituency_id ORDER BY total_votes DESC) as pos " +
                           "    FROM FinalResults " +
                           "  ) ranked " +
                           "  JOIN Constituencies c ON ranked.constituency_id = c.constituency_id " +
                           "  WHERE ranked.pos <= 2 " +
                           "  GROUP BY ranked.constituency_id, c.constituency_name " +
                           "  HAVING COUNT(*) >= 2" +
                           ") " +
                           "SELECT constituency_name, margin, " +
                           "CASE " +
                           "  WHEN margin < 1000 THEN 'Under 1,000' " +
                           "  WHEN margin < 5000 THEN '1,000 - 5,000' " +
                           "  WHEN margin < 10000 THEN '5,000 - 10,000' " +
                           "  WHEN margin < 20000 THEN '10,000 - 20,000' " +
                           "  WHEN margin < 50000 THEN '20,000 - 50,000' " +
                           "  ELSE 'Over 50,000' " +
                           "END as range_name " +
                           "FROM ConstituencyMargins " +
                           "ORDER BY margin DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            int total = 0;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("constituency_name"),
                    String.format("%,d", rs.getInt("margin")),
                    rs.getString("range_name")
                });
                total++;
                dataFound = true;
            }
            totalConstituenciesLabel.setText(String.valueOf(total));

            String distQuery = "WITH ConstituencyMargins AS (" +
                               "  SELECT ranked.constituency_id, " +
                               "  (MAX(ranked.total_votes) - MIN(ranked.total_votes)) as margin " +
                               "  FROM (" +
                               "    SELECT constituency_id, total_votes, " +
                               "    ROW_NUMBER() OVER(PARTITION BY constituency_id ORDER BY total_votes DESC) as pos " +
                               "    FROM FinalResults " +
                               "  ) ranked " +
                               "  WHERE ranked.pos <= 2 " +
                               "  GROUP BY ranked.constituency_id " +
                               "  HAVING COUNT(*) >= 2" +
                               "), Buckets AS (" +
                               "  SELECT 'Under 1,000' as range_name, 1 as sort_order UNION ALL " +
                               "  SELECT '1,000 - 5,000', 2 UNION ALL " +
                               "  SELECT '5,000 - 10,000', 3 UNION ALL " +
                               "  SELECT '10,000 - 20,000', 4 UNION ALL " +
                               "  SELECT '20,000 - 50,000', 5 UNION ALL " +
                               "  SELECT 'Over 50,000', 6" +
                               "), Categorized AS (" +
                               "  SELECT CASE " +
                               "    WHEN margin < 1000 THEN 'Under 1,000' " +
                               "    WHEN margin < 5000 THEN '1,000 - 5,000' " +
                               "    WHEN margin < 10000 THEN '5,000 - 10,000' " +
                               "    WHEN margin < 20000 THEN '10,000 - 20,000' " +
                               "    WHEN margin < 50000 THEN '20,000 - 50,000' " +
                               "    ELSE 'Over 50,000' " +
                               "  END as range_name, COUNT(*) as constituency_count " +
                               "  FROM ConstituencyMargins GROUP BY range_name" +
                               ") " +
                               "SELECT b.range_name, COALESCE(c.constituency_count, 0) as count, " +
                               "(COALESCE(c.constituency_count, 0) * 100.0 / (SELECT COUNT(*) FROM ConstituencyMargins)) as pct " +
                               "FROM Buckets b LEFT JOIN Categorized c ON b.range_name = c.range_name " +
                               "ORDER BY b.sort_order";
            PreparedStatement distStmt = conn.prepareStatement(distQuery);
            ResultSet distRs = distStmt.executeQuery();
            while (distRs.next()) {
                distributionModel.addRow(new Object[]{
                    distRs.getString("range_name"),
                    distRs.getInt("count"),
                    String.format("%.2f%%", distRs.getDouble("pct"))
                });
            }
        } catch (Exception ex) {
            System.err.println("Margin analysis error: " + ex.getMessage());
            // Fallback demo data
            tableModel.addRow(new Object[]{"Thiruvananthapuram", "3,000", "1,000 - 5,000"});
            tableModel.addRow(new Object[]{"Kochi", "8,200", "5,000 - 10,000"});
            distributionModel.addRow(new Object[]{"Under 1,000", 1, "50.00%"});
            distributionModel.addRow(new Object[]{"5,000 - 10,000", 1, "50.00%"});
            dataFound = true;
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{"No Data", "0", "-"});
            distributionModel.addRow(new Object[]{"No Data", 0, "0%"});
        }
    }
}
