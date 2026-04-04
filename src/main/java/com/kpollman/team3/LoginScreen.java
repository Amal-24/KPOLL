package com.kpollman.team3;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginScreen extends JPanel {

    private ModernUI.ModernTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleDropdown;
    private JLabel passwordLabel;
    private JLabel usernameLabel;
    private ModernUI.ModernButton loginButton;
    private LoginListener loginListener;

    public interface LoginListener {
        void onLoginSuccess();
    }

    public LoginScreen(LoginListener loginListener) {
        this.loginListener = loginListener;
        setBackground(ModernUI.BACKGROUND_COLOR);
        setLayout(new GridBagLayout());

        ModernUI.RoundedPanel loginCard = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        loginCard.setPreferredSize(new Dimension(450, 550));
        loginCard.setLayout(new GridBagLayout());
        loginCard.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel titleLabel = new JLabel("Issue Reporting Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        gbc.gridy = 0;
        loginCard.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("K-PollMan 2026", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        subtitleLabel.setForeground(ModernUI.ACCENT_COLOR);
        gbc.gridy = 1;
        loginCard.add(subtitleLabel, gbc);

        gbc.insets = new Insets(20, 0, 5, 0);

        JLabel roleLabel = new JLabel("Login As");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 2;
        loginCard.add(roleLabel, gbc);

        roleDropdown = new JComboBox<>(new String[]{"Booth Official", "Voter"});
        roleDropdown.setFont(ModernUI.MAIN_FONT);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        loginCard.add(roleDropdown, gbc);

        usernameLabel = new JLabel("Booth ID");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 5, 0);
        loginCard.add(usernameLabel, gbc);

        usernameField = new ModernUI.ModernTextField("");
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 15, 0);
        loginCard.add(usernameField, gbc);

        passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 5, 0);
        loginCard.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(ModernUI.MAIN_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 25, 0);
        loginCard.add(passwordField, gbc);

        loginButton = new ModernUI.ModernButton("Login");
        loginButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 0, 20, 0);
        loginCard.add(loginButton, gbc);

        add(loginCard);

        roleDropdown.addActionListener(e -> {
            boolean isVoter = roleDropdown.getSelectedItem().equals("Voter");
            usernameLabel.setText(isVoter ? "EPIC Number" : "Booth ID");
            usernameField.setText("");
            passwordLabel.setVisible(!isVoter);
            passwordField.setVisible(!isVoter);
            revalidate();
            repaint();
        });

        loginButton.addActionListener(e -> authenticateUser());
    }

    private void authenticateUser() {

        // Reset session completely
        Session.userId = 0;
        Session.role = null;
        Session.boothId = 0;
        Session.constituencyId = 0;
        
        System.out.println("=== SESSION RESET ===");
        System.out.println("New login attempt starting...");

        String role = (String) roleDropdown.getSelectedItem();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DatabaseHelper.getConnection()) {

            if ("Booth Official".equals(role)) {

                int boothId;
                try {
                    boothId = Integer.parseInt(username);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Booth ID");
                    return;
                }

                String query = "SELECT * FROM Booths WHERE booth_id = ? AND booth_password = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, boothId);
                pstmt.setString(2, password);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int officialId = boothId + 1000;  // Add offset: 1 → 1001, 2 → 1002
                    Session.login(officialId, "OFFICIAL");
                    Session.boothId = boothId;
                    System.out.println("✅ Official logged in - ID: " + Session.userId);
                    
                    // 🔥 FIX: Call loginListener for Officials
                    if (loginListener != null) {
                        loginListener.onLoginSuccess();
                    } else {
                        System.out.println("❌ loginListener is NULL for Official");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Official Credentials");
                }

            } else {

                String query = "SELECT * FROM Voters WHERE epic_no = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int voterId = rs.getInt("serial_no");
                    
                    // Check if voter_id is NULL (returns 0 in Java)
                    if (voterId == 0 || rs.wasNull()) {
                        JOptionPane.showMessageDialog(this, 
                            "Database Error: Voter ID is missing.\nPlease contact administrator.");
                        System.out.println("❌ ERROR: voter_id is NULL for EPIC: " + username);
                        return;
                    }
                    
                    Session.login(voterId, "VOTER");
                    System.out.println("✅ Voter logged in - ID: " + Session.userId);
                    System.out.println("   EPIC: " + username);
                    
                    if (loginListener != null) {
                        loginListener.onLoginSuccess();
                    } else {
                        System.out.println("❌ loginListener is NULL for Voter");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid EPIC Number");
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}