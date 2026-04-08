package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
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

        String[] columns = {"Result ID", "Constituency", "Round", "Candidate", "Votes", "Verification Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(ModernUI.MAIN_FONT);
        resultsTable.setRowHeight(40);
        hideIdColumn();

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);

        ModernUI.ModernButton verifyBtn = new ModernUI.ModernButton("Verify Selected");
        verifyBtn.setBackground(new Color(34, 197, 94));
        verifyBtn.addActionListener(e -> updateVerificationStatus(true));

        ModernUI.ModernButton rejectBtn = new ModernUI.ModernButton("Mark for Recount");
        rejectBtn.setBackground(new Color(239, 68, 68));
        rejectBtn.addActionListener(e -> updateVerificationStatus(false));

        actionPanel.add(verifyBtn);
        actionPanel.add(rejectBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshVerificationList();
    }

    private void hideIdColumn() {
        TableColumn idColumn = resultsTable.getColumnModel().getColumn(0);
        idColumn.setMinWidth(0);
        idColumn.setMaxWidth(0);
        idColumn.setPreferredWidth(0);
    }

    private void updateVerificationStatus(boolean verified) {
        int row = resultsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        int resultId = (int) tableModel.getValueAt(row, 0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "UPDATE RoundResults SET is_verified = ? WHERE result_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setBoolean(1, verified);
            pstmt.setInt(2, resultId);
            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, verified
                    ? "Round result verified and finalized."
                    : "Round result marked unverified for recount.");
                refreshVerificationList();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update verification: " + ex.getMessage());
        }
    }

    private void refreshVerificationList() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT rr.result_id, c.constituency_name, rr.round_no, can.candidate_name, rr.votes_counted, rr.is_verified " +
                           "FROM RoundResults rr " +
                           "JOIN CountingTables ct ON rr.table_id = ct.table_id " +
                           "JOIN Constituencies c ON ct.constituency_id = c.constituency_id " +
                           "JOIN Candidates can ON rr.candidate_id = can.candidate_id " +
                           "ORDER BY c.constituency_name, rr.round_no, can.candidate_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("result_id"),
                    rs.getString("constituency_name"),
                    rs.getInt("round_no"),
                    rs.getString("candidate_name"),
                    rs.getInt("votes_counted"),
                    rs.getBoolean("is_verified") ? "Verified" : "Pending"
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Verification list fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{0, "No Data", 0, "-", 0, "N/A"});
        }
    }
}
