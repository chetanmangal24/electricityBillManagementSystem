package com.ebms.dao;

import com.ebms.model.MeterReading;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for MeterReading entity.
 */
public class MeterReadingDAO extends BaseDAO {

    /**
     * Create a new meter reading.
     */
    public MeterReading create(MeterReading reading) throws SQLException {
        String sql = "INSERT INTO meter_readings (consumer_id, reading_date, previous_reading, current_reading) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, reading.getConsumerId());
            pstmt.setDate(2, Date.valueOf(reading.getReadingDate()));
            pstmt.setBigDecimal(3, reading.getPreviousReading());
            pstmt.setBigDecimal(4, reading.getCurrentReading());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating meter reading failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reading.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating meter reading failed, no ID obtained.");
                }
            }
        }

        return reading;
    }

    /**
     * Find reading by ID.
     */
    public Optional<MeterReading> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM meter_readings WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReading(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Find all readings for a consumer.
     */
    public List<MeterReading> findByConsumerId(Long consumerId) throws SQLException {
        String sql = "SELECT * FROM meter_readings WHERE consumer_id = ? ORDER BY reading_date DESC";
        List<MeterReading> readings = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, consumerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    readings.add(mapResultSetToReading(rs));
                }
            }
        }

        return readings;
    }

    /**
     * Get latest reading for a consumer.
     */
    public Optional<MeterReading> getLatestReading(Long consumerId) throws SQLException {
        String sql = "SELECT * FROM meter_readings WHERE consumer_id = ? ORDER BY reading_date DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, consumerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReading(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Check if a reading exists for consumer on specific date.
     */
    public boolean readingExistsForDate(Long consumerId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM meter_readings WHERE consumer_id = ? AND reading_date = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, consumerId);
            pstmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Check if reading has been billed.
     */
    public boolean isReadingBilled(Long readingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills WHERE reading_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, readingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private MeterReading mapResultSetToReading(ResultSet rs) throws SQLException {
        MeterReading reading = new MeterReading();
        reading.setId(rs.getLong("id"));
        reading.setConsumerId(rs.getLong("consumer_id"));
        reading.setReadingDate(rs.getDate("reading_date").toLocalDate());
        reading.setPreviousReading(rs.getBigDecimal("previous_reading"));
        reading.setCurrentReading(rs.getBigDecimal("current_reading"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            reading.setCreatedAt(createdAt.toLocalDateTime());
        }

        return reading;
    }
}
