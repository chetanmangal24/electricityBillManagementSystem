package com.ebms.dao;

import com.ebms.model.Consumer;
import com.ebms.model.Consumer.ConnectionType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for Consumer entity.
 */
public class ConsumerDAO extends BaseDAO {

    /**
     * Create a new consumer.
     */
    public Consumer create(Consumer consumer) throws SQLException {
        String sql = "INSERT INTO consumers (name, address, phone, meter_number, connection_type) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, consumer.getName());
            pstmt.setString(2, consumer.getAddress());
            pstmt.setString(3, consumer.getPhone());
            pstmt.setString(4, consumer.getMeterNumber());
            pstmt.setString(5, consumer.getConnectionType().name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating consumer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    consumer.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating consumer failed, no ID obtained.");
                }
            }
        }

        return consumer;
    }

    /**
     * Find consumer by ID.
     */
    public Optional<Consumer> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM consumers WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToConsumer(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Find consumer by meter number.
     */
    public Optional<Consumer> findByMeterNumber(String meterNumber) throws SQLException {
        String sql = "SELECT * FROM consumers WHERE meter_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meterNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToConsumer(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Find all consumers.
     */
    public List<Consumer> findAll() throws SQLException {
        String sql = "SELECT * FROM consumers ORDER BY name";
        List<Consumer> consumers = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                consumers.add(mapResultSetToConsumer(rs));
            }
        }

        return consumers;
    }

    /**
     * Search consumers by name or meter number.
     */
    public List<Consumer> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM consumers WHERE name LIKE ? OR meter_number LIKE ? ORDER BY name";
        List<Consumer> consumers = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    consumers.add(mapResultSetToConsumer(rs));
                }
            }
        }

        return consumers;
    }

    /**
     * Update consumer.
     */
    public void update(Consumer consumer) throws SQLException {
        String sql = "UPDATE consumers SET name = ?, address = ?, phone = ?, meter_number = ?, connection_type = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, consumer.getName());
            pstmt.setString(2, consumer.getAddress());
            pstmt.setString(3, consumer.getPhone());
            pstmt.setString(4, consumer.getMeterNumber());
            pstmt.setString(5, consumer.getConnectionType().name());
            pstmt.setLong(6, consumer.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete consumer by ID.
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM consumers WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Check if meter number exists.
     */
    public boolean meterNumberExists(String meterNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM consumers WHERE meter_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meterNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Check if meter number exists excluding a specific consumer.
     */
    public boolean meterNumberExistsExcluding(String meterNumber, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM consumers WHERE meter_number = ? AND id != ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meterNumber);
            pstmt.setLong(2, excludeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private Consumer mapResultSetToConsumer(ResultSet rs) throws SQLException {
        Consumer consumer = new Consumer();
        consumer.setId(rs.getLong("id"));
        consumer.setName(rs.getString("name"));
        consumer.setAddress(rs.getString("address"));
        consumer.setPhone(rs.getString("phone"));
        consumer.setMeterNumber(rs.getString("meter_number"));
        consumer.setConnectionType(ConnectionType.valueOf(rs.getString("connection_type")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            consumer.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            consumer.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return consumer;
    }
}
