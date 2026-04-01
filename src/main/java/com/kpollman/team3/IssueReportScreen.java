package com.kpollman.team3;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;

public class IssueReportScreen extends JPanel {
    private JComboBox<String> statusFilter;

    public IssueReportScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Issue Report Generator");
        titleLabel.setFont(ModernUI.TITLE_FONT);
        titleLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new IssueTrackingDashboard()));
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
        card.add(new JLabel("Filter by Status"), gbc);
        statusFilter = new JComboBox<>(new String[]{"All", "Open", "In-Progress", "Resolved", "Closed"});
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 25, 0);
        card.add(statusFilter, gbc);

        ModernUI.ModernButton generateBtn = new ModernUI.ModernButton("Generate CSV Report");
        generateBtn.setBackground(ModernUI.PRIMARY_COLOR);
        gbc.gridy = 2;
        card.add(generateBtn, gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        generateBtn.addActionListener(e -> generateReport());
    }

    private void generateReport() {
        String selectedStatus = (String) statusFilter.getSelectedItem();

        try (Connection conn = DatabaseHelper.getConnection()) {
            String filePath = "Issue_Report.csv";
            FileWriter writer = new FileWriter(filePath);
            writer.append("Issue ID,Category,Booth ID,Urgency,Status,Reported By,Reported At,Resolved At,Resolution Time\n");

            String query = "SELECT * FROM PollingIssues WHERE booth_id=?";
            if (!"All".equals(selectedStatus)) query += " AND status=?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, Session.boothId);
            if (!"All".equals(selectedStatus)) pstmt.setString(2, selectedStatus);

            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                Timestamp reported = rs.getTimestamp("reported_at");
                Timestamp resolved = rs.getTimestamp("resolved_at");
                String resolutionTime = "N/A";
                if (reported != null && resolved != null) {
                    long diff = (resolved.getTime() - reported.getTime()) / (1000 * 60);
                    resolutionTime = diff + " min";
                }

                writer.append(rs.getInt("issue_id") + ",");
                writer.append(rs.getString("category") + ",");
                writer.append(rs.getInt("booth_id") + ",");
                writer.append(rs.getString("urgency_level") + ",");
                writer.append(rs.getString("status") + ",");
                writer.append(rs.getString("reported_by") + ",");
                writer.append(reported + ",");
                writer.append(resolved + ",");
                writer.append(resolutionTime + "\n");
                count++;
            }
            writer.flush();
            writer.close();
            JOptionPane.showMessageDialog(this, "Report generated successfully!\nFile saved as: " + filePath + "\nEntries: " + count);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
