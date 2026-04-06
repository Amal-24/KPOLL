package com.kpollman.team2;

import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;

/**
 * Wait Time Calculator Screen.
 */
public class WaitTimeCalculatorScreen extends JPanel {
    private ModernUI.ModernTextField queueLengthField;
    private ModernUI.ModernTextField activeStationsField;
    private JLabel resultLabel;
    private QueueAlgorithm algorithm;

    public WaitTimeCalculatorScreen() {
        this.algorithm = new StandardWaitTimeAlgorithm();

        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Estimated Wait Time Calculator");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new QueueStatusDashboard()));
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
        card.add(new JLabel("Enter Queue Length (Voters)"), gbc);
        queueLengthField = new ModernUI.ModernTextField("0");
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(queueLengthField, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0);
        card.add(new JLabel("Enter Number of Active Stations"), gbc);
        activeStationsField = new ModernUI.ModernTextField("1");
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(activeStationsField, gbc);

        ModernUI.ModernButton calculateBtn = new ModernUI.ModernButton("Calculate Wait Time");
        calculateBtn.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 4;
        card.add(calculateBtn, gbc);

        resultLabel = new JLabel("Estimated Wait: 0 mins", JLabel.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        resultLabel.setForeground(ModernUI.ACCENT_COLOR);
        gbc.gridy = 5; gbc.insets = new Insets(20, 0, 0, 0);
        card.add(resultLabel, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        calculateBtn.addActionListener(e -> {
            try {
                int qLen = Integer.parseInt(queueLengthField.getText());
                int stations = Integer.parseInt(activeStationsField.getText());
                int waitTime = algorithm.calculateWaitTime(qLen, stations);
                resultLabel.setText("Estimated Wait: " + waitTime + " mins");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid integers");
            }
        });
    }
}
