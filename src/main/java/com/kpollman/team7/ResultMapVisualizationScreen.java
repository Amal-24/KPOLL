package com.kpollman.team7;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResultMapVisualizationScreen extends JPanel {
    private JPanel mapPanel;

    public ResultMapVisualizationScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Election Results Visualization Map");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new LiveResultsDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMockMap(g);
            }
        };
        mapPanel.setBackground(Color.WHITE);
        mapPanel.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setOpaque(false);
        mapContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mapContainer.add(mapPanel);
        add(mapContainer, BorderLayout.CENTER);

        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Visualization");
        refreshBtn.addActionListener(e -> mapPanel.repaint());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(refreshBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void drawMockMap(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT c.constituency_name, can.party_name FROM FinalResults fr JOIN Candidates can ON fr.candidate_id = can.candidate_id JOIN Constituencies c ON fr.constituency_id = c.constituency_id WHERE fr.is_winner = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            int x = 50, y = 50, count = 0;
            while (rs.next()) {
                String party = rs.getString("party_name");
                if (party.contains("Party A")) g2.setColor(new Color(239, 68, 68)); // Red 500
                else if (party.contains("Party B")) g2.setColor(new Color(59, 130, 246)); // Blue 500
                else g2.setColor(new Color(34, 197, 94)); // Green 500
                
                g2.fillRoundRect(x, y, 60, 60, 10, 10);
                g2.setColor(ModernUI.TEXT_COLOR_DARK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String name = rs.getString("constituency_name");
                g2.drawString(name, x, y + 75);
                
                x += 120;
                if (x > getWidth() - 100) { x = 50; y += 100; }
                count++;
            }
            if (count == 0) {
                g2.setColor(Color.GRAY);
                g2.setFont(ModernUI.MAIN_FONT);
                g2.drawString("No official winners declared yet to color the map.", 150, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
