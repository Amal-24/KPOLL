package com.kpollman.team1;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.model.Voter;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class VoterDetailsDisplayScreen extends JPanel {
    private Voter voter;
    private int boothId;

    public VoterDetailsDisplayScreen(Voter voter, int boothId) {
        this.voter = voter;
        this.boothId = boothId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Voter Identity Verification");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Search");
        backBtn.addActionListener(e -> MainDashboard.showView(new VoterAuthenticationScreen(boothId)));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.WEST;

        // Photo Placeholder
        JPanel photoPlaceholder = new JPanel();
        photoPlaceholder.setPreferredSize(new Dimension(150, 180));
        photoPlaceholder.setBackground(new Color(241, 245, 249));
        photoPlaceholder.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        photoPlaceholder.add(new JLabel("PHOTO"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 5;
        card.add(photoPlaceholder, gbc);

        gbc.gridheight = 1;
        gbc.gridx = 1;
        addDetailRow(card, "EPIC No:", voter.getEpicNo(), 0, gbc);
        addDetailRow(card, "Voter Name:", voter.getName(), 1, gbc);
        addDetailRow(card, "Age:", String.valueOf(voter.getAge()), 2, gbc);
        addDetailRow(card, "Serial No:", String.valueOf(voter.getSerialNo()), 3, gbc);
        
        JLabel statusLabel = new JLabel(voter.isVotedStatus() ? "ALREADY VOTED" : "ELIGIBLE TO VOTE");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(voter.isVotedStatus() ? new Color(239, 68, 68) : new Color(16, 185, 129));
        gbc.gridy = 4;
        card.add(statusLabel, gbc);

        add(card, BorderLayout.CENTER);

        // Footer Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        ModernUI.ModernButton verifyButton = new ModernUI.ModernButton("Confirm Identity & Mark as Voted");
        verifyButton.setBackground(ModernUI.PRIMARY_COLOR);
        verifyButton.setEnabled(!voter.isVotedStatus());
        verifyButton.addActionListener(e -> markAsVoted());

        actionPanel.add(verifyButton);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void addDetailRow(JPanel panel, String label, String value, int row, GridBagConstraints gbc) {
        gbc.gridy = row;
        JLabel lbl = new JLabel(label + " " + value);
        lbl.setFont(ModernUI.MAIN_FONT);
        panel.add(lbl, gbc);
    }

    private void markAsVoted() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to mark " + voter.getName() + " as voted?", "Confirm Action", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseHelper.getConnection()) {
            String updateQuery = "UPDATE Voters SET voted_status = TRUE WHERE epic_no = ?";
            PreparedStatement pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, voter.getEpicNo());
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Voter marked as voted successfully!");
                MainDashboard.showView(new BoothDashboard(boothId, "Current Booth", 0));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }
}
