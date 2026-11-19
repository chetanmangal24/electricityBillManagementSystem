package com.ebms.util;

/**
 * Input validation utility.
 */
public class ValidationUtil {

    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validate phone number (10 digits).
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    /**
     * Validate name (letters, spaces, minimum 2 characters).
     */
    public static boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z\\s]{2,100}");
    }

    /**
     * Validate meter number (alphanumeric, 3-50 characters).
     */
    public static boolean isValidMeterNumber(String meterNumber) {
        return meterNumber != null && meterNumber.matches("[A-Z0-9]{3,50}");
    }

    /**
     * Check if string is not null or empty.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
