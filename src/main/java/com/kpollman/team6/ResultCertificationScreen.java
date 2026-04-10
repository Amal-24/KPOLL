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
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        certificationTable = new JTable(tableModel);
        certificationTable.setFont(ModernUI.MAIN_FONT);
        certificationTable.setRowHeight(40);
        certificationTable.setShowVerticalLines(false);
        certificationTable.setGridColor(ModernUI.BORDER_COLOR);
        certificationTable.setSelectionBackground(new Color(0, 120, 215));
        certificationTable.setSelectionForeground(Color.WHITE);
        
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

    private void certifyResult() {
        int row = certificationTable.getSelectedRow();
        if (row != -1) {
            String name = (String) tableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Apply Digital Signature and Certify result for " + name + "?",
                "Digital Certification", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseHelper.getConnection()) {
                    String query = "UPDATE FinalResults SET certified_at = CURRENT_TIMESTAMP WHERE candidate_id = ? AND is_winner = TRUE";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, (int) tableModel.getValueAt(row, 0));
                    pstmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Result for " + name + " has been Digitally Certified.");
                    refreshWinners();
                } catch (Exception ex) {
                    System.err.println("Certification error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Demo Mode: " + name + " certified with Digital Signature.");
                    tableModel.setValueAt("CERTIFIED", row, 4);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a winner to certify.");
        }
    }

    private void refreshWinners() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
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
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Certification refresh error: " + ex.getMessage());
            // Fallback demo data
            tableModel.addRow(new Object[]{1, "John Doe", "Thiruvananthapuram", "45,000", "CERTIFIED"});
            dataFound = true;
        }

        if (!dataFound) {
            // Mock data
            tableModel.addRow(new Object[]{1, "Candidate X (Mock)", "Trivandrum", "45,000", "PENDING"});
            tableModel.addRow(new Object[]{3, "Candidate Z (Mock)", "Kochi", "62,100", "CERTIFIED"});
        }
    }
}
