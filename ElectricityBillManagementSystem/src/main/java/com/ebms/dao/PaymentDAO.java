package com.ebms.dao;

import com.ebms.model.Payment;
import com.ebms.model.Payment.PaymentMethod;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Payment entity.
 */
public class PaymentDAO extends BaseDAO {

    /**
     * Create a new payment and update bill in a transaction.
     */
    public Payment create(Payment payment) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert payment
            String sql = "INSERT INTO payments (bill_id, payment_date, amount_paid, payment_method, transaction_reference) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, payment.getBillId());
                pstmt.setDate(2, Date.valueOf(payment.getPaymentDate()));
                pstmt.setBigDecimal(3, payment.getAmountPaid());
                pstmt.setString(4, payment.getPaymentMethod().name());
                pstmt.setString(5, payment.getTransactionReference());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating payment failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating payment failed, no ID obtained.");
                    }
                }
            }

            // Update bill via BillDAO
            BillDAO billDAO = new BillDAO();
            billDAO.updatePayment(payment.getBillId(), payment.getAmountPaid());

            conn.commit();
            return payment;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Find all payments for a bill.
     */
    public List<Payment> findByBillId(Long billId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE bill_id = ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, billId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        }

        return payments;
    }

    /**
     * Find payments by date range.
     */
    public List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM payments WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        }

        return payments;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setBillId(rs.getLong("bill_id"));
        payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        payment.setAmountPaid(rs.getBigDecimal("amount_paid"));
        payment.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        payment.setTransactionReference(rs.getString("transaction_reference"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            payment.setCreatedAt(createdAt.toLocalDateTime());
        }

        return payment;
    }
}
