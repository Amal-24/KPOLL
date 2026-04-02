package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Result Verification Screen for verifying counts before finalizing.
 */
public class ResultVerificationScreen extends JFrame {
    private JTable verificationTable;
    private DefaultTableModel tableModel;

    public ResultVerificationScreen() {
        setTitle("K-PollMan 2026 - Result Verification Screen");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(230, 240, 255));
        JLabel titleLabel = new JLabel("Result Verification Dashboard", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"Result ID", "Round No", "Table ID", "Candidate", "Votes", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        verificationTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(verificationTable);
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel();
        ModernUI.ModernButton refreshButton = new ModernUI.ModernButton("Refresh Results");
        ModernUI.ModernButton verifyButton = new ModernUI.ModernButton("Verify Selected Result");

        actionPanel.add(refreshButton);
        actionPanel.add(verifyButton);
        add(actionPanel, BorderLayout.SOUTH);

        // Button Actions
        refreshButton.addActionListener(e -> refreshResults());
        verifyButton.addActionListener(e -> verifyResult());

        refreshResults();
    }

    private void refreshResults() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT rr.*, c.candidate_name FROM RoundResults rr LEFT JOIN Candidates c ON rr.candidate_id = c.candidate_id WHERE rr.is_verified = FALSE";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("result_id"),
                    rs.getInt("round_no"),
                    rs.getInt("table_id"),
                    rs.getString("candidate_name"),
                    rs.getInt("votes_counted"),
                    "PENDING"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching results: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void verifyResult() {
        int selectedRow = verificationTable.getSelectedRow();
        if (selectedRow >= 0) {
            int resultId = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseHelper.getConnection()) {
                String updateQuery = "UPDATE RoundResults SET is_verified = TRUE WHERE result_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setInt(1, resultId);
                pstmt.executeUpdate();
                refreshResults();
                JOptionPane.showMessageDialog(this, "Result verified successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a result to verify", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
