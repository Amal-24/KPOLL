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
    private boolean isPasswordMode;
    private int prefilledBoothId;

    public interface LoginListener {
        void onLoginSuccess(int boothId, String boothName, int constituencyId);
    }

    // Constructor for ID-only login
    public BoothLoginScreen(LoginListener loginListener) {
        this(loginListener, false, -1);
    }

    // Constructor for Password-only login
    public BoothLoginScreen(int boothId, LoginListener loginListener) {
        this(loginListener, true, boothId);
    }

    private BoothLoginScreen(LoginListener loginListener, boolean isPasswordMode, int boothId) {
        this.loginListener = loginListener;
        this.isPasswordMode = isPasswordMode;
        this.prefilledBoothId = boothId;

        setBackground(ModernUI.BACKGROUND_COLOR);
        setLayout(new GridBagLayout());

        // Center Login Card
        ModernUI.RoundedPanel loginCard = new ModernUI.RoundedPanel(30, ModernUI.CARD_BACKGROUND);
        loginCard.setPreferredSize(new Dimension(450, isPasswordMode ? 500 : 500));
        loginCard.setLayout(new GridBagLayout());
        loginCard.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Title
        JLabel titleLabel = new JLabel(isPasswordMode ? "Verify Booth Password" : "Booth Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        gbc.gridy = 0;
        loginCard.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel(isPasswordMode ? "Enter your booth password to access Dashboard" : "Enter your Booth ID to continue", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        subtitleLabel.setForeground(ModernUI.ACCENT_COLOR);
        gbc.gridy = 1;
        loginCard.add(subtitleLabel, gbc);

        gbc.insets = new Insets(20, 0, 5, 0);
        
        if (!isPasswordMode) {
            // Booth ID
            JLabel boothIdLabel = new JLabel("Booth ID");
            boothIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            gbc.gridy = 2;
            loginCard.add(boothIdLabel, gbc);

            boothIdField = new ModernUI.ModernTextField("");
            gbc.gridy = 3;
            gbc.insets = new Insets(0, 0, 15, 0);
            loginCard.add(boothIdField, gbc);
        } else {
            // Booth ID (read-only)
            JLabel boothIdLabel = new JLabel("Booth ID");
            boothIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            gbc.gridy = 2;
            loginCard.add(boothIdLabel, gbc);

            ModernUI.ModernTextField boothIdDisplay = new ModernUI.ModernTextField(String.valueOf(boothId));
            boothIdDisplay.setEnabled(false);
            gbc.gridy = 3;
            gbc.insets = new Insets(0, 0, 15, 0);
            loginCard.add(boothIdDisplay, gbc);

            // Password
            JLabel passwordLabel = new JLabel("Password");
            passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            gbc.gridy = 4;
            gbc.insets = new Insets(10, 0, 5, 0);
            loginCard.add(passwordLabel, gbc);

            passwordField = new JPasswordField();
            passwordField.setFont(ModernUI.MAIN_FONT);
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                new ModernUI.ModernTextField.RoundedBorder(10, ModernUI.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            gbc.gridy = 5;
            gbc.insets = new Insets(0, 0, 25, 0);
            loginCard.add(passwordField, gbc);
        }

        // Login Button
        loginButton = new ModernUI.ModernButton(isPasswordMode ? "Verify" : "Login");
        loginButton.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 20, 0);
        loginCard.add(loginButton, gbc);

        // Links
        JLabel contactAdmin = new JLabel("System Administrator Contact", JLabel.CENTER);
        contactAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 0, 0, 0);
        loginCard.add(contactAdmin, gbc);

        add(loginCard);

        loginButton.addActionListener(e -> authenticate());
    }

    private void authenticate() {
        if (!isPasswordMode) {
            String boothIdStr = boothIdField.getText().trim();
            if (boothIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Booth ID", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "SELECT * FROM Booths WHERE booth_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, Integer.parseInt(boothIdStr));
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (loginListener != null) {
                        loginListener.onLoginSuccess(rs.getInt("booth_id"), rs.getString("booth_name"), rs.getInt("constituency_id"));
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Booth ID", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            String password = new String(passwordField.getPassword()).trim();
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Password", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "SELECT * FROM Booths WHERE booth_id = ? AND booth_password = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, prefilledBoothId);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (loginListener != null) {
                        loginListener.onLoginSuccess(rs.getInt("booth_id"), rs.getString("booth_name"), rs.getInt("constituency_id"));
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
