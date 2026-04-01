package com.kpollman.team4;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GenderAnalyticsScreen extends JPanel {
    private JProgressBar maleBar, femaleBar, thirdGenderBar;
    private JLabel maleLabel, femaleLabel, thirdGenderLabel;

    public GenderAnalyticsScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Gender-wise Voter Turnout");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new TurnoutDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(20, ModernUI.CARD_BACKGROUND);
        card.setLayout(new GridLayout(6, 1, 10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        maleLabel = new JLabel("Male Turnout: 0");
        maleLabel.setFont(ModernUI.MAIN_FONT);
        maleBar = new JProgressBar(0, 100);
        maleBar.setStringPainted(true);
        maleBar.setForeground(new Color(59, 130, 246)); // Blue 500

        femaleLabel = new JLabel("Female Turnout: 0");
        femaleLabel.setFont(ModernUI.MAIN_FONT);
        femaleBar = new JProgressBar(0, 100);
        femaleBar.setStringPainted(true);
        femaleBar.setForeground(new Color(236, 72, 153)); // Pink 500

        thirdGenderLabel = new JLabel("Third Gender Turnout: 0");
        thirdGenderLabel.setFont(ModernUI.MAIN_FONT);
        thirdGenderBar = new JProgressBar(0, 100);
        thirdGenderBar.setStringPainted(true);
        thirdGenderBar.setForeground(new Color(249, 115, 22)); // Orange 500

        card.add(maleLabel); card.add(maleBar);
        card.add(femaleLabel); card.add(femaleBar);
        card.add(thirdGenderLabel); card.add(thirdGenderBar);

        add(card, BorderLayout.CENTER);

        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Analytics");
        refreshBtn.addActionListener(e -> fetchGenderStats());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(refreshBtn);
        add(footer, BorderLayout.SOUTH);

        fetchGenderStats();
    }

    private void fetchGenderStats() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT SUM(male_votes) as male, SUM(female_votes) as female, SUM(third_gender_votes) as third FROM HourlyTurnout";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int male = rs.getInt("male");
                int female = rs.getInt("female");
                int third = rs.getInt("third");
                int total = male + female + third;

                if (total > 0) {
                    maleLabel.setText("Male Turnout: " + String.format("%,d", male));
                    maleBar.setValue((male * 100) / total);

                    femaleLabel.setText("Female Turnout: " + String.format("%,d", female));
                    femaleBar.setValue((female * 100) / total);

                    thirdGenderLabel.setText("Third Gender Turnout: " + String.format("%,d", third));
                    thirdGenderBar.setValue((third * 100) / total);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
