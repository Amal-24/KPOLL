package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CountingProgressTrackerScreen extends JPanel {
    private JPanel progressPanel;

    public CountingProgressTrackerScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Counting Progress Tracker");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(progressPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setOpaque(false);
        contentContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        contentContainer.add(scrollPane);
        add(contentContainer, BorderLayout.CENTER);

        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Progress");
        refreshBtn.addActionListener(e -> refreshProgress());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(refreshBtn);
        add(footer, BorderLayout.SOUTH);

        refreshProgress();
    }

    private void refreshProgress() {
        progressPanel.removeAll();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_name, COUNT(DISTINCT rr.round_no) as rounds_completed FROM Constituencies c LEFT JOIN CountingTables ct ON c.constituency_id = ct.constituency_id LEFT JOIN RoundResults rr ON ct.table_id = rr.table_id GROUP BY c.constituency_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("constituency_name");
                int rounds = rs.getInt("rounds_completed");
                
                JPanel p = new JPanel(new BorderLayout());
                p.setBackground(Color.WHITE);
                p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ModernUI.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
                
                JLabel nameLabel = new JLabel(name);
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                p.add(nameLabel, BorderLayout.NORTH);
                
                JProgressBar bar = new JProgressBar(0, 14);
                bar.setValue(rounds);
                bar.setStringPainted(true);
                bar.setForeground(new Color(16, 185, 129)); // Emerald 500
                bar.setPreferredSize(new Dimension(0, 25));
                
                JPanel barPanel = new JPanel(new BorderLayout());
                barPanel.setOpaque(false);
                barPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                barPanel.add(bar);
                p.add(barPanel, BorderLayout.CENTER);
                
                progressPanel.add(p);
            }
            progressPanel.revalidate();
            progressPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
