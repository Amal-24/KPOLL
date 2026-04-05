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
    private boolean isLoggedIn = false;
    private int currentBoothId;
    private String currentBoothName;
    private int currentConstituencyId;

    public MainDashboard() {
        instance = this;
        setTitle("K-PollMan 2026 - Integrated Election Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupDashboard();
    }

    public static void showView(JPanel panel) {
        if (instance != null) {
            instance.contentArea.add(panel, "CurrentView");
            instance.cardLayout.show(instance.contentArea, "CurrentView");
            instance.contentArea.revalidate();
            instance.contentArea.repaint();
        }
    }

    private void setupDashboard() {
        setLayout(new BorderLayout());
        
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
        addNavItem("Home", e -> showModule("Home", new HomeView()));
        
        addNavItem("Dashboard", e -> {
            if (isLoggedIn) {
                showModule("Dashboard", new BoothDashboard(currentBoothId, currentBoothName, currentConstituencyId));
            } else {
                showModule("Login", new BoothLoginScreen((id, name, constId) -> {
                    isLoggedIn = true;
                    currentBoothId = id;
                    currentBoothName = name;
                    currentConstituencyId = constId;
                    com.kpollman.team3.Session.login(id, "OFFICIAL");
                    com.kpollman.team3.Session.boothId = id;
                    com.kpollman.team3.Session.constituencyId = constId;
                    showModule("Dashboard", new BoothDashboard(id, name, constId));
                }));
            }
        });

        addNavItem("Queue Mgmt", e -> {
            if (checkLogin()) showModule("Team2", new QueueStatusDashboard());
        });
        
        addNavItem("Issue Reporting", e -> {
            if (checkLogin()) showModule("Team3", new LoginScreen(() -> {
                showModule("Team3Dashboard", new IssueTrackingDashboard());
            }));
        });
        
        addNavItem("Turnout Analytics", e -> {
            if (checkLogin()) showModule("Team4", new TurnoutDashboard());
        });
        
        addNavItem("Counting Center", e -> {
            if (checkLogin()) showModule("Team5", new CountingCenterDashboard());
        });
        
        addNavItem("Result Processing", e -> {
            if (checkLogin()) showModule("Team6", new ResultAggregationScreen());
        });
        
        addNavItem("Live Results", e -> {
            if (checkLogin()) showModule("Team7", new LiveResultsDashboard());
        });

        sidebar.add(Box.createVerticalGlue());
        addNavItem("Logout", e -> {
            isLoggedIn = false;
            showModule("Home", new HomeView());
        });

        add(sidebar, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);

        // Initial view
        showModule("Home", new HomeView());
        setActiveNavItem("Home");
    }

    private boolean checkLogin() {
        if (!isLoggedIn) {
            JOptionPane.showMessageDialog(this, "Please login via Dashboard first", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void setActiveNavItem(String text) {
        for (Component c : sidebar.getComponents()) {
            if (c instanceof JPanel) {
                JPanel navItem = (JPanel) c;
                if (navItem.getComponentCount() > 0 && navItem.getComponent(0) instanceof JLabel) {
                    JLabel label = (JLabel) navItem.getComponent(0);
                    if (label.getText().equals(text)) {
                        if (activeNavItem != null) {
                            activeNavItem.setBackground(null);
                            activeNavItem.setOpaque(false);
                            ((JLabel)activeNavItem.getComponent(0)).setForeground(new Color(203, 213, 225));
                        }
                        activeNavItem = navItem;
                        navItem.setBackground(ModernUI.ACCENT_COLOR);
                        navItem.setOpaque(true);
                        label.setForeground(Color.WHITE);
                        navItem.repaint();
                        break;
                    }
                }
            }
        }
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
