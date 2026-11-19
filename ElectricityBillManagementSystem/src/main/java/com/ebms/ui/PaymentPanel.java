package com.ebms.ui;

import com.ebms.dao.BillDAO;
import com.ebms.dao.ConsumerDAO;
import com.ebms.dao.PaymentDAO;
import com.ebms.model.Bill;
import com.ebms.model.Consumer;
import com.ebms.model.Payment;
import com.ebms.model.Payment.PaymentMethod;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for recording payments.
 */
public class PaymentPanel extends JPanel {

    private final PaymentDAO paymentDAO;
    private final BillDAO billDAO;
    private final ConsumerDAO consumerDAO;
    private JComboBox<ConsumerItem> consumerCombo;
    private JComboBox<BillItem> billCombo;
    private JTextField paymentDateField;
    private JTextField amountField;
    private JComboBox<PaymentMethod> paymentMethodCombo;
    private JTextField transactionRefField;
    private JLabel billAmountLabel;
    private JLabel paidLabel;
    private JLabel remainingLabel;

    public PaymentPanel() {
        this.paymentDAO = new PaymentDAO();
        this.billDAO = new BillDAO();
        this.consumerDAO = new ConsumerDAO();
        initComponents();
        loadConsumers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Record Payment", SwingConstants.CENTER);
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
        consumerCombo.addActionListener(e -> loadPendingBills());
        gbc.gridx = 1;
        formPanel.add(consumerCombo, gbc);

        // Bill
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Pending Bill:"), gbc);

        billCombo = new JComboBox<>();
        billCombo.addActionListener(e -> loadBillDetails());
        gbc.gridx = 1;
        formPanel.add(billCombo, gbc);

        // Bill Details Panel
        JPanel billDetailsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        billDetailsPanel.setBorder(BorderFactory.createTitledBorder("Bill Details"));

        billDetailsPanel.add(new JLabel("Total Amount:"));
        billAmountLabel = new JLabel("₹0.00");
        billAmountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        billDetailsPanel.add(billAmountLabel);

        billDetailsPanel.add(new JLabel("Amount Paid:"));
        paidLabel = new JLabel("₹0.00");
        billDetailsPanel.add(paidLabel);

        billDetailsPanel.add(new JLabel("Amount Remaining:"));
        remainingLabel = new JLabel("₹0.00");
        remainingLabel.setFont(new Font("Arial", Font.BOLD, 12));
        remainingLabel.setForeground(new Color(200, 0, 0));
        billDetailsPanel.add(remainingLabel);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(billDetailsPanel, gbc);
        gbc.gridwidth = 1;

        // Payment Date
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Payment Date (YYYY-MM-DD):"), gbc);

        paymentDateField = new JTextField(20);
        paymentDateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        formPanel.add(paymentDateField, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Payment Amount (₹):"), gbc);

        amountField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        // Payment Method
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Payment Method:"), gbc);

        paymentMethodCombo = new JComboBox<>(PaymentMethod.values());
        gbc.gridx = 1;
        formPanel.add(paymentMethodCombo, gbc);

        // Transaction Reference
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Transaction Reference:"), gbc);

        transactionRefField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(transactionRefField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton saveButton = new JButton("Record Payment");
        saveButton.addActionListener(e -> handleSavePayment());
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
                loadPendingBills();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading consumers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPendingBills() {
        ConsumerItem selected = (ConsumerItem) consumerCombo.getSelectedItem();
        if (selected == null) return;

        try {
            List<Bill> bills = billDAO.findByConsumerId(selected.consumer.getId());
            billCombo.removeAllItems();

            for (Bill bill : bills) {
                if (bill.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
                    billCombo.addItem(new BillItem(bill));
                }
            }

            if (billCombo.getItemCount() > 0) {
                loadBillDetails();
            } else {
                clearBillDetails();
                JOptionPane.showMessageDialog(this,
                    "No pending bills for this consumer.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading bills: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBillDetails() {
        BillItem selected = (BillItem) billCombo.getSelectedItem();
        if (selected == null) {
            clearBillDetails();
            return;
        }

        Bill bill = selected.bill;
        billAmountLabel.setText("₹" + bill.getAmount().toString());
        paidLabel.setText("₹" + bill.getAmountPaid().toString());
        remainingLabel.setText("₹" + bill.getAmountRemaining().toString());
        amountField.setText(bill.getAmountRemaining().toString());
    }

    private void clearBillDetails() {
        billAmountLabel.setText("₹0.00");
        paidLabel.setText("₹0.00");
        remainingLabel.setText("₹0.00");
        amountField.setText("");
    }

    private void handleSavePayment() {
        BillItem billItem = (BillItem) billCombo.getSelectedItem();

        if (billItem == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a bill to pay.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate paymentDate = LocalDate.parse(paymentDateField.getText());
            BigDecimal paymentAmount = new BigDecimal(amountField.getText());

            // Validate
            if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Payment amount must be greater than zero.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            Bill bill = billItem.bill;
            if (paymentAmount.compareTo(bill.getAmountRemaining()) > 0) {
                JOptionPane.showMessageDialog(this,
                    "Payment amount cannot exceed remaining amount (₹" +
                    bill.getAmountRemaining() + ").",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            Payment payment = new Payment(
                bill.getId(),
                paymentDate,
                paymentAmount,
                (PaymentMethod) paymentMethodCombo.getSelectedItem(),
                transactionRefField.getText().trim()
            );

            paymentDAO.create(payment);

            JOptionPane.showMessageDialog(this,
                "Payment recorded successfully!\n\nAmount: ₹" + paymentAmount +
                "\nRemaining: ₹" + bill.getAmountRemaining().subtract(paymentAmount),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            loadPendingBills();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid payment amount format.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error recording payment: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        paymentDateField.setText(LocalDate.now().toString());
        amountField.setText("");
        transactionRefField.setText("");
        paymentMethodCombo.setSelectedIndex(0);
        if (consumerCombo.getItemCount() > 0) {
            consumerCombo.setSelectedIndex(0);
            loadPendingBills();
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

    private static class BillItem {
        Bill bill;

        BillItem(Bill bill) {
            this.bill = bill;
        }

        @Override
        public String toString() {
            return bill.getBillNumber() + " - ₹" + bill.getAmountRemaining() + " due";
        }
    }
}
