package com.kpollman.team5;

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
 * Result Verification Screen to verify counts before finalizing.
 */
public class ResultVerificationScreen extends JPanel {
    private JTable resultsTable;
    private DefaultTableModel tableModel;

    public ResultVerificationScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Round-wise Result Verification");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Constituency", "Round", "Candidate", "Votes", "Verification Status"};
        tableModel = new DefaultTableModel(columns, 0);
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(ModernUI.MAIN_FONT);
        resultsTable.setRowHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton verifyBtn = new ModernUI.ModernButton("Verify Selected Round");
        verifyBtn.setBackground(new Color(34, 197, 94));
        verifyBtn.addActionListener(e -> verifySelected());
        
        ModernUI.ModernButton rejectBtn = new ModernUI.ModernButton("Reject Round Result");
        rejectBtn.setBackground(new Color(239, 68, 68));
        rejectBtn.addActionListener(e -> rejectSelected());

        actionPanel.add(verifyBtn);
        actionPanel.add(rejectBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshVerificationList();
    }

    private void verifySelected() {
        int row = resultsTable.getSelectedRow();
        if (row != -1) {
            JOptionPane.showMessageDialog(this, "Round Result Verified and Finalized!");
            tableModel.setValueAt("Verified", row, 4);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to verify.");
        }
    }

    private void rejectSelected() {
        int row = resultsTable.getSelectedRow();
        if (row != -1) {
            JOptionPane.showMessageDialog(this, "Round Result Rejected. Needs Recounting.");
            tableModel.setValueAt("Rejected", row, 4);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to reject.");
        }
    }

    private void refreshVerificationList() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_name, rr.round_number, can.candidate_name, rr.votes_counted " +
                           "FROM RoundResults rr " +
                           "JOIN Constituencies c ON rr.constituency_id = c.constituency_id " +
                           "JOIN Candidates can ON rr.candidate_id = can.candidate_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("constituency_name"),
                    rs.getInt("round_number"),
                    rs.getString("candidate_name"),
                    rs.getInt("votes_counted"),
                    "Pending"
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Verification list fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{"No Data", 0, "-", 0, "N/A"});
        }
    }
}
