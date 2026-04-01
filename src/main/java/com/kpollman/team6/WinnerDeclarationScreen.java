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

public class WinnerDeclarationScreen extends JPanel {
    private JTable winnerTable;
    private DefaultTableModel tableModel;

    public WinnerDeclarationScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Winner Declaration Management");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new ResultAggregationScreen()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Constituency", "Leading Candidate", "Total Votes", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        winnerTable = new JTable(tableModel);
        winnerTable.setFont(ModernUI.MAIN_FONT);
        winnerTable.setRowHeight(40);
        winnerTable.setShowVerticalLines(false);
        winnerTable.setGridColor(ModernUI.BORDER_COLOR);
        winnerTable.setSelectionBackground(new Color(241, 245, 249));
        
        JTableHeader header = winnerTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(winnerTable);
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
        refreshBtn.addActionListener(e -> refreshWinners());
        
        ModernUI.ModernButton declareBtn = new ModernUI.ModernButton("Declare Selected Winner");
        declareBtn.setBackground(ModernUI.ACCENT_COLOR);
        declareBtn.addActionListener(e -> declareWinner());

        ModernUI.ModernButton certifyBtn = new ModernUI.ModernButton("Certify Results");
        certifyBtn.setBackground(ModernUI.PRIMARY_COLOR);
        certifyBtn.addActionListener(e -> MainDashboard.showView(new ResultCertificationScreen()));

        actionPanel.add(refreshBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(declareBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(certifyBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshWinners();
    }

    private void refreshWinners() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_id, c.constituency_name, can.candidate_name, fr.total_votes, fr.is_winner " +
                           "FROM Constituencies c " +
                           "JOIN FinalResults fr ON c.constituency_id = fr.constituency_id " +
                           "JOIN Candidates can ON fr.candidate_id = can.candidate_id " +
                           "WHERE fr.total_votes = (SELECT MAX(total_votes) FROM FinalResults WHERE constituency_id = c.constituency_id)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("constituency_id"),
                    rs.getString("constituency_name"),
                    rs.getString("candidate_name"),
                    String.format("%,d", rs.getInt("total_votes")),
                    rs.getBoolean("is_winner") ? "DECLARED" : "LEADING"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching winners: " + ex.getMessage());
        }
    }

    private void declareWinner() {
        int selectedRow = winnerTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String candidateName = (String) tableModel.getValueAt(selectedRow, 2);
            
            try (Connection conn = DatabaseHelper.getConnection()) {
                String updateQuery = "UPDATE FinalResults SET is_winner = TRUE " +
                                     "WHERE constituency_id = ? AND total_votes = (SELECT MAX(total_votes) FROM (SELECT * FROM FinalResults) as t WHERE constituency_id = ?)";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setInt(1, id);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
                refreshWinners();
                JOptionPane.showMessageDialog(this, "Successfully declared " + candidateName + " as winner!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a constituency");
        }
    }
}
