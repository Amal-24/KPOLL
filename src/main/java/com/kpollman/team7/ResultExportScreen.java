package com.kpollman.team7;

import com.kpollman.ui.ModernUI;
import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Enhanced Result Export Screen for exporting election results to multiple formats.
 */
public class ResultExportScreen extends JFrame {
    private JComboBox<String> formatDropdown;
    private JCheckBox includeConstituencyTrends;
    private JCheckBox includePartyTally;
    private JCheckBox includeMarginAnalysis;
    private ModernUI.ModernButton exportButton;

    public ResultExportScreen() {
        setTitle("K-PollMan 2026 - Export Results Portal");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        header.setBackground(ModernUI.ACCENT_COLOR);
        JLabel titleLabel = new JLabel("Official Result Exporting Tool");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);
        add(header, BorderLayout.NORTH);

        // Content Panel
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        content.add(new JLabel("Select Export Format:"), gbc);
        formatDropdown = new JComboBox<>(new String[]{"PDF (Official Report)", "Excel (.xlsx)", "JSON (API Data)", "CSV (Raw Data)"});
        gbc.gridx = 1;
        content.add(formatDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        content.add(new JLabel("Include in Export:"), gbc);

        gbc.gridy = 2; includeConstituencyTrends = new JCheckBox("Constituency-wise Trends", true);
        content.add(includeConstituencyTrends, gbc);

        gbc.gridy = 3; includePartyTally = new JCheckBox("Party-wise Seat Tally", true);
        content.add(includePartyTally, gbc);

        gbc.gridy = 4; includeMarginAnalysis = new JCheckBox("Winning Margin Analysis", true);
        content.add(includeMarginAnalysis, gbc);

        add(content, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        footer.setOpaque(false);
        
        exportButton = new ModernUI.ModernButton("Generate and Download");
        exportButton.setBackground(new Color(34, 197, 94));
        exportButton.addActionListener(e -> performExport());
        
        ModernUI.ModernButton cancelBtn = new ModernUI.ModernButton("Cancel");
        cancelBtn.setBackground(Color.LIGHT_GRAY);
        cancelBtn.addActionListener(e -> this.dispose());

        footer.add(cancelBtn);
        footer.add(exportButton);
        add(footer, BorderLayout.SOUTH);
    }

    private void performExport() {
        String format = (String) formatDropdown.getSelectedItem();
        
        // Mocking file selection and export process
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Export File");
        fileChooser.setSelectedFile(new File("Election_Results_2026." + format.toLowerCase().split(" ")[0]));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Simulating a delay for processing
            Timer timer = new Timer(1500, e -> {
                JOptionPane.showMessageDialog(this, 
                    "Export successful!\nFile saved: " + fileToSave.getAbsolutePath(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            });
            timer.setRepeats(false);
            timer.start();
            
            exportButton.setText("Exporting...");
            exportButton.setEnabled(false);
        }
    }
}
