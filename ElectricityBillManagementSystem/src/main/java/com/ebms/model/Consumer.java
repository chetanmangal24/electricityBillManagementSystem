package com.ebms.model;

import java.time.LocalDateTime;

/**
 * Consumer entity representing an electricity consumer.
 */
public class Consumer {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private String meterNumber;
    private ConnectionType connectionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ConnectionType {
        RESIDENTIAL, COMMERCIAL, INDUSTRIAL
    }

    // Constructors
    public Consumer() {
    }

    public Consumer(String name, String address, String phone, String meterNumber, ConnectionType connectionType) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.meterNumber = meterNumber;
        this.connectionType = connectionType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
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
        return "Consumer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", meterNumber='" + meterNumber + '\'' +
                ", connectionType=" + connectionType +
                '}';
    }
}
