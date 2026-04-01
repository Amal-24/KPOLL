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

public class ResultCertificationScreen extends JPanel {
    private JTable certificationTable;
    private DefaultTableModel tableModel;

    public ResultCertificationScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Official Result Certification");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Winners");
        backBtn.addActionListener(e -> MainDashboard.showView(new WinnerDeclarationScreen()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Constituency", "Total Votes", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        certificationTable = new JTable(tableModel);
        certificationTable.setFont(ModernUI.MAIN_FONT);
        certificationTable.setRowHeight(40);
        certificationTable.setShowVerticalLines(false);
        certificationTable.setGridColor(ModernUI.BORDER_COLOR);
        
        JTableHeader header = certificationTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(certificationTable);
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
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Winners");
        refreshBtn.addActionListener(e -> refreshWinners());
        
        ModernUI.ModernButton certifyBtn = new ModernUI.ModernButton("Digitally Certify Result");
        certifyBtn.setBackground(ModernUI.ACCENT_COLOR);
        certifyBtn.addActionListener(e -> certifyResult());

        ModernUI.ModernButton publishBtn = new ModernUI.ModernButton("Go to Publishing");
        publishBtn.setBackground(ModernUI.PRIMARY_COLOR);
        publishBtn.addActionListener(e -> MainDashboard.showView(new ResultPublishingScreen()));

        actionPanel.add(refreshBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(certifyBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(publishBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshWinners();
    }

    private void refreshWinners() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT fr.candidate_id, can.candidate_name, c.constituency_name, fr.total_votes, fr.certified_at " +
                           "FROM FinalResults fr " +
                           "JOIN Candidates can ON fr.candidate_id = can.candidate_id " +
                           "JOIN Constituencies c ON fr.constituency_id = c.constituency_id " +
                           "WHERE fr.is_winner = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("candidate_id"),
                    rs.getString("candidate_name"),
                    rs.getString("constituency_name"),
                    String.format("%,d", rs.getInt("total_votes")),
                    rs.getTimestamp("certified_at") != null ? "CERTIFIED" : "PENDING"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void certifyResult() {
        int selectedRow = certificationTable.getSelectedRow();
        if (selectedRow >= 0) {
            int candidateId = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseHelper.getConnection()) {
                String updateQuery = "UPDATE FinalResults SET certified_at = CURRENT_TIMESTAMP WHERE candidate_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setInt(1, candidateId);
                pstmt.executeUpdate();
                refreshWinners();
                JOptionPane.showMessageDialog(this, "Result digitally certified successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a result to certify");
        }
    }
}
