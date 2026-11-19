package com.ebms.service;

import com.ebms.dao.BillDAO;
import com.ebms.dao.ConsumerDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for dashboard statistics.
 */
public class DashboardService {

    private final ConsumerDAO consumerDAO;
    private final BillDAO billDAO;

    public DashboardService() {
        this.consumerDAO = new ConsumerDAO();
        this.billDAO = new BillDAO();
    }

    /**
     * Get dashboard statistics.
     */
    public Map<String, Object> getStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        // Count consumers
        int consumerCount = consumerDAO.findAll().size();
        stats.put("totalConsumers", consumerCount);

        // Count pending bills
        int pendingBills = billDAO.findPendingBills().size();
        stats.put("pendingBills", pendingBills);

        // Calculate total pending amount
        BigDecimal totalPending = BigDecimal.ZERO;
        for (var bill : billDAO.findPendingBills()) {
            totalPending = totalPending.add(bill.getAmountRemaining());
        }
        stats.put("totalPendingAmount", totalPending);

        // Update overdue bills
        billDAO.markOverdueBills();

        return stats;
    }
}
