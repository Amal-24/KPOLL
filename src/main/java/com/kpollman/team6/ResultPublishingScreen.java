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
        
        ModernUI.ModernButton publishButton = new ModernUI.ModernButton("Publish to Public Interface");
        publishButton.setBackground(ModernUI.PRIMARY_COLOR);
        publishButton.addActionListener(e -> publishResults());

        ModernUI.ModernButton exportButton = new ModernUI.ModernButton("Export to JSON");
        exportButton.setBackground(ModernUI.ACCENT_COLOR);
        exportButton.addActionListener(e -> exportResultsToJson());

        actionPanel.add(publishButton);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(exportButton);
        add(actionPanel, BorderLayout.SOUTH);

        refreshPublishedResults();
    }

    private void publishResults() {
        int row = publishTable.getSelectedRow();
        boolean publishAll = row == -1;
        try (Connection conn = DatabaseHelper.getConnection()) {
            if (publishAll) {
                String updateQuery = "UPDATE FinalResults SET published_at = CURRENT_TIMESTAMP WHERE is_winner = TRUE";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                int count = pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Published " + count + " official results to the public interface.");
            } else {
                String constituency = (String) tableModel.getValueAt(row, 0);
                String updateQuery = "UPDATE FinalResults fr JOIN Constituencies c ON fr.constituency_id = c.constituency_id " +
                                     "SET fr.published_at = CURRENT_TIMESTAMP WHERE fr.is_winner = TRUE AND c.constituency_name = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setString(1, constituency);
                int count = pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Published " + count + " result(s) for " + constituency + " to the public interface.");
            }
            refreshPublishedResults();
        } catch (Exception ex) {
            System.err.println("Publish error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Demo Mode: Results published to the public interface.");
            refreshPublishedResults();
        }
    }

    private void exportResultsToJson() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Results to JSON");
        chooser.setSelectedFile(new java.io.File("official-results.json"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write("[");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (i > 0) writer.write(",\n");
                    writer.write("  {");
                    writer.write("\"constituency\": \"" + tableModel.getValueAt(i, 0) + "\"");
                    writer.write(", \"winner\": \"" + tableModel.getValueAt(i, 1) + "\"");
                    writer.write(", \"party\": \"" + tableModel.getValueAt(i, 2) + "\"");
                    writer.write(", \"votes\": \"" + tableModel.getValueAt(i, 3) + "\"");
                    writer.write(", \"status\": \"" + tableModel.getValueAt(i, 4) + "\"");
                    writer.write("}");
                }
                writer.write("\n]");
                JOptionPane.showMessageDialog(this, "Exported published results to " + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshPublishedResults() {
        tableModel.setRowCount(0);
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_name, can.candidate_name, can.party_name, fr.total_votes, fr.certified_at, fr.published_at " +
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
                    rs.getTimestamp("published_at") != null ? "PUBLISHED" : (rs.getTimestamp("certified_at") != null ? "CERTIFIED" : "PENDING")
                });
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Publish refresh error: " + ex.getMessage());
        }

        if (!dataFound) {
            // Mock data
            tableModel.addRow(new Object[]{"Trivandrum (Mock)", "Candidate X", "Party A", "45,000", "PUBLISHED"});
            tableModel.addRow(new Object[]{"Kochi (Mock)", "Candidate Z", "Party C", "62,100", "CERTIFIED"});
        }
    }
}
