package com.ebms.ui;

import com.ebms.dao.BillDAO;
import com.ebms.dao.ConsumerDAO;
import com.ebms.dao.MeterReadingDAO;
import com.ebms.model.Bill;
import com.ebms.model.Consumer;
import com.ebms.model.MeterReading;
import com.ebms.service.TariffConfig;
import com.ebms.util.BillNumberGenerator;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for generating bills.
 */
public class GenerateBillPanel extends JPanel {

    private final BillDAO billDAO;
    private final ConsumerDAO consumerDAO;
    private final MeterReadingDAO readingDAO;
    private JComboBox<ConsumerItem> consumerCombo;
    private JComboBox<ReadingItem> readingCombo;
    private JTextField billDateField;
    private JTextField dueDateField;
    private JLabel unitsLabel;
    private JLabel amountLabel;
    private JTextArea tariffInfoArea;

    public GenerateBillPanel() {
        this.billDAO = new BillDAO();
        this.consumerDAO = new ConsumerDAO();
        this.readingDAO = new MeterReadingDAO();
        initComponents();
        loadConsumers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Generate Bill", SwingConstants.CENTER);
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
        consumerCombo.addActionListener(e -> loadUnbilledReadings());
        gbc.gridx = 1;
        formPanel.add(consumerCombo, gbc);

        // Reading
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Meter Reading:"), gbc);

        readingCombo = new JComboBox<>();
        readingCombo.addActionListener(e -> calculateBill());
        gbc.gridx = 1;
        formPanel.add(readingCombo, gbc);

        // Bill Date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Bill Date (YYYY-MM-DD):"), gbc);

        billDateField = new JTextField(20);
        billDateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        formPanel.add(billDateField, gbc);

        // Due Date
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc);

        dueDateField = new JTextField(20);
        dueDateField.setText(LocalDate.now().plusDays(30).toString());
        gbc.gridx = 1;
        formPanel.add(dueDateField, gbc);

        // Units Consumed
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Units Consumed:"), gbc);

        unitsLabel = new JLabel("0.00 kWh");
        unitsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1;
        formPanel.add(unitsLabel, gbc);

        // Bill Amount
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Bill Amount:"), gbc);

        amountLabel = new JLabel("₹0.00");
        amountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        amountLabel.setForeground(new Color(0, 150, 0));
        gbc.gridx = 1;
        formPanel.add(amountLabel, gbc);

        // Tariff Info
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Tariff Info:"), gbc);

        tariffInfoArea = new JTextArea(5, 30);
        tariffInfoArea.setEditable(false);
        tariffInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane tariffScroll = new JScrollPane(tariffInfoArea);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(tariffScroll, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton generateButton = new JButton("Generate Bill");
        generateButton.addActionListener(e -> handleGenerateBill());
        buttonPanel.add(generateButton);

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
                loadUnbilledReadings();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading consumers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUnbilledReadings() {
        ConsumerItem selected = (ConsumerItem) consumerCombo.getSelectedItem();
        if (selected == null) return;

        try {
            List<MeterReading> readings = readingDAO.findByConsumerId(selected.consumer.getId());
            readingCombo.removeAllItems();

            for (MeterReading reading : readings) {
                if (!readingDAO.isReadingBilled(reading.getId())) {
                    readingCombo.addItem(new ReadingItem(reading));
                }
            }

            if (readingCombo.getItemCount() > 0) {
                calculateBill();
                tariffInfoArea.setText(TariffConfig.getTariffInfo(selected.consumer.getConnectionType()));
            } else {
                unitsLabel.setText("No unbilled readings");
                amountLabel.setText("₹0.00");
                tariffInfoArea.setText("No unbilled readings available for this consumer.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading readings: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateBill() {
        ConsumerItem consumerItem = (ConsumerItem) consumerCombo.getSelectedItem();
        ReadingItem readingItem = (ReadingItem) readingCombo.getSelectedItem();

        if (consumerItem == null || readingItem == null) return;

        MeterReading reading = readingItem.reading;
        BigDecimal units = reading.getUnitsConsumed();
        BigDecimal amount = TariffConfig.calculateBillAmount(
            consumerItem.consumer.getConnectionType(),
            units
        );

        unitsLabel.setText(units.toString() + " kWh");
        amountLabel.setText("₹" + amount.toString());
    }

    private void handleGenerateBill() {
        ConsumerItem consumerItem = (ConsumerItem) consumerCombo.getSelectedItem();
        ReadingItem readingItem = (ReadingItem) readingCombo.getSelectedItem();

        if (consumerItem == null || readingItem == null) {
            JOptionPane.showMessageDialog(this,
                "Please select consumer and reading.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate billDate = LocalDate.parse(billDateField.getText());
            LocalDate dueDate = LocalDate.parse(dueDateField.getText());

            if (dueDate.isBefore(billDate)) {
                JOptionPane.showMessageDialog(this,
                    "Due date cannot be before bill date.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            Consumer consumer = consumerItem.consumer;
            MeterReading reading = readingItem.reading;

            BigDecimal units = reading.getUnitsConsumed();
            BigDecimal amount = TariffConfig.calculateBillAmount(consumer.getConnectionType(), units);

            // Generate unique bill number
            String billNumber = BillNumberGenerator.generateWithConsumer(consumer.getId());

            Bill bill = new Bill(
                billNumber,
                consumer.getId(),
                reading.getId(),
                billDate,
                units,
                amount,
                dueDate
            );

            billDAO.create(bill);

            JOptionPane.showMessageDialog(this,
                "Bill generated successfully!\n\nBill Number: " + billNumber + "\nAmount: ₹" + amount,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            loadUnbilledReadings();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error generating bill: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        billDateField.setText(LocalDate.now().toString());
        dueDateField.setText(LocalDate.now().plusDays(30).toString());
        unitsLabel.setText("0.00 kWh");
        amountLabel.setText("₹0.00");
        tariffInfoArea.setText("");
        if (consumerCombo.getItemCount() > 0) {
            consumerCombo.setSelectedIndex(0);
            loadUnbilledReadings();
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

    private static class ReadingItem {
        MeterReading reading;

        ReadingItem(MeterReading reading) {
            this.reading = reading;
        }

        @Override
        public String toString() {
            return reading.getReadingDate() + " - " + reading.getUnitsConsumed() + " kWh";
        }
    }
}
