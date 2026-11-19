package com.ebms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment entity representing a payment record.
 */
public class Payment {

    private Long id;
    private Long billId;
    private LocalDate paymentDate;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private LocalDateTime createdAt;

    public enum PaymentMethod {
        CASH, CARD, UPI, NETBANKING, CHEQUE
    }

    // Constructors
    public Payment() {
    }

    public Payment(Long billId, LocalDate paymentDate, BigDecimal amountPaid,
                   PaymentMethod paymentMethod, String transactionReference) {
        this.billId = billId;
        this.paymentDate = paymentDate;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", billId=" + billId +
                ", amountPaid=" + amountPaid +
                ", paymentMethod=" + paymentMethod +
                '}';
    }
}
