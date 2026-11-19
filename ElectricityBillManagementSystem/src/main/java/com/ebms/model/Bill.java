package com.ebms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bill entity representing an electricity bill.
 */
public class Bill {

    private Long id;
    private String billNumber;
    private Long consumerId;
    private Long readingId;
    private LocalDate billDate;
    private BigDecimal unitsConsumed;
    private BigDecimal amount;
    private LocalDate dueDate;
    private BillStatus status;
    private BigDecimal amountPaid;
    private BigDecimal amountRemaining;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum BillStatus {
        UNPAID, PARTIALLY_PAID, PAID, OVERDUE
    }

    // Constructors
    public Bill() {
        this.amountPaid = BigDecimal.ZERO;
    }

    public Bill(String billNumber, Long consumerId, Long readingId, LocalDate billDate,
                BigDecimal unitsConsumed, BigDecimal amount, LocalDate dueDate) {
        this.billNumber = billNumber;
        this.consumerId = consumerId;
        this.readingId = readingId;
        this.billDate = billDate;
        this.unitsConsumed = unitsConsumed;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = BillStatus.UNPAID;
        this.amountPaid = BigDecimal.ZERO;
        this.amountRemaining = amount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
        this.readingId = readingId;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public BigDecimal getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(BigDecimal unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getAmountRemaining() {
        return amountRemaining;
    }

    public void setAmountRemaining(BigDecimal amountRemaining) {
        this.amountRemaining = amountRemaining;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", billNumber='" + billNumber + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", amountRemaining=" + amountRemaining +
                '}';
    }
}
