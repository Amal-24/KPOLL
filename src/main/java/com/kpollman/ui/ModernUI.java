package com.kpollman.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ModernUI {
    public static final Color SIDEBAR_COLOR = new Color(30, 41, 59); // Slate 800
    public static final Color BACKGROUND_COLOR = new Color(241, 245, 249); // Slate 100
    public static final Color ACCENT_COLOR = new Color(51, 65, 85); // Slate 700
    public static final Color PRIMARY_COLOR = new Color(15, 23, 42); // Slate 900
    public static final Color TEXT_COLOR_DARK = new Color(15, 23, 42); // Slate 900
    public static final Color TEXT_COLOR_LIGHT = Color.WHITE;
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200

    public static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);

    public static void setupUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        // JOptionPane styling
        UIManager.put("OptionPane.background", CARD_BACKGROUND);
        UIManager.put("Panel.background", CARD_BACKGROUND);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR_DARK);
        UIManager.put("OptionPane.messageFont", MAIN_FONT);
        UIManager.put("Button.font", MAIN_FONT);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.foreground", TEXT_COLOR_DARK);
        UIManager.put("OptionPane.border", BorderFactory.createLineBorder(BORDER_COLOR, 1));
    }

    public static class RoundedPanel extends JPanel {
        private int cornerRadius;

        public RoundedPanel(int radius, Color backgroundColor) {
            this.cornerRadius = radius;
            setBackground(backgroundColor);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            g2.dispose();
        }
    }

    public static class ModernButton extends JButton {
        public ModernButton(String text) {
            super(text);
            setFont(MAIN_FONT);
            setForeground(TEXT_COLOR_LIGHT);
            setBackground(ACCENT_COLOR);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);
            g2.dispose();
        }

        @Override
        public void setContentAreaFilled(boolean b) {}
    }

    public static class ModernTextField extends JTextField {
        public ModernTextField(String placeholder) {
            super(placeholder);
            setFont(MAIN_FONT);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
        }

        public static class RoundedBorder extends javax.swing.border.AbstractBorder {
            private final int radius;
            private final Color color;

            public RoundedBorder(int radius, Color color) {
                this.radius = radius;
                this.color = color;
            }

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
                g2.dispose();
            }
        }
    }
}
