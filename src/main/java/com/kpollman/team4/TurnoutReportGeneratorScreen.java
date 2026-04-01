package com.kpollman.team4;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TurnoutReportGeneratorScreen extends JPanel {
    private JTextArea reportArea;
    private JComboBox<String> constituencyDropdown;
    private AnalyticsEngine engine;

    public TurnoutReportGeneratorScreen() {
        this.engine = new KeralaElectionAnalytics();
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Turnout Report Generator");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new TurnoutDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Selection Area
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        selectionPanel.setOpaque(false);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        constituencyDropdown = new JComboBox<>();
        constituencyDropdown.setFont(ModernUI.MAIN_FONT);
        loadConstituencies();
        
        selectionPanel.add(new JLabel("Select Constituency:"));
        selectionPanel.add(constituencyDropdown);
        
        ModernUI.ModernButton summaryButton = new ModernUI.ModernButton("Summary Report");
        summaryButton.addActionListener(e -> generateSummaryReport());
        
        ModernUI.ModernButton detailedButton = new ModernUI.ModernButton("Detailed Report");
        detailedButton.setBackground(ModernUI.ACCENT_COLOR);
        detailedButton.addActionListener(e -> generateDetailedReport());
        
        selectionPanel.add(summaryButton);
        selectionPanel.add(detailedButton);

        add(selectionPanel, BorderLayout.CENTER); // Will be replaced by a nested panel for layout

        // Main Content Panel for Selection + Report
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(selectionPanel, BorderLayout.NORTH);

        reportArea = new JTextArea();
        reportArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        reportArea.setEditable(false);
        reportArea.setMargin(new Insets(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void loadConstituencies() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT constituency_name FROM Constituencies";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                constituencyDropdown.addItem(rs.getString("constituency_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateSummaryReport() {
        String name = (String) constituencyDropdown.getSelectedItem();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT SUM(ht.male_votes + ht.female_votes + ht.third_gender_votes) as total " +
                           "FROM Constituencies c " +
                           "JOIN Booths b ON c.constituency_id = b.constituency_id " +
                           "JOIN HourlyTurnout ht ON b.booth_id = ht.booth_id " +
                           "WHERE c.constituency_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                String report = engine.generateReport(name, total);
                reportArea.setText("--- Turnout Summary Report ---\n\n" + report);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateDetailedReport() {
        String name = (String) constituencyDropdown.getSelectedItem();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT SUM(ht.male_votes) as male, SUM(ht.female_votes) as female, SUM(ht.third_gender_votes) as third " +
                           "FROM Constituencies c " +
                           "JOIN Booths b ON c.constituency_id = b.constituency_id " +
                           "JOIN HourlyTurnout ht ON b.booth_id = ht.booth_id " +
                           "WHERE c.constituency_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int male = rs.getInt("male");
                int female = rs.getInt("female");
                int third = rs.getInt("third");
                String report = engine.generateReport(name, male, female, third);
                reportArea.setText("--- Detailed Turnout Report ---\n\n" + report);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
