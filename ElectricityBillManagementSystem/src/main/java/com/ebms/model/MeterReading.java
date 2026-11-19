package com.ebms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MeterReading entity representing a meter reading record.
 */
public class MeterReading {

    private Long id;
    private Long consumerId;
    private LocalDate readingDate;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private LocalDateTime createdAt;

    // Constructors
    public MeterReading() {
    }

    public MeterReading(Long consumerId, LocalDate readingDate,
                        BigDecimal previousReading, BigDecimal currentReading) {
        this.consumerId = consumerId;
        this.readingDate = readingDate;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
    }

    // Calculated field
    public BigDecimal getUnitsConsumed() {
        return currentReading.subtract(previousReading);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public LocalDate getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }

    public BigDecimal getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(BigDecimal previousReading) {
        this.previousReading = previousReading;
    }

    public BigDecimal getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(BigDecimal currentReading) {
        this.currentReading = currentReading;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MeterReading{" +
                "id=" + id +
                ", consumerId=" + consumerId +
                ", readingDate=" + readingDate +
                ", unitsConsumed=" + getUnitsConsumed() +
                '}';
    }
}
