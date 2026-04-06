package com.kpollman.team6;

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
 * Constituency Result Details Screen showing candidate-wise votes for a constituency.
 */
public class ResultDetailsScreen extends JPanel {
    private JTable detailsTable;
    private DefaultTableModel tableModel;
    private int constituencyId;

    public ResultDetailsScreen(int constituencyId) {
        this.constituencyId = constituencyId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Constituency Detailed Results");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new ResultAggregationScreen()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Candidate Name", "Party", "Votes Counted", "Percentage (%)"};
        tableModel = new DefaultTableModel(columns, 0);
        detailsTable = new JTable(tableModel);
        detailsTable.setFont(ModernUI.MAIN_FONT);
        detailsTable.setRowHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        refreshDetails();
    }

    private void refreshDetails() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String totalVotesQuery = "SELECT SUM(votes_counted) as total FROM RoundResults WHERE constituency_id = ?";
            PreparedStatement totalPstmt = conn.prepareStatement(totalVotesQuery);
            totalPstmt.setInt(1, constituencyId);
            ResultSet totalRs = totalPstmt.executeQuery();
            int totalVotes = totalRs.next() ? totalRs.getInt("total") : 0;

            String query = "SELECT c.candidate_name, c.party_name, SUM(rr.votes_counted) as votes " +
                           "FROM Candidates c " +
                           "JOIN RoundResults rr ON c.candidate_id = rr.candidate_id " +
                           "WHERE rr.constituency_id = ? " +
                           "GROUP BY c.candidate_id, c.candidate_name, c.party_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, constituencyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int votes = rs.getInt("votes");
                double percentage = totalVotes > 0 ? (votes * 100.0 / totalVotes) : 0.0;
                tableModel.addRow(new Object[]{
                    rs.getString("candidate_name"),
                    rs.getString("party_name"),
                    String.format("%,d", votes),
                    String.format("%.2f%%", percentage)
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Details fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            // Mock data
            tableModel.addRow(new Object[]{"Candidate A (Mock)", "Party LDF", "45,200", "48.5%"});
            tableModel.addRow(new Object[]{"Candidate B (Mock)", "Party UDF", "41,800", "44.8%"});
            tableModel.addRow(new Object[]{"Candidate C (Mock)", "Party NDA", "6,150", "6.7%"});
        }
    }
}
