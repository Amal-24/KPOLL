package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Round-wise Result Entry Screen to enter votes per candidate per round.
 */
public class RoundWiseResultEntryScreen extends JPanel {
    private JComboBox<String> constituencyBox;
    private JComboBox<String> tableBox;
    private JComboBox<Integer> roundBox;
    private JComboBox<String> candidateBox;
    private ModernUI.ModernTextField votesField;

    public RoundWiseResultEntryScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Round-wise Result Entry");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);

        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.gridx = 0;

        gbc.gridy = 0;
        card.add(new JLabel("Select Constituency"), gbc);
        constituencyBox = new JComboBox<>();
        loadConstituencies();
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        card.add(constituencyBox, gbc);
        constituencyBox.addActionListener(e -> {
            loadTables();
            loadCandidates();
        });

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Select Counting Table"), gbc);
        tableBox = new JComboBox<>();
        loadTables();
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        card.add(tableBox, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Select Round Number"), gbc);
        roundBox = new JComboBox<>();
        for (int i = 1; i <= 20; i++) roundBox.addItem(i);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 15, 0);
        card.add(roundBox, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Select Candidate"), gbc);
        candidateBox = new JComboBox<>();
        loadCandidates();
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 15, 0);
        card.add(candidateBox, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Enter Votes Counted"), gbc);
        votesField = new ModernUI.ModernTextField("0");
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 25, 0);
        card.add(votesField, gbc);

        ModernUI.ModernButton saveBtn = new ModernUI.ModernButton("Save Round Result");
        saveBtn.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 10;
        card.add(saveBtn, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        saveBtn.addActionListener(e -> saveResult());
    }

    private void loadConstituencies() {
        constituencyBox.removeAllItems();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT constituency_id, constituency_name FROM Constituencies ORDER BY constituency_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                constituencyBox.addItem(rs.getInt("constituency_id") + " - " + rs.getString("constituency_name"));
            }
        } catch (Exception e) {
            System.err.println("Error loading constituencies: " + e.getMessage());
        }
    }

    private void loadTables() {
        tableBox.removeAllItems();
        if (constituencyBox.getSelectedIndex() < 0) return;
        try {
            int constituencyId = getIdFromCombo((String) constituencyBox.getSelectedItem());
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "SELECT ct.table_id, cc.center_name " +
                               "FROM CountingTables ct " +
                               "JOIN CountingCenters cc ON ct.center_id = cc.center_id " +
                               "WHERE ct.constituency_id = ? ORDER BY ct.table_id";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, constituencyId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    tableBox.addItem(rs.getInt("table_id") + " - " + rs.getString("center_name"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading tables: " + e.getMessage());
        }
    }

    private void loadCandidates() {
        candidateBox.removeAllItems();
        if (constituencyBox.getSelectedIndex() < 0) return;

        try {
            int constituencyId = getIdFromCombo((String) constituencyBox.getSelectedItem());
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "SELECT candidate_id, candidate_name FROM Candidates WHERE constituency_id = ? ORDER BY candidate_name";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, constituencyId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    candidateBox.addItem(rs.getInt("candidate_id") + " - " + rs.getString("candidate_name"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading candidates: " + e.getMessage());
        }
    }

    private void saveResult() {
        try {
            int votes = Integer.parseInt(votesField.getText().trim());
            if (votes < 0) throw new NumberFormatException();

            String tableItem = (String) tableBox.getSelectedItem();
            String candidateItem = (String) candidateBox.getSelectedItem();
            Integer roundValue = (Integer) roundBox.getSelectedItem();
            if (tableItem == null || candidateItem == null || roundValue == null) {
                JOptionPane.showMessageDialog(this, "Please select table, candidate, and round.");
                return;
            }

            int tableId = getIdFromCombo(tableItem);
            int candidateId = getIdFromCombo(candidateItem);
            int round = roundValue;

            try (Connection conn = DatabaseHelper.getConnection()) {
                String checkQuery = "SELECT result_id FROM RoundResults WHERE table_id = ? AND round_no = ? AND candidate_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, tableId);
                checkStmt.setInt(2, round);
                checkStmt.setInt(3, candidateId);
                ResultSet rs = checkStmt.executeQuery();

                int rows;
                if (rs.next()) {
                    int resultId = rs.getInt("result_id");
                    String updateQuery = "UPDATE RoundResults SET votes_counted = ?, is_verified = FALSE WHERE result_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, votes);
                    updateStmt.setInt(2, resultId);
                    rows = updateStmt.executeUpdate();
                } else {
                    String insertQuery = "INSERT INTO RoundResults (round_no, table_id, candidate_id, votes_counted, is_verified) VALUES (?, ?, ?, ?, FALSE)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setInt(1, round);
                    insertStmt.setInt(2, tableId);
                    insertStmt.setInt(3, candidateId);
                    insertStmt.setInt(4, votes);
                    rows = insertStmt.executeUpdate();
                }

                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Round result saved successfully.");
                    MainDashboard.showView(new CountingProgressTrackerScreen());
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid non-negative number for votes.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving result: " + ex.getMessage());
        }
    }

    private int getIdFromCombo(String value) {
        return Integer.parseInt(value.split(" - ")[0].trim());
    }
}
