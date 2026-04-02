package com.kpollman.team4;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Hourly Turnout Updater Screen.
 */
public class HourlyTurnoutUpdaterScreen extends JFrame {
    private int boothId;
    private JComboBox<Integer> hourDropdown;
    private JTextField maleField, femaleField, thirdGenderField;
    private ModernUI.ModernButton updateButton;

    public HourlyTurnoutUpdaterScreen(int boothId) {
        this.boothId = boothId;
        setTitle("K-PollMan 2026 - Hourly Turnout Updater (Booth " + boothId + ")");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Update Hourly Turnout", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; add(new JLabel("Reporting Hour (24h):"), gbc);
        Integer[] hours = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        hourDropdown = new JComboBox<>(hours);
        gbc.gridx = 1; add(hourDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Male Votes:"), gbc);
        maleField = new JTextField(10);
        gbc.gridx = 1; add(maleField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Female Votes:"), gbc);
        femaleField = new JTextField(10);
        gbc.gridx = 1; add(femaleField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; add(new JLabel("Third Gender Votes:"), gbc);
        thirdGenderField = new JTextField(10);
        gbc.gridx = 1; add(thirdGenderField, gbc);

        updateButton = new ModernUI.ModernButton("Save Turnout Data");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(updateButton, gbc);

        updateButton.addActionListener(e -> updateTurnout());
        
        // Load existing data for selected hour
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
                    JOptionPane.showMessageDialog(this, "Turnout data updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
