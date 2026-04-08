package com.kpollman.team4;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TurnoutDashboard extends JPanel {
    private JTable turnoutTable;
    private DefaultTableModel tableModel;
    private JPanel turnoutGraphPanel;
    private GenderDistributionChart genderChart;
    private JLabel overallTurnoutLabel;
    private JLabel topConstituencyLabel;
    private AnalyticsEngine engine;

    public TurnoutDashboard() {
        this.engine = new KeralaElectionAnalytics();
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Turnout Analytics Dashboard");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton refreshBtn = new ModernUI.ModernButton("Refresh Stats");
        refreshBtn.addActionListener(e -> refreshTurnoutStats());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Constituency Name", "Male", "Female", "TG", "Total", "Turnout %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        turnoutTable = new JTable(tableModel);
        turnoutTable.setFont(ModernUI.MAIN_FONT);
        turnoutTable.setRowHeight(40);
        turnoutTable.setShowVerticalLines(false);
        turnoutTable.setGridColor(ModernUI.BORDER_COLOR);
        turnoutTable.setSelectionBackground(new Color(0, 120, 215));
        turnoutTable.setSelectionForeground(Color.WHITE);
        
        JTableHeader header = turnoutTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ModernUI.ACCENT_COLOR);
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane tableScrollPane = new JScrollPane(turnoutTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR, 1));
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        // Graphs & charts section
        turnoutGraphPanel = new JPanel();
        turnoutGraphPanel.setLayout(new BoxLayout(turnoutGraphPanel, BoxLayout.Y_AXIS));
        turnoutGraphPanel.setBackground(Color.WHITE);
        turnoutGraphPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ModernUI.BORDER_COLOR),
                "Constituency-wise Turnout Graph"
            )
        );
        JScrollPane graphScrollPane = new JScrollPane(turnoutGraphPanel);
        graphScrollPane.setBorder(null);

        genderChart = new GenderDistributionChart();
        genderChart.setPreferredSize(new Dimension(320, 230));
        genderChart.setBackground(Color.WHITE);

        JPanel chartCard = new JPanel(new BorderLayout(0, 10));
        chartCard.setBackground(Color.WHITE);
        chartCard.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ModernUI.BORDER_COLOR),
                "Overall Turnout Composition Chart"
            )
        );
        chartCard.add(genderChart, BorderLayout.CENTER);

        overallTurnoutLabel = new JLabel("Overall turnout: 0.00%");
        overallTurnoutLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        overallTurnoutLabel.setForeground(ModernUI.TEXT_COLOR_DARK);
        topConstituencyLabel = new JLabel("Top constituency: N/A");
        topConstituencyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topConstituencyLabel.setForeground(ModernUI.TEXT_COLOR_DARK);

        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        summaryPanel.setOpaque(false);
        summaryPanel.add(overallTurnoutLabel);
        summaryPanel.add(topConstituencyLabel);
        chartCard.add(summaryPanel, BorderLayout.SOUTH);

        JPanel chartsContainer = new JPanel(new BorderLayout(12, 0));
        chartsContainer.setOpaque(false);
        chartsContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        chartsContainer.add(chartCard, BorderLayout.WEST);
        chartsContainer.add(graphScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(320);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setTopComponent(tableScrollPane);
        splitPane.setBottomComponent(chartsContainer);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        centerContainer.add(splitPane, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton updateBtn = new ModernUI.ModernButton("Update Hourly Turnout");
        updateBtn.addActionListener(e -> {
            String boothIdInput = JOptionPane.showInputDialog(this, "Enter Booth ID to update:");
            if (boothIdInput != null && !boothIdInput.isEmpty()) {
                try {
                    int boothId = Integer.parseInt(boothIdInput);
                    MainDashboard.showView(new HourlyTurnoutUpdaterScreen(boothId));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid Booth ID");
                }
            }
        });

        ModernUI.ModernButton genderBtn = new ModernUI.ModernButton("Gender Analytics");
        genderBtn.addActionListener(e -> MainDashboard.showView(new GenderAnalyticsScreen()));

        ModernUI.ModernButton compareBtn = new ModernUI.ModernButton("Constituency Comparison");
        compareBtn.addActionListener(e -> MainDashboard.showView(new ConstituencyComparisonScreen()));

        ModernUI.ModernButton historyBtn = new ModernUI.ModernButton("Historical Comparison");
        historyBtn.addActionListener(e -> MainDashboard.showView(new HistoricalComparisonScreen()));
        
        ModernUI.ModernButton reportBtn = new ModernUI.ModernButton("Generate Report");
        reportBtn.setBackground(ModernUI.ACCENT_COLOR);
        reportBtn.addActionListener(e -> MainDashboard.showView(new TurnoutReportGeneratorScreen()));

        actionPanel.add(updateBtn);
        actionPanel.add(genderBtn);
        actionPanel.add(compareBtn);
        actionPanel.add(historyBtn);
        actionPanel.add(reportBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshTurnoutStats();
    }

    private void refreshTurnoutStats() {
        tableModel.setRowCount(0);
        turnoutGraphPanel.removeAll();
        boolean dataFound = false;
        int totalMale = 0;
        int totalFemale = 0;
        int totalThird = 0;
        int totalVotedAll = 0;
        int totalRegisteredAll = 0;
        List<ConstituencyTurnout> constituencyTurnouts = new ArrayList<>();

        try (Connection conn = DatabaseHelper.getConnection()) {
            // Use only the latest reported hour per booth so turnout reflects current snapshot.
            String query = "SELECT c.constituency_id, c.constituency_name, c.total_voters, " +
                           "COALESCE(SUM(ht_latest.male_votes), 0) as male, " +
                           "COALESCE(SUM(ht_latest.female_votes), 0) as female, " +
                           "COALESCE(SUM(ht_latest.third_gender_votes), 0) as third " +
                           "FROM Constituencies c " +
                           "LEFT JOIN Booths b ON c.constituency_id = b.constituency_id " +
                           "LEFT JOIN (" +
                           "    SELECT ht1.booth_id, ht1.male_votes, ht1.female_votes, ht1.third_gender_votes " +
                           "    FROM HourlyTurnout ht1 " +
                           "    INNER JOIN (" +
                           "        SELECT booth_id, MAX(report_hour) as max_hour " +
                           "        FROM HourlyTurnout " +
                           "        GROUP BY booth_id" +
                           "    ) latest ON ht1.booth_id = latest.booth_id AND ht1.report_hour = latest.max_hour" +
                           ") ht_latest ON b.booth_id = ht_latest.booth_id " +
                           "GROUP BY c.constituency_id, c.constituency_name, c.total_voters";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("constituency_id");
                String name = rs.getString("constituency_name");
                int male = rs.getInt("male");
                int female = rs.getInt("female");
                int third = rs.getInt("third");
                int totalVoted = male + female + third;
                int totalRegistered = rs.getInt("total_voters");
                double percentage = engine.calculateTurnoutPercentage(totalVoted, totalRegistered);
                
                tableModel.addRow(new Object[]{id, name, male, female, third, totalVoted, String.format("%.2f%%", percentage)});
                constituencyTurnouts.add(new ConstituencyTurnout(name, totalVoted, totalRegistered, percentage));
                totalMale += male;
                totalFemale += female;
                totalThird += third;
                totalVotedAll += totalVoted;
                totalRegisteredAll += totalRegistered;
                dataFound = true;
            }
        } catch (Exception ex) {
            System.err.println("Turnout data fetch error: " + ex.getMessage());
        }

        if (!dataFound) {
            tableModel.addRow(new Object[]{0, "No data available", 0, 0, 0, 0, "0%"});
            overallTurnoutLabel.setText("Overall turnout: 0.00%");
            topConstituencyLabel.setText("Top constituency: N/A");
            genderChart.setData(0, 0, 0);
            addTurnoutBar("No constituency data", 0, 0, 0);
        } else {
            constituencyTurnouts.sort(Comparator.comparingDouble(ConstituencyTurnout::percentage).reversed());
            for (ConstituencyTurnout turnout : constituencyTurnouts) {
                addTurnoutBar(turnout.name(), turnout.percentage(), turnout.totalVoted(), turnout.totalRegistered());
            }

            double overallPercent = engine.calculateTurnoutPercentage(totalVotedAll, totalRegisteredAll);
            overallTurnoutLabel.setText(String.format("Overall turnout: %.2f%% (%s / %s)",
                overallPercent, formatNumber(totalVotedAll), formatNumber(totalRegisteredAll)));
            ConstituencyTurnout top = constituencyTurnouts.get(0);
            topConstituencyLabel.setText(String.format("Top constituency: %s (%.2f%%)",
                top.name(), top.percentage()));
            genderChart.setData(totalMale, totalFemale, totalThird);
        }

        turnoutGraphPanel.revalidate();
        turnoutGraphPanel.repaint();
    }

    private void addTurnoutBar(String constituency, double percentage, int totalVoted, int totalRegistered) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setOpaque(false);
        rowPanel.setMaximumSize(new Dimension(900, 44));

        JLabel nameLabel = new JLabel(constituency);
        nameLabel.setPreferredSize(new Dimension(210, 30));
        nameLabel.setFont(ModernUI.MAIN_FONT);

        JProgressBar turnoutBar = new JProgressBar(0, 100);
        turnoutBar.setValue((int) Math.round(percentage));
        turnoutBar.setStringPainted(true);
        turnoutBar.setString(String.format("%.2f%% (%s / %s)",
            percentage, formatNumber(totalVoted), formatNumber(totalRegistered)));
        turnoutBar.setForeground(getTurnoutColor(percentage));
        turnoutBar.setBackground(new Color(230, 233, 238));

        rowPanel.add(nameLabel, BorderLayout.WEST);
        rowPanel.add(turnoutBar, BorderLayout.CENTER);
        turnoutGraphPanel.add(rowPanel);
        turnoutGraphPanel.add(Box.createVerticalStrut(6));
    }

    private Color getTurnoutColor(double percentage) {
        if (percentage >= 75) return new Color(22, 163, 74);
        if (percentage >= 60) return new Color(245, 158, 11);
        return new Color(239, 68, 68);
    }

    private String formatNumber(int value) {
        return String.format("%,d", value);
    }

    private record ConstituencyTurnout(String name, int totalVoted, int totalRegistered, double percentage) {}

    private static class GenderDistributionChart extends JPanel {
        private int maleVotes;
        private int femaleVotes;
        private int thirdGenderVotes;

        public void setData(int maleVotes, int femaleVotes, int thirdGenderVotes) {
            this.maleVotes = maleVotes;
            this.femaleVotes = femaleVotes;
            this.thirdGenderVotes = thirdGenderVotes;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = maleVotes + femaleVotes + thirdGenderVotes;
            int width = getWidth();
            int height = getHeight();

            if (total <= 0) {
                g2.setColor(new Color(107, 114, 128));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString("No turnout data yet", 20, height / 2);
                g2.dispose();
                return;
            }

            int chartSize = Math.min(width - 24, height - 44);
            int x = 12;
            int y = 10;

            Color maleColor = new Color(59, 130, 246);
            Color femaleColor = new Color(236, 72, 153);
            Color thirdColor = new Color(16, 185, 129);

            int maleAngle = (int) Math.round((maleVotes * 360.0) / total);
            int femaleAngle = (int) Math.round((femaleVotes * 360.0) / total);
            int thirdAngle = 360 - maleAngle - femaleAngle;

            g2.setColor(maleColor);
            g2.fillArc(x, y, chartSize, chartSize, 90, -maleAngle);
            g2.setColor(femaleColor);
            g2.fillArc(x, y, chartSize, chartSize, 90 - maleAngle, -femaleAngle);
            g2.setColor(thirdColor);
            g2.fillArc(x, y, chartSize, chartSize, 90 - maleAngle - femaleAngle, -thirdAngle);

            int legendX = x + chartSize + 14;
            int legendY = y + 24;
            drawLegend(g2, legendX, legendY, maleColor, "Male", maleVotes, total);
            drawLegend(g2, legendX, legendY + 26, femaleColor, "Female", femaleVotes, total);
            drawLegend(g2, legendX, legendY + 52, thirdColor, "TG", thirdGenderVotes, total);

            g2.dispose();
        }

        private void drawLegend(Graphics2D g2, int x, int y, Color color, String label, int value, int total) {
            g2.setColor(color);
            g2.fillRoundRect(x, y - 10, 12, 12, 4, 4);
            g2.setColor(new Color(31, 41, 55));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            double percent = total == 0 ? 0 : (value * 100.0 / total);
            g2.drawString(String.format("%s: %s (%.1f%%)", label, String.format("%,d", value), percent), x + 18, y);
        }
    }
}
