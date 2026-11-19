package com.ebms.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generate unique bill numbers.
 * Format: BILL-YYYYMM-XXXXXX
 */
public class BillNumberGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private BillNumberGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Generate bill number with sequential ID.
     * @param sequenceId Sequential database ID
     * @return Unique bill number
     */
    public static String generate(long sequenceId) {
        String yearMonth = LocalDate.now().format(FORMATTER);
        return String.format("BILL-%s-%06d", yearMonth, sequenceId);
    }

    /**
     * Generate bill number with consumer ID and timestamp.
     * @param consumerId Consumer ID
     * @return Unique bill number
     */
    public static String generateWithConsumer(long consumerId) {
        String yearMonth = LocalDate.now().format(FORMATTER);
        long timestamp = System.currentTimeMillis() % 100000;
        return String.format("BILL-%s-%03d-%05d", yearMonth, consumerId, timestamp);
    }
}
