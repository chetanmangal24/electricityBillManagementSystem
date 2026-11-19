package com.ebms.ui;

import com.ebms.dao.ConsumerDAO;
import com.ebms.model.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for viewing and searching consumers.
 */
public class ViewConsumersPanel extends JPanel {

    private final ConsumerDAO consumerDAO;
    private JTable consumerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ViewConsumersPanel() {
        this.consumerDAO = new ConsumerDAO();
        initComponents();
        loadConsumers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("View Consumers", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadConsumers());
        searchPanel.add(refreshButton);

        add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Meter Number", "Phone", "Connection Type", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        consumerTable = new JTable(tableModel);
        consumerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(consumerTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(e -> viewDetails());
        buttonPanel.add(viewDetailsButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> handleDelete());
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadConsumers() {
        try {
            List<Consumer> consumers = consumerDAO.findAll();
            tableModel.setRowCount(0);

            for (Consumer consumer : consumers) {
                tableModel.addRow(new Object[]{
                    consumer.getId(),
                    consumer.getName(),
                    consumer.getMeterNumber(),
                    consumer.getPhone(),
                    consumer.getConnectionType(),
                    consumer.getAddress()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading consumers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadConsumers();
            return;
        }

        try {
            List<Consumer> consumers = consumerDAO.search(keyword);
            tableModel.setRowCount(0);

            for (Consumer consumer : consumers) {
                tableModel.addRow(new Object[]{
                    consumer.getId(),
                    consumer.getName(),
                    consumer.getMeterNumber(),
                    consumer.getPhone(),
                    consumer.getConnectionType(),
                    consumer.getAddress()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error searching consumers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDetails() {
        int selectedRow = consumerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a consumer first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long consumerId = (Long) tableModel.getValueAt(selectedRow, 0);

        try {
            Consumer consumer = consumerDAO.findById(consumerId).orElse(null);
            if (consumer != null) {
                String details = String.format(
                    "ID: %d\nName: %s\nMeter Number: %s\nPhone: %s\nConnection Type: %s\nAddress: %s",
                    consumer.getId(),
                    consumer.getName(),
                    consumer.getMeterNumber(),
                    consumer.getPhone(),
                    consumer.getConnectionType(),
                    consumer.getAddress()
                );

                JOptionPane.showMessageDialog(this,
                    details,
                    "Consumer Details",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading consumer details: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        int selectedRow = consumerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a consumer to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long consumerId = (Long) tableModel.getValueAt(selectedRow, 0);
        String consumerName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete consumer '" + consumerName + "'?\nThis will also delete all associated readings, bills and payments.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                consumerDAO.delete(consumerId);
                JOptionPane.showMessageDialog(this,
                    "Consumer deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadConsumers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting consumer: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
