package com.kpollman.team7;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PartySeatTallyScreen extends JPanel {
    private JTable tallyTable;
    private DefaultTableModel tableModel;

    private JPanel chartPanel;

    public PartySeatTallyScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Live Party-wise Seat Tally");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new LiveResultsDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center split: Table and Chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Table
        String[] columns = {"Party Name", "Won", "Leading", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tallyTable = new JTable(tableModel);
        tallyTable.setFont(ModernUI.MAIN_FONT);
        tallyTable.setRowHeight(40);
        tallyTable.setShowVerticalLines(false);
        tallyTable.setGridColor(ModernUI.BORDER_COLOR);
        tallyTable.setSelectionBackground(new Color(0, 120, 215));
        tallyTable.setSelectionForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(tallyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1));
        splitPane.setTopComponent(scrollPane);

        // Chart Panel (Visual Representation)
        chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR), "Seat Distribution Visualization"));
        
        JScrollPane chartScroll = new JScrollPane(chartPanel);
        chartScroll.setBorder(null);
        splitPane.setBottomComponent(chartScroll);

        add(splitPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Tally Data");
        refreshBtn.addActionListener(e -> refreshTally());
        
        actionPanel.add(refreshBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshTally();
    }

    private void refreshTally() {
        tableModel.setRowCount(0);
        chartPanel.removeAll();
        int totalSeats = 140; // Kerala Assembly total seats
        boolean dataFound = false;

        try (Connection conn = DatabaseHelper.getConnection()) {
            String wonQuery = "SELECT can.party_name, COUNT(*) as won_count FROM FinalResults fr JOIN Candidates can ON fr.candidate_id = can.candidate_id WHERE fr.is_winner = TRUE GROUP BY can.party_name";
            PreparedStatement wonPstmt = conn.prepareStatement(wonQuery);
            ResultSet wonRs = wonPstmt.executeQuery();
            
            while (wonRs.next()) {
                String party = wonRs.getString("party_name");
                int count = wonRs.getInt("won_count");
                
                addTallyRow(party, count, totalSeats);
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Tally data fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            // No party seat data available
        }
        
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void addTallyRow(String party, int count, int total) {
        tableModel.addRow(new Object[]{party, count, 0, count});
        
        JPanel barRow = new JPanel(new BorderLayout(10, 0));
        barRow.setOpaque(false);
        barRow.setMaximumSize(new Dimension(800, 40));
        JLabel partyLbl = new JLabel(party);
        partyLbl.setPreferredSize(new Dimension(150, 30));
        
        JProgressBar bar = new JProgressBar(0, total);
        bar.setValue(count);
        bar.setStringPainted(true);
        bar.setString(count + " Seats");
        bar.setForeground(ModernUI.ACCENT_COLOR);
        
        barRow.add(partyLbl, BorderLayout.WEST);
        barRow.add(bar, BorderLayout.CENTER);
        chartPanel.add(barRow);
        chartPanel.add(Box.createVerticalStrut(5));
    }
}
