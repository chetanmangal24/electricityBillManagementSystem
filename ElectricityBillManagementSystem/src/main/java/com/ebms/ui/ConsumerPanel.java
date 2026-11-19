package com.ebms.ui;

import com.ebms.dao.ConsumerDAO;
import com.ebms.model.Consumer;
import com.ebms.model.Consumer.ConnectionType;
import com.ebms.util.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Panel for adding/editing consumers.
 */
public class ConsumerPanel extends JPanel {

    private final ConsumerDAO consumerDAO;
    private JTextField nameField;
    private JTextArea addressArea;
    private JTextField phoneField;
    private JTextField meterField;
    private JComboBox<ConnectionType> connectionTypeCombo;
    private Consumer editingConsumer;

    public ConsumerPanel() {
        this.consumerDAO = new ConsumerDAO();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Add New Consumer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);

        nameField = new JTextField(30);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(nameField, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Address:"), gbc);

        addressArea = new JTextArea(3, 30);
        addressArea.setLineWrap(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(addressScroll, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Phone:"), gbc);

        phoneField = new JTextField(30);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(phoneField, gbc);

        // Meter Number
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Meter Number:"), gbc);

        meterField = new JTextField(30);
        gbc.gridx = 1;
        formPanel.add(meterField, gbc);

        // Connection Type
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Connection Type:"), gbc);

        connectionTypeCombo = new JComboBox<>(ConnectionType.values());
        gbc.gridx = 1;
        formPanel.add(connectionTypeCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton saveButton = new JButton("Save Consumer");
        saveButton.addActionListener(e -> handleSave());
        buttonPanel.add(saveButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleSave() {
        // Validate inputs
        String name = nameField.getText().trim();
        String address = addressArea.getText().trim();
        String phone = phoneField.getText().trim();
        String meterNumber = meterField.getText().trim().toUpperCase();

        if (!ValidationUtil.isValidName(name)) {
            JOptionPane.showMessageDialog(this,
                "Invalid name. Use only letters and spaces (2-100 characters).",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtil.isNotEmpty(address)) {
            JOptionPane.showMessageDialog(this,
                "Address is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this,
                "Invalid phone number. Enter 10 digits.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidMeterNumber(meterNumber)) {
            JOptionPane.showMessageDialog(this,
                "Invalid meter number. Use alphanumeric characters (3-50 characters).",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Check duplicate meter number
            if (editingConsumer == null) {
                if (consumerDAO.meterNumberExists(meterNumber)) {
                    JOptionPane.showMessageDialog(this,
                        "Meter number already exists.",
                        "Duplicate Meter Number",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                if (consumerDAO.meterNumberExistsExcluding(meterNumber, editingConsumer.getId())) {
                    JOptionPane.showMessageDialog(this,
                        "Meter number already exists.",
                        "Duplicate Meter Number",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Consumer consumer = editingConsumer != null ? editingConsumer : new Consumer();
            consumer.setName(name);
            consumer.setAddress(address);
            consumer.setPhone(phone);
            consumer.setMeterNumber(meterNumber);
            consumer.setConnectionType((ConnectionType) connectionTypeCombo.getSelectedItem());

            if (editingConsumer == null) {
                consumerDAO.create(consumer);
                JOptionPane.showMessageDialog(this,
                    "Consumer created successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                consumerDAO.update(consumer);
                JOptionPane.showMessageDialog(this,
                    "Consumer updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }

            clearForm();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        nameField.setText("");
        addressArea.setText("");
        phoneField.setText("");
        meterField.setText("");
        connectionTypeCombo.setSelectedIndex(0);
        editingConsumer = null;
    }

    public void loadConsumer(Consumer consumer) {
        this.editingConsumer = consumer;
        nameField.setText(consumer.getName());
        addressArea.setText(consumer.getAddress());
        phoneField.setText(consumer.getPhone());
        meterField.setText(consumer.getMeterNumber());
        connectionTypeCombo.setSelectedItem(consumer.getConnectionType());
    }
}
