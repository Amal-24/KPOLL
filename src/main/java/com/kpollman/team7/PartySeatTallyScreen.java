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

public class PartySeatTallyScreen extends JPanel {
    private JTable tallyTable;
    private DefaultTableModel tableModel;

    public PartySeatTallyScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Party-wise Seat Tally");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new LiveResultsDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Party Name", "Won", "Leading", "Total"};
        tableModel = new DefaultTableModel(columns, 0);
        tallyTable = new JTable(tableModel);
        tallyTable.setFont(ModernUI.MAIN_FONT);
        tallyTable.setRowHeight(40);
        tallyTable.setShowVerticalLines(false);
        tallyTable.setGridColor(ModernUI.BORDER_COLOR);
        tallyTable.setSelectionBackground(new Color(241, 245, 249));
        
        JTableHeader header = tallyTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(tallyTable);
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
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Tally");
        refreshBtn.addActionListener(e -> refreshTally());
        
        actionPanel.add(refreshBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshTally();
    }

    private void refreshTally() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String wonQuery = "SELECT can.party_name, COUNT(*) as won_count FROM FinalResults fr JOIN Candidates can ON fr.candidate_id = can.candidate_id WHERE fr.is_winner = TRUE GROUP BY can.party_name";
            PreparedStatement wonPstmt = conn.prepareStatement(wonQuery);
            ResultSet wonRs = wonPstmt.executeQuery();
            while (wonRs.next()) {
                tableModel.addRow(new Object[]{
                    wonRs.getString("party_name"),
                    wonRs.getInt("won_count"),
                    0,
                    wonRs.getInt("won_count")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching tally: " + ex.getMessage());
        }
    }
}
