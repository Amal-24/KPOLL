package com.kpollman.team4;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Hourly Turnout Updater Screen as a JPanel for integration.
 */
public class HourlyTurnoutUpdaterScreen extends JPanel {
    private int boothId;
    private JComboBox<Integer> hourDropdown;
    private ModernUI.ModernTextField maleField, femaleField, thirdGenderField;
    private ModernUI.ModernButton updateButton;

    public HourlyTurnoutUpdaterScreen(int boothId) {
        this.boothId = boothId;
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Hourly Turnout Updater (Booth " + boothId + ")");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new TurnoutDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.gridx = 0;

        gbc.gridy = 0;
        card.add(new JLabel("Select Reporting Hour (24h)"), gbc);
        Integer[] hours = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        hourDropdown = new JComboBox<>(hours);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(hourDropdown, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Male Votes Count"), gbc);
        maleField = new ModernUI.ModernTextField("0");
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(maleField, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Female Votes Count"), gbc);
        femaleField = new ModernUI.ModernTextField("0");
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(femaleField, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Third Gender Votes Count"), gbc);
        thirdGenderField = new ModernUI.ModernTextField("0");
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(thirdGenderField, gbc);

        updateButton = new ModernUI.ModernButton("Save Hourly Turnout");
        updateButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 8;
        card.add(updateButton, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        updateButton.addActionListener(e -> updateTurnout());
        hourDropdown.addActionListener(e -> fetchCurrentTurnout());
        fetchCurrentTurnout();
    }

    private void fetchCurrentTurnout() {
        int selectedHour = (int) hourDropdown.getSelectedItem();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT male_votes, female_votes, third_gender_votes FROM HourlyTurnout WHERE booth_id = ? AND report_hour = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, boothId);
            pstmt.setInt(2, selectedHour);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                maleField.setText(String.valueOf(rs.getInt("male_votes")));
                femaleField.setText(String.valueOf(rs.getInt("female_votes")));
                thirdGenderField.setText(String.valueOf(rs.getInt("third_gender_votes")));
            } else {
                maleField.setText("0");
                femaleField.setText("0");
                thirdGenderField.setText("0");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTurnout() {
        try {
            int selectedHour = (int) hourDropdown.getSelectedItem();
            int male = Integer.parseInt(maleField.getText());
            int female = Integer.parseInt(femaleField.getText());
            int third = Integer.parseInt(thirdGenderField.getText());

            try (Connection conn = DatabaseHelper.getConnection()) {
                String insertQuery = "INSERT INTO HourlyTurnout (booth_id, report_hour, male_votes, female_votes, third_gender_votes) " +
                                     "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE male_votes = ?, female_votes = ?, third_gender_votes = ?, last_updated = CURRENT_TIMESTAMP";
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setInt(1, boothId);
                pstmt.setInt(2, selectedHour);
                pstmt.setInt(3, male);
                pstmt.setInt(4, female);
                pstmt.setInt(5, third);
                pstmt.setInt(6, male);
                pstmt.setInt(7, female);
                pstmt.setInt(8, third);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Hourly turnout updated successfully!");
                    MainDashboard.showView(new TurnoutDashboard());
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid integers");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating turnout: " + ex.getMessage());
        }
    }
}
