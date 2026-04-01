package com.kpollman.team3;

import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;

public class IssueCategorySelectionScreen extends JPanel {
    public IssueCategorySelectionScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("What type of issue are you reporting?", JLabel.CENTER);
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel categoriesPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        categoriesPanel.setOpaque(false);

        categoriesPanel.add(createCategoryCard("EVM", new Color(239, 68, 68))); // Red 500
        categoriesPanel.add(createCategoryCard("Voter List", new Color(59, 130, 246))); // Blue 500
        categoriesPanel.add(createCategoryCard("Accessibility", new Color(34, 197, 94))); // Green 500
        categoriesPanel.add(createCategoryCard("Law & Order", new Color(249, 115, 22))); // Orange 500

        add(categoriesPanel, BorderLayout.CENTER);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new IssueTrackingDashboard()));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createCategoryCard(String category, Color color) {
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(20, ModernUI.CARD_BACKGROUND);
        card.setLayout(new GridBagLayout());
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel label = new JLabel(category);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(color);
        card.add(label);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                IssueReportingForm form = new IssueReportingForm(category);
                MainDashboard.showView(form);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 250, 252));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(ModernUI.CARD_BACKGROUND);
            }
        });

        return card;
    }
}
