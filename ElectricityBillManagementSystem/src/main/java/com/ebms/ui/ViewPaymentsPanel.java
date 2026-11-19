package com.ebms.ui;

import com.ebms.dao.PaymentDAO;
import com.ebms.dao.BillDAO;
import com.ebms.dao.ConsumerDAO;
import com.ebms.model.Payment;
import com.ebms.model.Bill;
import com.ebms.model.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for viewing payment history.
 */
public class ViewPaymentsPanel extends JPanel {

    private final PaymentDAO paymentDAO;
    private final BillDAO billDAO;
    private final ConsumerDAO consumerDAO;
    private JTable paymentTable;
    private DefaultTableModel tableModel;
    private JTextField billNumberField;

    public ViewPaymentsPanel() {
        this.paymentDAO = new PaymentDAO();
        this.billDAO = new BillDAO();
        this.consumerDAO = new ConsumerDAO();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Payment History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(titleLabel);
        filterPanel.add(new JLabel("Bill Number:"));

        billNumberField = new JTextField(15);
        filterPanel.add(billNumberField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchPayments());
        filterPanel.add(searchButton);

        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(e -> loadAllPayments());
        filterPanel.add(showAllButton);

        add(filterPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Payment ID", "Bill Number", "Consumer", "Payment Date", "Amount Paid", "Payment Method", "Transaction Ref"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        paymentTable = new JTable(tableModel);
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(paymentTable);
        add(scrollPane, BorderLayout.CENTER);

        loadAllPayments();
    }

    private void loadAllPayments() {
        try {
            LocalDate startDate = LocalDate.now().minusMonths(6);
            LocalDate endDate = LocalDate.now();

            List<Payment> payments = paymentDAO.findByDateRange(startDate, endDate);
            tableModel.setRowCount(0);

            for (Payment payment : payments) {
                Bill bill = billDAO.findById(payment.getBillId()).orElse(null);
                Consumer consumer = null;

                if (bill != null) {
                    consumer = consumerDAO.findById(bill.getConsumerId()).orElse(null);
                }

                tableModel.addRow(new Object[]{
                    payment.getId(),
                    bill != null ? bill.getBillNumber() : "N/A",
                    consumer != null ? consumer.getName() : "Unknown",
                    payment.getPaymentDate(),
                    "₹" + payment.getAmountPaid(),
                    payment.getPaymentMethod(),
                    payment.getTransactionReference() != null ? payment.getTransactionReference() : "N/A"
                });
            }

            billNumberField.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading payments: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPayments() {
        String billNumber = billNumberField.getText().trim();

        if (billNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a bill number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Bill bill = billDAO.findByBillNumber(billNumber).orElse(null);

            if (bill == null) {
                JOptionPane.showMessageDialog(this,
                    "Bill not found.",
                    "Search Result",
                    JOptionPane.INFORMATION_MESSAGE);
                tableModel.setRowCount(0);
                return;
            }

            List<Payment> payments = paymentDAO.findByBillId(bill.getId());
            Consumer consumer = consumerDAO.findById(bill.getConsumerId()).orElse(null);

            tableModel.setRowCount(0);

            for (Payment payment : payments) {
                tableModel.addRow(new Object[]{
                    payment.getId(),
                    bill.getBillNumber(),
                    consumer != null ? consumer.getName() : "Unknown",
                    payment.getPaymentDate(),
                    "₹" + payment.getAmountPaid(),
                    payment.getPaymentMethod(),
                    payment.getTransactionReference() != null ? payment.getTransactionReference() : "N/A"
                });
            }

            if (payments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No payments found for this bill.",
                    "Search Result",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error searching payments: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
