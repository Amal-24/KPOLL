package com.kpollman.team5;

import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Counting Staff Assignment Screen to assign supervisors and counting assistants.
 */
public class CountingStaffAssignmentScreen extends JPanel {
    private JTable staffTable;
    private DefaultTableModel tableModel;

    public CountingStaffAssignmentScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Counting Staff Assignment Portal");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        String[] columns = {"Staff Name", "Role", "Assigned Table", "Assigned Center", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        staffTable = new JTable(tableModel);
        staffTable.setFont(ModernUI.MAIN_FONT);
        staffTable.setRowHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);
        
        ModernUI.ModernButton assignBtn = new ModernUI.ModernButton("Assign New Staff");
        assignBtn.setBackground(ModernUI.PRIMARY_COLOR);
        assignBtn.addActionListener(e -> assignNewStaff());
        
        ModernUI.ModernButton editBtn = new ModernUI.ModernButton("Edit Assignment");
        editBtn.addActionListener(e -> editAssignment());

        actionPanel.add(assignBtn);
        actionPanel.add(editBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshStaffList();
    }

    private void assignNewStaff() {
        JOptionPane.showMessageDialog(this, "Staff Assignment Dialog - Logic to be implemented.");
    }

    private void editAssignment() {
        if (staffTable.getSelectedRow() != -1) {
            JOptionPane.showMessageDialog(this, "Editing Assignment for: " + tableModel.getValueAt(staffTable.getSelectedRow(), 0));
        } else {
            JOptionPane.showMessageDialog(this, "Please select a staff member to edit.");
        }
    }

    private void refreshStaffList() {
        tableModel.setRowCount(0);
        // Note: Implement database fetch for staff assignments when database schema is updated
        // For now, showing placeholder
    }
}
