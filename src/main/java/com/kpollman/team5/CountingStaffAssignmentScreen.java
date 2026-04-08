package com.kpollman.team5;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.ui.MainDashboard;
import com.kpollman.ui.ModernUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

        String[] columns = {"Table ID", "Staff Name", "Role", "Assigned Table", "Assigned Center", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        staffTable = new JTable(tableModel);
        staffTable.setFont(ModernUI.MAIN_FONT);
        staffTable.setRowHeight(40);

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);

        ModernUI.ModernButton assignBtn = new ModernUI.ModernButton("Assign Staff");
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
        JComboBox<String> tableBox = new JComboBox<>();
        loadTableOptions(tableBox);
        if (tableBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No counting tables found. Please create tables first.");
            return;
        }

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Supervisor", "Assistant"});
        JTextField nameField = new JTextField();

        Object[] form = {
            "Table", tableBox,
            "Role", roleBox,
            "Staff Name", nameField
        };

        int choice = JOptionPane.showConfirmDialog(this, form, "Assign Staff", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        String selectedTable = (String) tableBox.getSelectedItem();
        String selectedRole = (String) roleBox.getSelectedItem();
        String staffName = nameField.getText() == null ? "" : nameField.getText().trim();
        if (selectedTable == null || selectedRole == null || staffName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide table, role and name.");
            return;
        }

        int tableId = Integer.parseInt(selectedTable.split(" - ")[0]);
        updateStaffAssignment(tableId, selectedRole, staffName);
    }

    private void editAssignment() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a staff assignment to edit.");
            return;
        }

        int tableId = (int) tableModel.getValueAt(row, 0);
        String role = String.valueOf(tableModel.getValueAt(row, 2));
        JTextField nameField = new JTextField(String.valueOf(tableModel.getValueAt(row, 1)));

        Object[] form = {
            "Role", role,
            "New Name", nameField
        };
        int choice = JOptionPane.showConfirmDialog(this, form, "Edit Assignment", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        String staffName = nameField.getText() == null ? "" : nameField.getText().trim();
        if (staffName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.");
            return;
        }

        updateStaffAssignment(tableId, role, staffName);
    }

    private void updateStaffAssignment(int tableId, String role, String name) {
        String column = "Supervisor".equalsIgnoreCase(role) ? "supervisor_name" : "assistant_name";
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "UPDATE CountingTables SET " + column + " = ? WHERE table_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setInt(2, tableId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Assignment saved.");
                refreshStaffList();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save assignment: " + ex.getMessage());
        }
    }

    private void loadTableOptions(JComboBox<String> tableBox) {
        tableBox.removeAllItems();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT ct.table_id, c.constituency_name, cc.center_name " +
                           "FROM CountingTables ct " +
                           "JOIN Constituencies c ON ct.constituency_id = c.constituency_id " +
                           "JOIN CountingCenters cc ON ct.center_id = cc.center_id " +
                           "ORDER BY ct.table_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableBox.addItem(rs.getInt("table_id") + " - " + rs.getString("constituency_name") + " / " + rs.getString("center_name"));
            }
        } catch (Exception ex) {
            System.err.println("Load table options error: " + ex.getMessage());
        }
    }

    private void refreshStaffList() {
        tableModel.setRowCount(0);
        boolean found = false;
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT ct.table_id, c.constituency_name, cc.center_name, ct.supervisor_name, ct.assistant_name " +
                           "FROM CountingTables ct " +
                           "JOIN Constituencies c ON ct.constituency_id = c.constituency_id " +
                           "JOIN CountingCenters cc ON ct.center_id = cc.center_id " +
                           "ORDER BY ct.table_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                String assignedTable = "Table " + tableId + " - " + rs.getString("constituency_name");
                String assignedCenter = rs.getString("center_name");

                String supervisor = rs.getString("supervisor_name");
                if (supervisor != null && !supervisor.trim().isEmpty()) {
                    tableModel.addRow(new Object[]{tableId, supervisor, "Supervisor", assignedTable, assignedCenter, "Assigned"});
                    found = true;
                }

                String assistant = rs.getString("assistant_name");
                if (assistant != null && !assistant.trim().isEmpty()) {
                    tableModel.addRow(new Object[]{tableId, assistant, "Assistant", assignedTable, assignedCenter, "Assigned"});
                    found = true;
                }
            }
        } catch (Exception ex) {
            System.err.println("Staff assignment fetch error: " + ex.getMessage());
        }

        if (!found) {
            tableModel.addRow(new Object[]{0, "No assignments found", "-", "-", "-", "N/A"});
        }
    }
}
