package com.ebms.dao;

import com.ebms.model.Bill;
import com.ebms.model.Bill.BillStatus;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillDAO extends BaseDAO {

    public Bill create(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (bill_number, consumer_id, reading_id, bill_date, units_consumed, amount, due_date, status, amount_paid, amount_remaining) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, bill.getBillNumber());
            pstmt.setLong(2, bill.getConsumerId());
            pstmt.setLong(3, bill.getReadingId());
            pstmt.setDate(4, Date.valueOf(bill.getBillDate()));
            pstmt.setBigDecimal(5, bill.getUnitsConsumed());
            pstmt.setBigDecimal(6, bill.getAmount());
            pstmt.setDate(7, Date.valueOf(bill.getDueDate()));
            pstmt.setString(8, bill.getStatus().name());
            pstmt.setBigDecimal(9, bill.getAmountPaid());
            pstmt.setBigDecimal(10, bill.getAmountRemaining());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bill.setId(generatedKeys.getLong(1));
                }
            }
        }
        return bill;
    }

    public Optional<Bill> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM bills WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Bill> findByBillNumber(String billNumber) throws SQLException {
        String sql = "SELECT * FROM bills WHERE bill_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, billNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Bill> findByConsumerId(Long consumerId) throws SQLException {
        String sql = "SELECT * FROM bills WHERE consumer_id = ? ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, consumerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
            }
        }
        return bills;
    }

    public List<Bill> findPendingBills() throws SQLException {
        String sql = "SELECT * FROM bills WHERE status IN ('UNPAID', 'PARTIALLY_PAID', 'OVERDUE') ORDER BY due_date";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                bills.add(mapResultSetToBill(rs));
            }
        }
        return bills;
    }

    public List<Bill> search(String keyword) throws SQLException {
        String sql = "SELECT b.* FROM bills b LEFT JOIN consumers c ON b.consumer_id = c.id WHERE b.bill_number LIKE ? OR c.name LIKE ? OR c.meter_number LIKE ? ORDER BY b.bill_date DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
            }
        }
        return bills;
    }

    public void updatePayment(Long billId, BigDecimal paymentAmount) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            Bill bill = findById(billId).orElseThrow(() -> new SQLException("Bill not found"));

            BigDecimal newAmountPaid = bill.getAmountPaid().add(paymentAmount);
            BigDecimal newAmountRemaining = bill.getAmount().subtract(newAmountPaid);

            BillStatus newStatus;
            if (newAmountRemaining.compareTo(BigDecimal.ZERO) == 0) {
                newStatus = BillStatus.PAID;
            } else if (newAmountPaid.compareTo(BigDecimal.ZERO) > 0) {
                newStatus = BillStatus.PARTIALLY_PAID;
            } else {
                newStatus = bill.getStatus();
            }

            String sql = "UPDATE bills SET amount_paid = ?, amount_remaining = ?, status = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, newAmountPaid);
                pstmt.setBigDecimal(2, newAmountRemaining);
                pstmt.setString(3, newStatus.name());
                pstmt.setLong(4, billId);
                pstmt.executeUpdate();
            }

            conn.commit();
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

    public int markOverdueBills() throws SQLException {
        String sql = "UPDATE bills SET status = 'OVERDUE' WHERE due_date < CURDATE() AND status IN ('UNPAID', 'PARTIALLY_PAID')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        }
    }

    public BigDecimal getTotalPendingAmount(Long consumerId) throws SQLException {
        String sql = "SELECT SUM(amount_remaining) FROM bills WHERE consumer_id = ? AND status != 'PAID'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, consumerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal(1);
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setId(rs.getLong("id"));
        bill.setBillNumber(rs.getString("bill_number"));
        bill.setConsumerId(rs.getLong("consumer_id"));
        bill.setReadingId(rs.getLong("reading_id"));
        bill.setBillDate(rs.getDate("bill_date").toLocalDate());
        bill.setUnitsConsumed(rs.getBigDecimal("units_consumed"));
        bill.setAmount(rs.getBigDecimal("amount"));
        bill.setDueDate(rs.getDate("due_date").toLocalDate());
        bill.setStatus(BillStatus.valueOf(rs.getString("status")));
        bill.setAmountPaid(rs.getBigDecimal("amount_paid"));
        bill.setAmountRemaining(rs.getBigDecimal("amount_remaining"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            bill.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            bill.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return bill;
    }
}
