package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Counting Progress Tracker Screen providing visual progress bars for each constituency.
 */
public class CountingProgressTrackerScreen extends JPanel {
    private JPanel progressPanel;

    public CountingProgressTrackerScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Counting Progress Tracker");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(progressPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        refreshProgress();
    }

    private void refreshProgress() {
        progressPanel.removeAll();
        boolean dataFound = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_name, " +
                           "(SELECT COUNT(DISTINCT round_number) FROM RoundResults WHERE constituency_id = c.constituency_id) as rounds_completed, " +
                           "20 as total_rounds " + // Assuming 20 rounds per constituency
                           "FROM Constituencies c";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("constituency_name");
                int completed = rs.getInt("rounds_completed");
                int total = rs.getInt("total_rounds");
                addProgressBar(name, completed, total);
                dataFound = true;
            }
        } catch (Exception e) {
            System.err.println("Progress fetch error: " + e.getMessage());
        }

        if (!dataFound) {
            // Mock data
            addProgressBar("Thiruvananthapuram (Mock)", 12, 20);
            addProgressBar("Ernakulam (Mock)", 8, 20);
            addProgressBar("Kozhikode (Mock)", 15, 20);
            addProgressBar("Wayanad (Mock)", 20, 20);
        }
        progressPanel.revalidate();
        progressPanel.repaint();
    }

    private void addProgressBar(String name, int completed, int total) {
        ModernUI.RoundedPanel card = new ModernUI.RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout(15, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(800, 100));

        JLabel nameLabel = new JLabel(name + " - Rounds: " + completed + " / " + total);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(nameLabel, BorderLayout.NORTH);

        JProgressBar bar = new JProgressBar(0, total);
        bar.setValue(completed);
        bar.setStringPainted(true);
        bar.setForeground(completed == total ? new Color(34, 197, 94) : ModernUI.ACCENT_COLOR);
        card.add(bar, BorderLayout.CENTER);

        progressPanel.add(card);
        progressPanel.add(Box.createVerticalStrut(20));
    }
}
