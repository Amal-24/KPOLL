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

public class ResultPublishingScreen extends JPanel {
    private JTable publishTable;
    private DefaultTableModel tableModel;

    public ResultPublishingScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Official Result Publishing Portal");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Certification");
        backBtn.addActionListener(e -> MainDashboard.showView(new ResultCertificationScreen()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Constituency", "Winner", "Party", "Votes", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        publishTable = new JTable(tableModel);
        publishTable.setFont(ModernUI.MAIN_FONT);
        publishTable.setRowHeight(40);
        publishTable.setShowVerticalLines(false);
        publishTable.setGridColor(ModernUI.BORDER_COLOR);
        
        JTableHeader header = publishTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(publishTable);
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
        
        ModernUI.ModernButton publishButton = new ModernUI.ModernButton("Publish to Website/Media");
        publishButton.setBackground(ModernUI.PRIMARY_COLOR);
        publishButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Successfully published official results to public interface and media portals!");
        });

        ModernUI.ModernButton exportButton = new ModernUI.ModernButton("Export to PDF/JSON");
        exportButton.setBackground(ModernUI.ACCENT_COLOR);

        actionPanel.add(publishButton);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(exportButton);
        add(actionPanel, BorderLayout.SOUTH);

        refreshPublishedResults();
    }

    private void refreshPublishedResults() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_name, can.candidate_name, can.party_name, fr.total_votes, fr.certified_at " +
                           "FROM FinalResults fr " +
                           "JOIN Candidates can ON fr.candidate_id = can.candidate_id " +
                           "JOIN Constituencies c ON fr.constituency_id = c.constituency_id " +
                           "WHERE fr.is_winner = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("constituency_name"),
                    rs.getString("candidate_name"),
                    rs.getString("party_name"),
                    String.format("%,d", rs.getInt("total_votes")),
                    rs.getTimestamp("certified_at") != null ? "CERTIFIED" : "PENDING"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
