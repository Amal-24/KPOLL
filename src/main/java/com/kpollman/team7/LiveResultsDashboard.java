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

public class LiveResultsDashboard extends JPanel {
    private JTable trendsTable;
    private DefaultTableModel tableModel;

    public LiveResultsDashboard() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Live Election Results 2026");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Results");
        refreshBtn.addActionListener(e -> refreshLiveData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Constituency", "Leading Candidate", "Party", "Votes", "Margin", "Status"};
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
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton tallyBtn = new ModernUI.ModernButton("Party Seat Tally");
        tallyBtn.addActionListener(e -> MainDashboard.showView(new PartySeatTallyScreen()));
        
        ModernUI.ModernButton trendsBtn = new ModernUI.ModernButton("Constituency Trends");
        trendsBtn.addActionListener(e -> MainDashboard.showView(new ConstituencyWiseTrendsScreen()));

        ModernUI.ModernButton marginBtn = new ModernUI.ModernButton("Margin Analysis");
        marginBtn.addActionListener(e -> MainDashboard.showView(new MarginAnalysisScreen()));

        ModernUI.ModernButton mapBtn = new ModernUI.ModernButton("Result Map");
        mapBtn.setBackground(ModernUI.ACCENT_COLOR);
        mapBtn.addActionListener(e -> MainDashboard.showView(new ResultMapVisualizationScreen()));

        ModernUI.ModernButton exportBtn = new ModernUI.ModernButton("Export Results");
        exportBtn.setBackground(new Color(34, 197, 94));
        exportBtn.addActionListener(e -> new ResultExportScreen().setVisible(true));

        actionPanel.add(tallyBtn);
        actionPanel.add(trendsBtn);
        actionPanel.add(marginBtn);
        actionPanel.add(mapBtn);
        actionPanel.add(exportBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshLiveData();
    }

    private void refreshLiveData() {
        tableModel.setRowCount(0);
        ResultDataProcessor processor = new KeralaLiveResultProcessor();
        boolean dataFound = false;
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Complex query to get leading candidate and their margin over runner-up
            String query = "WITH RankedResults AS (" +
                           "  SELECT c.constituency_name, can.candidate_name, can.party_name, fr.total_votes, fr.is_winner, " +
                           "  ROW_NUMBER() OVER(PARTITION BY fr.constituency_id ORDER BY fr.total_votes DESC) as pos " +
                           "  FROM FinalResults fr " +
                           "  JOIN Candidates can ON fr.candidate_id = can.candidate_id " +
                           "  JOIN Constituencies c ON fr.constituency_id = c.constituency_id" +
                           ") " +
                           "SELECT r1.constituency_name, r1.candidate_name, r1.party_name, r1.total_votes, r1.is_winner, " +
                           "COALESCE(r2.total_votes, 0) as runner_up_votes " +
                           "FROM RankedResults r1 " +
                           "LEFT JOIN RankedResults r2 ON r1.constituency_name = r2.constituency_name AND r2.pos = 2 " +
                           "WHERE r1.pos = 1";

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int leadingVotes = rs.getInt("total_votes");
                int runnerUpVotes = rs.getInt("runner_up_votes");
                int margin = leadingVotes - runnerUpVotes;
                
                String trend = processor.processTrend(leadingVotes, runnerUpVotes);
                
                tableModel.addRow(new Object[]{
                    rs.getString("constituency_name"),
                    rs.getString("candidate_name"),
                    rs.getString("party_name"),
                    String.format("%,d", leadingVotes),
                    String.format("%,d", margin),
                    rs.getBoolean("is_winner") ? "WINNER (" + trend + ")" : "LEADING (" + trend + ")"
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Live results fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{"No Results", "-", "-", "0", "0", "NO DATA"});
        }
    }
}
