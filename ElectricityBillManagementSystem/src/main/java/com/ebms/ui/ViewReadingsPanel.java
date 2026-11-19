package com.ebms.ui;

import com.ebms.dao.MeterReadingDAO;
import com.ebms.dao.ConsumerDAO;
import com.ebms.model.MeterReading;
import com.ebms.model.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for viewing meter readings.
 */
public class ViewReadingsPanel extends JPanel {

    private final MeterReadingDAO readingDAO;
    private final ConsumerDAO consumerDAO;
    private JTable readingTable;
    private DefaultTableModel tableModel;
    private JComboBox<ConsumerItem> consumerCombo;

    public ViewReadingsPanel() {
        this.readingDAO = new MeterReadingDAO();
        this.consumerDAO = new ConsumerDAO();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("View Meter Readings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(titleLabel);
        filterPanel.add(new JLabel("Filter by Consumer:"));

        consumerCombo = new JComboBox<>();
        consumerCombo.addItem(new ConsumerItem(null)); // All consumers
        consumerCombo.addActionListener(e -> loadReadings());
        filterPanel.add(consumerCombo);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadConsumers();
            loadReadings();
        });
        filterPanel.add(refreshButton);

        add(filterPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Consumer", "Meter Number", "Reading Date", "Previous (kWh)", "Current (kWh)", "Units Consumed"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        readingTable = new JTable(tableModel);
        readingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(readingTable);
        add(scrollPane, BorderLayout.CENTER);

        loadConsumers();
        loadReadings();
    }

    private void loadConsumers() {
        try {
            List<Consumer> consumers = consumerDAO.findAll();
            consumerCombo.removeAllItems();
            consumerCombo.addItem(new ConsumerItem(null)); // All consumers

            for (Consumer consumer : consumers) {
                consumerCombo.addItem(new ConsumerItem(consumer));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading consumers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReadings() {
        try {
            ConsumerItem selected = (ConsumerItem) consumerCombo.getSelectedItem();
            List<MeterReading> readings;

            if (selected == null || selected.consumer == null) {
                // Load all readings - we'll need to join with consumers
                readings = loadAllReadings();
            } else {
                readings = readingDAO.findByConsumerId(selected.consumer.getId());
            }

            tableModel.setRowCount(0);

            for (MeterReading reading : readings) {
                Consumer consumer = consumerDAO.findById(reading.getConsumerId()).orElse(null);

                tableModel.addRow(new Object[]{
                    reading.getId(),
                    consumer != null ? consumer.getName() : "Unknown",
                    consumer != null ? consumer.getMeterNumber() : "N/A",
                    reading.getReadingDate(),
                    reading.getPreviousReading(),
                    reading.getCurrentReading(),
                    reading.getUnitsConsumed()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading readings: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<MeterReading> loadAllReadings() throws SQLException {
        // For simplicity, load all consumers and their readings
        List<Consumer> consumers = consumerDAO.findAll();
        java.util.List<MeterReading> allReadings = new java.util.ArrayList<>();

        for (Consumer consumer : consumers) {
            allReadings.addAll(readingDAO.findByConsumerId(consumer.getId()));
        }

        return allReadings;
    }

    private static class ConsumerItem {
        Consumer consumer;

        ConsumerItem(Consumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public String toString() {
            if (consumer == null) {
                return "All Consumers";
            }
            return consumer.getName() + " (" + consumer.getMeterNumber() + ")";
        }
    }
}
