package com.kpollman.team7;

import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;

/**
 * Result Export Screen for exporting results to PDF, Excel, JSON formats.
 */
public class ResultExportScreen extends JFrame {
    private JComboBox<String> formatDropdown;
    private ModernUI.ModernButton exportButton;

    public ResultExportScreen() {
        setTitle("K-PollMan 2026 - Export Results Portal");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Official Result Exporting Tool", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; add(new JLabel("Export Format:"), gbc);
        formatDropdown = new JComboBox<>(new String[]{"PDF", "Excel (XLSX)", "JSON", "CSV"});
        gbc.gridx = 1; add(formatDropdown, gbc);

        exportButton = new ModernUI.ModernButton("Generate Export File");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(exportButton, gbc);

        exportButton.addActionListener(e -> {
            String format = (String) formatDropdown.getSelectedItem();
            JOptionPane.showMessageDialog(this, "Successfully generated election results export in " + format + " format!", "Export Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        });
    }
}
