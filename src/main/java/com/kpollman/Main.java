package com.kpollman;

import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Setup modern UI styles
        ModernUI.setupUI();
        
        // Start the integrated application from the new Modern Dashboard
        SwingUtilities.invokeLater(() -> {
            new MainDashboard().setVisible(true);
        });
    }
}
