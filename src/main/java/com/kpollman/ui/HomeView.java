package com.kpollman.ui;

import javax.swing.*;
import java.awt.*;

public class HomeView extends JPanel {
    public HomeView() {
        setBackground(ModernUI.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Welcome Header
        JLabel welcomeLabel = new JLabel("Welcome to K-PollMan 2026");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel("Integrated Election Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(ModernUI.ACCENT_COLOR);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));

        // App Details Card
        ModernUI.RoundedPanel detailsCard = new ModernUI.RoundedPanel(20, ModernUI.CARD_BACKGROUND);
        detailsCard.setLayout(new GridBagLayout());
        detailsCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        detailsCard.setMaximumSize(new Dimension(800, 400));
        detailsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        String[][] details = {
            {"System Name:", "K-PollMan 2026"},
            {"Version:", "1.0.0-BETA"},
            {"Release Date:", "April 2026"},
            {"Core Modules:", "Booth Mgmt, Queue Analytics, Issue Tracking, Live Results"},
            {"Status:", "Election Ready"}
        };

        for (int i = 0; i < details.length; i++) {
            JLabel keyLabel = new JLabel(details[i][0]);
            keyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            gbc.gridy = i;
            gbc.gridx = 0;
            detailsCard.add(keyLabel, gbc);

            JLabel valLabel = new JLabel(details[i][1]);
            valLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 1;
            detailsCard.add(valLabel, gbc);
        }

        mainPanel.add(detailsCard);
        mainPanel.add(Box.createVerticalStrut(40));

        // Quick Info Section
        JLabel infoHeader = new JLabel("Quick Information");
        infoHeader.setFont(ModernUI.HEADER_FONT);
        infoHeader.setForeground(ModernUI.TEXT_COLOR_DARK);
        infoHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(infoHeader);
        mainPanel.add(Box.createVerticalStrut(20));

        String infoText = "<html>K-PollMan is a comprehensive solution designed to streamline the election process. " +
                "It provides real-time monitoring of polling booths, efficient queue management, " +
                "instant issue reporting, and transparent live result aggregation.</html>";
        JLabel infoBody = new JLabel(infoText);
        infoBody.setFont(ModernUI.MAIN_FONT);
        infoBody.setForeground(ModernUI.TEXT_COLOR_DARK);
        infoBody.setMaximumSize(new Dimension(800, 100));
        infoBody.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(infoBody);

        add(mainPanel, BorderLayout.CENTER);
    }
}
