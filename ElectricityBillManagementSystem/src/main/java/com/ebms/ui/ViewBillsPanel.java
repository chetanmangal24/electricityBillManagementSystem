package com.ebms.ui;

import com.ebms.dao.BillDAO;
import com.ebms.dao.ConsumerDAO;
import com.ebms.model.Bill;
import com.ebms.model.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for viewing bills.
 */
public class ViewBillsPanel extends JPanel {

    private final BillDAO billDAO;
    private final ConsumerDAO consumerDAO;
    private JTable billTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ViewBillsPanel() {
        this.billDAO = new BillDAO();
        this.consumerDAO = new ConsumerDAO();
        initComponents();
        loadBills();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("View Bills", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(titleLabel);
        searchPanel.add(new JLabel("Search:"));

        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);

        JButton pendingButton = new JButton("Show Pending");
        pendingButton.addActionListener(e -> loadPendingBills());
        searchPanel.add(pendingButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadBills());
        searchPanel.add(refreshButton);

        add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Bill No", "Consumer", "Meter No", "Bill Date", "Due Date", "Units", "Amount", "Paid", "Remaining", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        billTable = new JTable(tableModel);
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(billTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(e -> viewDetails());
        buttonPanel.add(viewDetailsButton);

        JButton printButton = new JButton("Print Bill");
        printButton.addActionListener(e -> printBill());
        buttonPanel.add(printButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadBills() {
        try {
            // Load all bills with consumer info
            List<Consumer> consumers = consumerDAO.findAll();
            tableModel.setRowCount(0);

            for (Consumer consumer : consumers) {
                List<Bill> bills = billDAO.findByConsumerId(consumer.getId());

                for (Bill bill : bills) {
                    tableModel.addRow(new Object[]{
                        bill.getBillNumber(),
                        consumer.getName(),
                        consumer.getMeterNumber(),
                        bill.getBillDate(),
                        bill.getDueDate(),
                        bill.getUnitsConsumed(),
                        "₹" + bill.getAmount(),
                        "₹" + bill.getAmountPaid(),
                        "₹" + bill.getAmountRemaining(),
                        bill.getStatus()
                    });
                }
            }

            // Update overdue bills
            billDAO.markOverdueBills();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading bills: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPendingBills() {
        try {
            List<Bill> bills = billDAO.findPendingBills();
            tableModel.setRowCount(0);

            for (Bill bill : bills) {
                Consumer consumer = consumerDAO.findById(bill.getConsumerId()).orElse(null);

                if (consumer != null) {
                    tableModel.addRow(new Object[]{
                        bill.getBillNumber(),
                        consumer.getName(),
                        consumer.getMeterNumber(),
                        bill.getBillDate(),
                        bill.getDueDate(),
                        bill.getUnitsConsumed(),
                        "₹" + bill.getAmount(),
                        "₹" + bill.getAmountPaid(),
                        "₹" + bill.getAmountRemaining(),
                        bill.getStatus()
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading pending bills: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadBills();
            return;
        }

        try {
            List<Bill> bills = billDAO.search(keyword);
            tableModel.setRowCount(0);

            for (Bill bill : bills) {
                Consumer consumer = consumerDAO.findById(bill.getConsumerId()).orElse(null);

                if (consumer != null) {
                    tableModel.addRow(new Object[]{
                        bill.getBillNumber(),
                        consumer.getName(),
                        consumer.getMeterNumber(),
                        bill.getBillDate(),
                        bill.getDueDate(),
                        bill.getUnitsConsumed(),
                        "₹" + bill.getAmount(),
                        "₹" + bill.getAmountPaid(),
                        "₹" + bill.getAmountRemaining(),
                        bill.getStatus()
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error searching bills: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDetails() {
        int selectedRow = billTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a bill first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billNumber = (String) tableModel.getValueAt(selectedRow, 0);

        try {
            Bill bill = billDAO.findByBillNumber(billNumber).orElse(null);
            if (bill != null) {
                Consumer consumer = consumerDAO.findById(bill.getConsumerId()).orElse(null);

                String details = String.format(
                    "Bill Number: %s\n" +
                    "Consumer: %s\n" +
                    "Meter Number: %s\n" +
                    "Bill Date: %s\n" +
                    "Due Date: %s\n" +
                    "Units Consumed: %s kWh\n" +
                    "Total Amount: ₹%s\n" +
                    "Amount Paid: ₹%s\n" +
                    "Amount Remaining: ₹%s\n" +
                    "Status: %s",
                    bill.getBillNumber(),
                    consumer != null ? consumer.getName() : "Unknown",
                    consumer != null ? consumer.getMeterNumber() : "N/A",
                    bill.getBillDate(),
                    bill.getDueDate(),
                    bill.getUnitsConsumed(),
                    bill.getAmount(),
                    bill.getAmountPaid(),
                    bill.getAmountRemaining(),
                    bill.getStatus()
                );

                JOptionPane.showMessageDialog(this,
                    details,
                    "Bill Details",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading bill details: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printBill() {
        int selectedRow = billTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a bill to print.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billNumber = (String) tableModel.getValueAt(selectedRow, 0);

        try {
            Bill bill = billDAO.findByBillNumber(billNumber).orElse(null);
            if (bill != null) {
                Consumer consumer = consumerDAO.findById(bill.getConsumerId()).orElse(null);

                // Create print dialog
                JDialog printDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Bill - " + billNumber, true);
                printDialog.setSize(500, 600);
                printDialog.setLocationRelativeTo(this);

                JTextArea billText = new JTextArea();
                billText.setEditable(false);
                billText.setFont(new Font("Monospaced", Font.PLAIN, 12));
                billText.setMargin(new Insets(10, 10, 10, 10));

                String billContent = String.format(
                    "========================================\n" +
                    "   ELECTRICITY BILL MANAGEMENT SYSTEM   \n" +
                    "========================================\n\n" +
                    "Bill Number: %s\n" +
                    "Bill Date: %s\n" +
                    "Due Date: %s\n\n" +
                    "----------------------------------------\n" +
                    "CONSUMER DETAILS\n" +
                    "----------------------------------------\n" +
                    "Name: %s\n" +
                    "Address: %s\n" +
                    "Meter Number: %s\n" +
                    "Connection Type: %s\n\n" +
                    "----------------------------------------\n" +
                    "BILLING DETAILS\n" +
                    "----------------------------------------\n" +
                    "Units Consumed: %s kWh\n" +
                    "Total Amount: ₹%s\n" +
                    "Amount Paid: ₹%s\n" +
                    "Amount Remaining: ₹%s\n" +
                    "Status: %s\n\n" +
                    "----------------------------------------\n" +
                    "Please pay before due date to avoid\n" +
                    "late payment charges.\n" +
                    "========================================\n",
                    bill.getBillNumber(),
                    bill.getBillDate(),
                    bill.getDueDate(),
                    consumer != null ? consumer.getName() : "Unknown",
                    consumer != null ? consumer.getAddress() : "N/A",
                    consumer != null ? consumer.getMeterNumber() : "N/A",
                    consumer != null ? consumer.getConnectionType() : "N/A",
                    bill.getUnitsConsumed(),
                    bill.getAmount(),
                    bill.getAmountPaid(),
                    bill.getAmountRemaining(),
                    bill.getStatus()
                );

                billText.setText(billContent);

                JScrollPane scrollPane = new JScrollPane(billText);
                printDialog.add(scrollPane, BorderLayout.CENTER);

                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> printDialog.dispose());
                JPanel buttonPanel = new JPanel();
                buttonPanel.add(closeButton);
                printDialog.add(buttonPanel, BorderLayout.SOUTH);

                printDialog.setVisible(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading bill: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
