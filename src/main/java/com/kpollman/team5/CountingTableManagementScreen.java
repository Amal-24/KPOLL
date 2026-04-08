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
import java.util.ArrayList;
import java.util.List;

/**
 * Counting Table Management Screen to add, edit, and assign tables.
 */
public class CountingTableManagementScreen extends JPanel {
    private JTable tablesTable;
    private DefaultTableModel tableModel;

    public CountingTableManagementScreen() {
        setLayout(new BorderLayout());
        setBackground(ModernUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Counting Table Management");
        title.setFont(ModernUI.TITLE_FONT);
        title.setForeground(ModernUI.TEXT_COLOR_DARK);
        headerPanel.add(title, BorderLayout.WEST);

        ModernUI.ModernButton backBtn = new ModernUI.ModernButton("Back to Dashboard");
        backBtn.addActionListener(e -> MainDashboard.showView(new CountingCenterDashboard()));
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"Table ID", "Center", "Constituency", "Supervisor", "Assistant"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablesTable = new JTable(tableModel);
        tablesTable.setFont(ModernUI.MAIN_FONT);
        tablesTable.setRowHeight(40);

        JScrollPane scrollPane = new JScrollPane(tablesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);

        ModernUI.ModernButton addTableBtn = new ModernUI.ModernButton("Add Table");
        addTableBtn.setBackground(ModernUI.PRIMARY_COLOR);
        addTableBtn.addActionListener(e -> addNewTable());

        ModernUI.ModernButton editTableBtn = new ModernUI.ModernButton("Edit Table");
        editTableBtn.addActionListener(e -> editSelectedTable());

        ModernUI.ModernButton deleteTableBtn = new ModernUI.ModernButton("Delete Table");
        deleteTableBtn.setBackground(new Color(239, 68, 68));
        deleteTableBtn.addActionListener(e -> deleteSelectedTable());

        ModernUI.ModernButton assignStaffBtn = new ModernUI.ModernButton("Assign Staff");
        assignStaffBtn.addActionListener(e -> MainDashboard.showView(new CountingStaffAssignmentScreen()));

        actionPanel.add(addTableBtn);
        actionPanel.add(editTableBtn);
        actionPanel.add(deleteTableBtn);
        actionPanel.add(assignStaffBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshTables();
    }

    private void addNewTable() {
        List<Option> centers = loadCenters();
        List<Option> constituencies = loadConstituencies();
        if (centers.isEmpty() || constituencies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please configure centers and constituencies first.");
            return;
        }

        JComboBox<Option> centerBox = new JComboBox<>(centers.toArray(new Option[0]));
        JComboBox<Option> constituencyBox = new JComboBox<>(constituencies.toArray(new Option[0]));
        JTextField supervisorField = new JTextField();
        JTextField assistantField = new JTextField();

        Object[] form = {
            "Center", centerBox,
            "Constituency", constituencyBox,
            "Supervisor", supervisorField,
            "Assistant", assistantField
        };

        int choice = JOptionPane.showConfirmDialog(this, form, "Add Counting Table", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        Option selectedCenter = (Option) centerBox.getSelectedItem();
        Option selectedConstituency = (Option) constituencyBox.getSelectedItem();
        if (selectedCenter == null || selectedConstituency == null) return;

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "INSERT INTO CountingTables (center_id, constituency_id, supervisor_name, assistant_name) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, selectedCenter.id());
            pstmt.setInt(2, selectedConstituency.id());
            pstmt.setString(3, emptyToNull(supervisorField.getText()));
            pstmt.setString(4, emptyToNull(assistantField.getText()));
            pstmt.executeUpdate();
            refreshTables();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding table: " + ex.getMessage());
        }
    }

    private void editSelectedTable() {
        int row = tablesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a table to edit.");
            return;
        }

        int tableId = (int) tableModel.getValueAt(row, 0);
        List<Option> centers = loadCenters();
        List<Option> constituencies = loadConstituencies();
        if (centers.isEmpty() || constituencies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Centers or constituencies are missing.");
            return;
        }

        JComboBox<Option> centerBox = new JComboBox<>(centers.toArray(new Option[0]));
        JComboBox<Option> constituencyBox = new JComboBox<>(constituencies.toArray(new Option[0]));
        JTextField supervisorField = new JTextField(valueOrEmpty(tableModel.getValueAt(row, 3)));
        JTextField assistantField = new JTextField(valueOrEmpty(tableModel.getValueAt(row, 4)));

        loadCurrentTableSelections(tableId, centerBox, constituencyBox);

        Object[] form = {
            "Center", centerBox,
            "Constituency", constituencyBox,
            "Supervisor", supervisorField,
            "Assistant", assistantField
        };

        int choice = JOptionPane.showConfirmDialog(this, form, "Edit Counting Table", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        Option selectedCenter = (Option) centerBox.getSelectedItem();
        Option selectedConstituency = (Option) constituencyBox.getSelectedItem();
        if (selectedCenter == null || selectedConstituency == null) return;

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "UPDATE CountingTables SET center_id = ?, constituency_id = ?, supervisor_name = ?, assistant_name = ? WHERE table_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, selectedCenter.id());
            pstmt.setInt(2, selectedConstituency.id());
            pstmt.setString(3, emptyToNull(supervisorField.getText()));
            pstmt.setString(4, emptyToNull(assistantField.getText()));
            pstmt.setInt(5, tableId);
            pstmt.executeUpdate();
            refreshTables();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating table: " + ex.getMessage());
        }
    }

    private void deleteSelectedTable() {
        int row = tablesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a table to delete.");
            return;
        }

        int tableId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete table ID " + tableId + "? Linked round entries will fail if foreign keys exist.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "DELETE FROM CountingTables WHERE table_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, tableId);
            pstmt.executeUpdate();
            refreshTables();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting table: " + ex.getMessage());
        }
    }

    private void refreshTables() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT ct.table_id, cc.center_name, c.constituency_name, " +
                           "COALESCE(ct.supervisor_name, '-') as supervisor_name, " +
                           "COALESCE(ct.assistant_name, '-') as assistant_name " +
                           "FROM CountingTables ct " +
                           "JOIN CountingCenters cc ON ct.center_id = cc.center_id " +
                           "JOIN Constituencies c ON ct.constituency_id = c.constituency_id " +
                           "ORDER BY ct.table_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("table_id"),
                    rs.getString("center_name"),
                    rs.getString("constituency_name"),
                    rs.getString("supervisor_name"),
                    rs.getString("assistant_name")
                });
            }
        } catch (Exception ex) {
            System.err.println("Counting tables fetch error: " + ex.getMessage());
        }
    }

    private List<Option> loadCenters() {
        List<Option> centers = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT center_id, center_name FROM CountingCenters ORDER BY center_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                centers.add(new Option(rs.getInt("center_id"), rs.getString("center_name")));
            }
        } catch (Exception ex) {
            System.err.println("Center load error: " + ex.getMessage());
        }
        return centers;
    }

    private List<Option> loadConstituencies() {
        List<Option> constituencies = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT constituency_id, constituency_name FROM Constituencies ORDER BY constituency_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                constituencies.add(new Option(rs.getInt("constituency_id"), rs.getString("constituency_name")));
            }
        } catch (Exception ex) {
            System.err.println("Constituency load error: " + ex.getMessage());
        }
        return constituencies;
    }

    private void loadCurrentTableSelections(int tableId, JComboBox<Option> centerBox, JComboBox<Option> constituencyBox) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT center_id, constituency_id FROM CountingTables WHERE table_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                setSelectedById(centerBox, rs.getInt("center_id"));
                setSelectedById(constituencyBox, rs.getInt("constituency_id"));
            }
        } catch (Exception ex) {
            System.err.println("Current table load error: " + ex.getMessage());
        }
    }

    private void setSelectedById(JComboBox<Option> comboBox, int id) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Option item = comboBox.getItemAt(i);
            if (item.id() == id) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private String emptyToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOrEmpty(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value);
        return "-".equals(text) ? "" : text;
    }

    private record Option(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}
