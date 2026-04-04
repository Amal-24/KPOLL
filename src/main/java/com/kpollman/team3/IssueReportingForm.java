package com.kpollman.team3;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class IssueReportingForm extends JPanel {

    private JTextArea descriptionArea;
    private ModernUI.ModernTextField boothIdField;
    private JComboBox<String> urgencyDropdown;
    private JComboBox<String> categoryDropdown;

    public IssueReportingForm(String defaultCategory) {

        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        ModernUI.RoundedPanel formCard = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Report New Polling Issue", JLabel.CENTER);
        titleLabel.setFont(ModernUI.HEADER_FONT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Category
        gbc.gridy = 1; gbc.gridx = 0;
        formCard.add(new JLabel("Category:"), gbc);

        String[] categories = {"EVM", "Voter List", "Accessibility", "Law & Order"};
        categoryDropdown = new JComboBox<>(categories);
        categoryDropdown.setSelectedItem(defaultCategory);

        gbc.gridx = 1;
        formCard.add(categoryDropdown, gbc);

        // Booth ID
        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Booth ID:"), gbc);

        boothIdField = new ModernUI.ModernTextField(String.valueOf(Session.boothId));
        gbc.gridx = 1;
        formCard.add(boothIdField, gbc);

        // Urgency
        gbc.gridx = 0; gbc.gridy = 3;
        formCard.add(new JLabel("Urgency Level:"), gbc);

        urgencyDropdown = new JComboBox<>(new String[]{"Low", "Medium", "High", "Critical"});
        gbc.gridx = 1;
        formCard.add(urgencyDropdown, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 4;
        formCard.add(new JLabel("Description:"), gbc);

        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setFont(ModernUI.MAIN_FONT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));

        gbc.gridx = 1;
        formCard.add(scrollPane, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        ModernUI.ModernButton submitButton = new ModernUI.ModernButton("Submit Report");
        submitButton.addActionListener(e -> submitIssue());

        ModernUI.ModernButton cancelBtn = new ModernUI.ModernButton("Cancel");
        cancelBtn.setBackground(ModernUI.ACCENT_COLOR);
        cancelBtn.addActionListener(e -> MainDashboard.showView(new IssueTrackingDashboard()));

        btnPanel.add(submitButton);
        btnPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formCard.add(btnPanel, gbc);

        add(formCard, BorderLayout.CENTER);
    }

    private void submitIssue() {

    String description = descriptionArea.getText().trim();
    String boothIdStr = boothIdField.getText().trim();
    String urgency = (String) urgencyDropdown.getSelectedItem();
    String category = (String) categoryDropdown.getSelectedItem();

    // 🔥 CRITICAL FIX: Convert category for database
    switch(category) {
        case "Voter List":
            category = "VoterList";
            break;
        case "Law & Order":
            category = "LawAndOrder";
            break;
        case "EVM":
            category = "EVM";
            break;
        case "Accessibility":
            category = "Accessibility";
            break;
    }

    System.out.println("🔍 Category for DB: " + category);

    if (description.isEmpty() || boothIdStr.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in all required fields");
        return;
    }

    int boothId;
    try {
        boothId = Integer.parseInt(boothIdStr);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Invalid Booth ID");
        return;
    }

    try (Connection conn = DatabaseHelper.getConnection()) {

        String insertQuery = "INSERT INTO PollingIssues (category, description, booth_id, urgency_level, status, reported_by) VALUES (?, ?, ?, ?, 'Open', ?)";

        PreparedStatement pstmt = conn.prepareStatement(insertQuery);
        pstmt.setString(1, category);
        pstmt.setString(2, description);
        pstmt.setInt(3, boothId);
        pstmt.setString(4, urgency);
        pstmt.setInt(5, Session.userId);

        pstmt.executeUpdate();

        JOptionPane.showMessageDialog(this, "Issue reported successfully!");
        MainDashboard.showView(new IssueTrackingDashboard());

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        ex.printStackTrace();
    }
}
}