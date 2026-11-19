package com.ebms.ui;

import com.ebms.dao.ConsumerDAO;
import com.ebms.dao.MeterReadingDAO;
import com.ebms.model.Consumer;
import com.ebms.model.MeterReading;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Panel for recording meter readings.
 */
public class MeterReadingPanel extends JPanel {

    private final ConsumerDAO consumerDAO;
    private final MeterReadingDAO readingDAO;
    private JComboBox<ConsumerItem> consumerCombo;
    private JTextField readingDateField;
    private JTextField previousReadingField;
    private JTextField currentReadingField;
    private JLabel unitsLabel;

    public MeterReadingPanel() {
        this.consumerDAO = new ConsumerDAO();
        this.readingDAO = new MeterReadingDAO();
        initComponents();
        loadConsumers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Record Meter Reading", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Consumer
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Consumer:"), gbc);

        consumerCombo = new JComboBox<>();
        consumerCombo.addActionListener(e -> loadLatestReading());
        gbc.gridx = 1;
        formPanel.add(consumerCombo, gbc);

        // Reading Date
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Reading Date (YYYY-MM-DD):"), gbc);

        readingDateField = new JTextField(20);
        readingDateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        formPanel.add(readingDateField, gbc);

        // Previous Reading
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Previous Reading (kWh):"), gbc);

        previousReadingField = new JTextField(20);
        previousReadingField.setEditable(false);
        gbc.gridx = 1;
        formPanel.add(previousReadingField, gbc);

        // Current Reading
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Current Reading (kWh):"), gbc);

        currentReadingField = new JTextField(20);
        currentReadingField.addActionListener(e -> calculateUnits());
        gbc.gridx = 1;
        formPanel.add(currentReadingField, gbc);

        JButton calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(e -> calculateUnits());
        gbc.gridx = 2;
        formPanel.add(calculateButton, gbc);

        // Units Consumed
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Units Consumed:"), gbc);

        unitsLabel = new JLabel("0.00 kWh");
        unitsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1;
        formPanel.add(unitsLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton saveButton = new JButton("Save Reading");
        saveButton.addActionListener(e -> handleSave());
        buttonPanel.add(saveButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadConsumers() {
        try {
            List<Consumer> consumers = consumerDAO.findAll();
            consumerCombo.removeAllItems();

            for (Consumer consumer : consumers) {
                consumerCombo.addItem(new ConsumerItem(consumer));
            }

            if (consumerCombo.getItemCount() > 0) {
                loadLatestReading();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading consumers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLatestReading() {
        ConsumerItem selected = (ConsumerItem) consumerCombo.getSelectedItem();
        if (selected == null) return;

        try {
            Optional<MeterReading> latestReading = readingDAO.getLatestReading(selected.consumer.getId());

            if (latestReading.isPresent()) {
                previousReadingField.setText(latestReading.get().getCurrentReading().toString());
            } else {
                previousReadingField.setText("0.00");
            }

            currentReadingField.setText("");
            unitsLabel.setText("0.00 kWh");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading latest reading: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateUnits() {
        try {
            BigDecimal previous = new BigDecimal(previousReadingField.getText());
            BigDecimal current = new BigDecimal(currentReadingField.getText());
            BigDecimal units = current.subtract(previous);
            unitsLabel.setText(units.toString() + " kWh");
        } catch (NumberFormatException ex) {
            unitsLabel.setText("Invalid input");
        }
    }

    private void handleSave() {
        ConsumerItem selected = (ConsumerItem) consumerCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a consumer.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate readingDate = LocalDate.parse(readingDateField.getText());
            BigDecimal previousReading = new BigDecimal(previousReadingField.getText());
            BigDecimal currentReading = new BigDecimal(currentReadingField.getText());

            // Validate
            if (currentReading.compareTo(previousReading) < 0) {
                JOptionPane.showMessageDialog(this,
                    "Current reading cannot be less than previous reading.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check duplicate
            if (readingDAO.readingExistsForDate(selected.consumer.getId(), readingDate)) {
                JOptionPane.showMessageDialog(this,
                    "A reading already exists for this consumer on this date.",
                    "Duplicate Reading",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            MeterReading reading = new MeterReading(
                selected.consumer.getId(),
                readingDate,
                previousReading,
                currentReading
            );

            readingDAO.create(reading);

            JOptionPane.showMessageDialog(this,
                "Meter reading saved successfully!\nUnits consumed: " + reading.getUnitsConsumed() + " kWh",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        readingDateField.setText(LocalDate.now().toString());
        currentReadingField.setText("");
        unitsLabel.setText("0.00 kWh");
        if (consumerCombo.getItemCount() > 0) {
            consumerCombo.setSelectedIndex(0);
            loadLatestReading();
        }
    }

    private static class ConsumerItem {
        Consumer consumer;

        ConsumerItem(Consumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public String toString() {
            return consumer.getName() + " (" + consumer.getMeterNumber() + ")";
        }
    }
}
