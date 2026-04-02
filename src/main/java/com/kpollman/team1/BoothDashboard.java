package com.kpollman.team1;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BoothDashboard extends JPanel {
    private int boothId;
    private int constituencyId;
    private String boothName;
    private String constituencyName;

    private JLabel totalVotersLabel;
    private JLabel votesCastLabel;
    private JLabel remainingVotersLabel;
    private JLabel percentageLabel;

    public BoothDashboard(int boothId, String boothName, int constituencyId) {
        this.boothId = boothId;
        this.boothName = boothName;
        this.constituencyId = constituencyId;

        // If data is missing (e.g. coming back from other screens), fetch from DB
        if (this.boothName == null || this.boothName.equals("Current Booth") || this.constituencyId == 0) {
            fetchBoothDetails();
        }

        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Data");
        refreshBtn.addActionListener(e -> refreshStats());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Scroll Pane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(Box.createVerticalStrut(30));

        // Info and Percentage Row
        JPanel topRow = new JPanel(new GridLayout(1, 2, 30, 0));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(2000, 250));

        // Booth Info Card
        ModernUI.RoundedPanel infoCard = new ModernUI.RoundedPanel(20, ModernUI.CARD_BACKGROUND);
        infoCard.setLayout(new GridBagLayout());
        infoCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 20);

        constituencyName = fetchConstituencyName();
        addInfoRow(infoCard, "Booth No:", String.valueOf(boothId), 0, gbc);
        addInfoRow(infoCard, "Booth Name:", boothName, 1, gbc);
        addInfoRow(infoCard, "Constituency:", (constituencyName != null ? constituencyName : "Unknown"), 2, gbc);
        addInfoRow(infoCard, "Booth Type:", "Rural", 3, gbc); // Placeholder

        topRow.add(infoCard);

        // Percentage Card
        ModernUI.RoundedPanel percentCard = new ModernUI.RoundedPanel(20, ModernUI.CARD_BACKGROUND);
        percentCard.setLayout(new BorderLayout());
        percentCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        percentageLabel = new JLabel("0%", JLabel.CENTER);
        percentageLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        percentageLabel.setForeground(new Color(16, 185, 129)); // Emerald 500
        
        JLabel pctTitle = new JLabel("Voting Percentage", JLabel.CENTER);
        pctTitle.setFont(ModernUI.MAIN_FONT);
        
        percentCard.add(pctTitle, BorderLayout.NORTH);
        percentCard.add(percentageLabel, BorderLayout.CENTER);
        
        topRow.add(percentCard);
        contentPanel.add(topRow);

        contentPanel.add(Box.createVerticalStrut(30));

        // Stats Row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 30, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(2000, 150));

        totalVotersLabel = new JLabel("0");
        votesCastLabel = new JLabel("0");
        remainingVotersLabel = new JLabel("0");

        statsRow.add(createStatCard("Total Voters", totalVotersLabel));
        statsRow.add(createStatCard("Votes Cast", votesCastLabel));
        statsRow.add(createStatCard("Remaining", remainingVotersLabel));

        contentPanel.add(statsRow);
        
        // Actions Row
        contentPanel.add(Box.createVerticalStrut(30));
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionsRow.setOpaque(false);
        
        ModernUI.ModernButton authBtn = new ModernUI.ModernButton("Voter Authentication");
        authBtn.addActionListener(e -> MainDashboard.showView(new VoterAuthenticationScreen(boothId)));
        
        ModernUI.ModernButton statusBtn = new ModernUI.ModernButton("Voted Status Management");
        statusBtn.addActionListener(e -> MainDashboard.showView(new VotedStatusManagementScreen(boothId)));
        statusBtn.setBackground(ModernUI.ACCENT_COLOR);
        
        actionsRow.add(authBtn);
        actionsRow.add(Box.createHorizontalStrut(20));
        actionsRow.add(statusBtn);
        
        contentPanel.add(actionsRow);

        add(contentPanel, BorderLayout.CENTER);

        refreshStats();
    }

    private void addInfoRow(JPanel panel, String label, String value, int row, GridBagConstraints gbc) {
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUI.MAIN_FONT);
        lbl.setForeground(ModernUI.ACCENT_COLOR);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(val, gbc);
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(20, ModernUI.CARD_BACKGROUND);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ModernUI.MAIN_FONT);
        titleLabel.setForeground(ModernUI.ACCENT_COLOR);
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private String fetchConstituencyName() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT constituency_name FROM Constituencies WHERE constituency_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, constituencyId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("constituency_name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void fetchBoothDetails() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT booth_name, constituency_id FROM Booths WHERE booth_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, boothId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.boothName = rs.getString("booth_name");
                this.constituencyId = rs.getInt("constituency_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshStats() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Total Voters in this booth
            String totalQuery = "SELECT COUNT(*) FROM Voters WHERE booth_id = ?";
            PreparedStatement pstmtTotal = conn.prepareStatement(totalQuery);
            pstmtTotal.setInt(1, boothId);
            ResultSet rsTotal = pstmtTotal.executeQuery();
            int total = 0;
            if (rsTotal.next()) total = rsTotal.getInt(1);
            totalVotersLabel.setText(String.format("%,d", total));

            // Votes Cast
            String castQuery = "SELECT COUNT(*) FROM Voters WHERE booth_id = ? AND voted_status = TRUE";
            PreparedStatement pstmtCast = conn.prepareStatement(castQuery);
            pstmtCast.setInt(1, boothId);
            ResultSet rsCast = pstmtCast.executeQuery();
            int cast = 0;
            if (rsCast.next()) cast = rsCast.getInt(1);
            votesCastLabel.setText(String.format("%,d", cast));

            // Remaining
            remainingVotersLabel.setText(String.format("%,d", total - cast));
            
            // Percentage
            if (total > 0) {
                int pct = (int) ((cast * 100.0) / total);
                percentageLabel.setText(pct + "%");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching stats: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
