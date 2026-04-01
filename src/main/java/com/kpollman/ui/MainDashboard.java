package com.kpollman.ui;

import com.kpollman.team1.BoothLoginScreen;
import com.kpollman.team1.BoothDashboard;
import com.kpollman.team2.QueueStatusDashboard;
import com.kpollman.team3.IssueTrackingDashboard;
import com.kpollman.team3.LoginScreen;
import com.kpollman.team4.TurnoutDashboard;
import com.kpollman.team5.CountingCenterDashboard;
import com.kpollman.team6.ResultAggregationScreen;
import com.kpollman.team7.LiveResultsDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainDashboard extends JFrame {
    private static MainDashboard instance;
    private JPanel sidebar;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JPanel activeNavItem;
    private JPanel mainContainer;
    private CardLayout mainLayout;

    public MainDashboard() {
        instance = this;
        setTitle("K-PollMan 2026 - Integrated Election Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainLayout = new CardLayout();
        mainContainer = new JPanel(mainLayout);

        // Login Screen
        BoothLoginScreen loginScreen = new BoothLoginScreen((boothId, boothName, constituencyId) -> {
            setupDashboard(boothId, boothName, constituencyId);
            mainLayout.show(mainContainer, "Dashboard");
        });
        mainContainer.add(loginScreen, "Login");

        add(mainContainer);
        mainLayout.show(mainContainer, "Login");
    }

    public static void showView(JPanel panel) {
        if (instance != null) {
            instance.contentArea.add(panel, "CurrentView");
            instance.cardLayout.show(instance.contentArea, "CurrentView");
            instance.contentArea.revalidate();
            instance.contentArea.repaint();
        }
    }

    private void setupDashboard(int boothId, String boothName, int constituencyId) {
        JPanel dashboardWrapper = new JPanel(new BorderLayout());
        
        // Sidebar
        sidebar = new JPanel();
        sidebar.setBackground(ModernUI.SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(260, 800));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 15, 30, 15));

        JLabel appTitle = new JLabel("K-PollMan 2026");
        appTitle.setFont(ModernUI.HEADER_FONT);
        appTitle.setForeground(ModernUI.TEXT_COLOR_LIGHT);
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(appTitle);
        sidebar.add(Box.createVerticalStrut(40));

        // Content Area
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(ModernUI.BACKGROUND_COLOR);

        // Navigation Items
        addNavItem("Dashboard", e -> showModule("Dashboard", new BoothDashboard(boothId, boothName, constituencyId)));
        addNavItem("Queue Mgmt", e -> showModule("Team2", new QueueStatusDashboard()));
        addNavItem("Issue Reporting", e -> showModule("Team3", new LoginScreen(() -> {
            showModule("Team3Dashboard", new IssueTrackingDashboard());
        })));
        addNavItem("Turnout Analytics", e -> showModule("Team4", new TurnoutDashboard()));
        addNavItem("Counting Center", e -> showModule("Team5", new CountingCenterDashboard()));
        addNavItem("Result Processing", e -> showModule("Team6", new ResultAggregationScreen()));
        addNavItem("Live Results", e -> showModule("Team7", new LiveResultsDashboard()));

        sidebar.add(Box.createVerticalGlue());
        addNavItem("Logout", e -> {
            mainLayout.show(mainContainer, "Login");
        });

        dashboardWrapper.add(sidebar, BorderLayout.WEST);
        dashboardWrapper.add(contentArea, BorderLayout.CENTER);

        mainContainer.add(dashboardWrapper, "Dashboard");
        
        // Initial view
        showModule("Dashboard", new BoothDashboard(boothId, boothName, constituencyId));
    }

    private void addNavItem(String text, ActionListener action) {
        JPanel navItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        navItem.setOpaque(false);
        navItem.setMaximumSize(new Dimension(230, 50));
        navItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setFont(ModernUI.MAIN_FONT);
        label.setForeground(new Color(203, 213, 225)); // Slate 300
        navItem.add(label);

        navItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (activeNavItem != null) {
                    activeNavItem.setBackground(null);
                    activeNavItem.setOpaque(false);
                    ((JLabel)activeNavItem.getComponent(0)).setForeground(new Color(203, 213, 225));
                }
                activeNavItem = navItem;
                navItem.setBackground(ModernUI.ACCENT_COLOR);
                navItem.setOpaque(true);
                ((JLabel)navItem.getComponent(0)).setForeground(Color.WHITE);
                action.actionPerformed(null);
                navItem.repaint();
            }
        });

        sidebar.add(navItem);
        sidebar.add(Box.createVerticalStrut(8));
    }

    private void showModule(String name, JComponent component) {
        contentArea.add(component, name);
        cardLayout.show(contentArea, name);
        contentArea.revalidate();
        contentArea.repaint();
    }

    public static void main(String[] args) {
        // Set Look and Feel to System
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}
