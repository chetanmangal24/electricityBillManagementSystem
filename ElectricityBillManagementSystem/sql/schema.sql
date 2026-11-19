-- Electricity Bill Management System - Database Schema
-- Safe schema creation script (non-destructive, uses IF NOT EXISTS)

-- Create database
CREATE DATABASE IF NOT EXISTS ebms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ebms;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB;

-- Consumers table
CREATE TABLE IF NOT EXISTS consumers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    meter_number VARCHAR(50) NOT NULL UNIQUE,
    connection_type ENUM('RESIDENTIAL', 'COMMERCIAL', 'INDUSTRIAL') NOT NULL DEFAULT 'RESIDENTIAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_meter_number (meter_number),
    INDEX idx_name (name)
) ENGINE=InnoDB;

-- Meter readings table
CREATE TABLE IF NOT EXISTS meter_readings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumer_id BIGINT NOT NULL,
    reading_date DATE NOT NULL,
    previous_reading DECIMAL(10, 2) NOT NULL,
    current_reading DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reading_consumer FOREIGN KEY (consumer_id)
        REFERENCES consumers(id) ON DELETE CASCADE,
    CONSTRAINT chk_valid_reading CHECK (current_reading >= previous_reading),
    UNIQUE KEY unique_consumer_date (consumer_id, reading_date),
    INDEX idx_consumer_date (consumer_id, reading_date)
) ENGINE=InnoDB;

-- Bills table
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(50) NOT NULL UNIQUE,
    consumer_id BIGINT NOT NULL,
    reading_id BIGINT NOT NULL,
    bill_date DATE NOT NULL,
    units_consumed DECIMAL(10, 2) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    due_date DATE NOT NULL,
    status ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID', 'OVERDUE') NOT NULL DEFAULT 'UNPAID',
    amount_paid DECIMAL(12, 2) DEFAULT 0.00,
    amount_remaining DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_consumer FOREIGN KEY (consumer_id)
        REFERENCES consumers(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_reading FOREIGN KEY (reading_id)
        REFERENCES meter_readings(id) ON DELETE CASCADE,
    CONSTRAINT chk_units_positive CHECK (units_consumed >= 0),
    CONSTRAINT chk_amount_positive CHECK (amount >= 0),
    CONSTRAINT chk_amount_paid_valid CHECK (amount_paid >= 0 AND amount_paid <= amount),
    CONSTRAINT chk_amount_remaining_valid CHECK (amount_remaining >= 0 AND amount_remaining <= amount),
    UNIQUE KEY unique_reading_bill (reading_id),
    INDEX idx_bill_number (bill_number),
    INDEX idx_consumer_status (consumer_id, status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB;

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    amount_paid DECIMAL(12, 2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'UPI', 'NETBANKING', 'CHEQUE') NOT NULL,
    transaction_reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_bill FOREIGN KEY (bill_id)
        REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_amount_positive CHECK (amount_paid > 0),
    INDEX idx_bill_date (bill_id, payment_date),
    INDEX idx_payment_date (payment_date)
) ENGINE=InnoDB;

-- Create a view for bill summary with consumer details
CREATE OR REPLACE VIEW bill_summary AS
SELECT
    b.id AS bill_id,
    b.bill_number,
    c.name AS consumer_name,
    c.meter_number,
    c.connection_type,
    b.bill_date,
    b.due_date,
    b.units_consumed,
    b.amount AS total_amount,
    b.amount_paid,
    b.amount_remaining,
    b.status,
    DATEDIFF(CURDATE(), b.due_date) AS days_overdue
FROM bills b
INNER JOIN consumers c ON b.consumer_id = c.id;

-- Insert sample data for testing (if not exists)
INSERT IGNORE INTO consumers (id, name, address, phone, meter_number, connection_type) VALUES
(1, 'Sample Consumer', '123 Main Street', '9876543210', 'MTR001', 'RESIDENTIAL');

-- Insert default admin user (username: admin, password: admin123)
-- Password hash generated using PBKDF2WithHmacSHA256 (10,000 iterations)
INSERT IGNORE INTO users (username, password_hash, full_name, role) VALUES
('admin', 'Lx5Nc6vOdKKuL3j7qkUZ3A==:J2kP6GX5OueOmx06LZFOyeTRdHsxGdEXip7x+47nAu4=', 'System Administrator', 'ADMIN');
