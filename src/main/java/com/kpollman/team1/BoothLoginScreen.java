package com.kpollman.team1;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BoothLoginScreen extends JPanel {
    private ModernUI.ModernTextField boothIdField;
    private JPasswordField passwordField;
    private ModernUI.ModernButton loginButton;
    private LoginListener loginListener;

    public interface LoginListener {
        void onLoginSuccess(int boothId, String boothName, int constituencyId);
    }

    public BoothLoginScreen(LoginListener loginListener) {
        this.loginListener = loginListener;
        setBackground(ModernUI.BACKGROUND_COLOR);
        setLayout(new GridBagLayout());

        // Center Login Card
        ModernUI.RoundedPanel loginCard = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        loginCard.setPreferredSize(new Dimension(450, 550));
        loginCard.setLayout(new GridBagLayout());
        loginCard.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Title
        JLabel titleLabel = new JLabel("Booth Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        gbc.gridy = 0;
        loginCard.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("K-PollMan 2026", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        subtitleLabel.setForeground(ModernUI.ACCENT_COLOR);
        gbc.gridy = 1;
        loginCard.add(subtitleLabel, gbc);

        gbc.insets = new Insets(20, 0, 5, 0);
        
        // Booth ID
        JLabel boothIdLabel = new JLabel("Booth ID");
        boothIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 2;
        loginCard.add(boothIdLabel, gbc);

        boothIdField = new ModernUI.ModernTextField("");
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        loginCard.add(boothIdField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 5, 0);
        loginCard.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(ModernUI.MAIN_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 25, 0);
        loginCard.add(passwordField, gbc);

        // Login Button
        loginButton = new ModernUI.ModernButton("Login");
        loginButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 20, 0);
        loginCard.add(loginButton, gbc);

        // Links
        JLabel forgotPassword = new JLabel("Forgot Password?", JLabel.RIGHT);
        forgotPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        loginCard.add(forgotPassword, gbc);

        JLabel contactAdmin = new JLabel("System Administrator Contact", JLabel.CENTER);
        contactAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 8;
        gbc.insets = new Insets(20, 0, 0, 0);
        loginCard.add(contactAdmin, gbc);

        add(loginCard);

        loginButton.addActionListener(e -> authenticate());
    }

    private void authenticate() {
        String boothIdStr = boothIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (boothIdStr.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Booth ID and Password", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT * FROM Booths WHERE booth_id = ? AND booth_password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, Integer.parseInt(boothIdStr));
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String boothName = rs.getString("booth_name");
                int constituencyId = rs.getInt("constituency_id");
                
                if (loginListener != null) {
                    loginListener.onLoginSuccess(Integer.parseInt(boothIdStr), boothName, constituencyId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Booth ID or Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
