package com.ebms.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password hashing utility using PBKDF2WithHmacSHA256.
 * More secure than plain salted SHA-256.
 */
public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Hash a password with PBKDF2 and random salt.
     * @param password Plain text password
     * @return Salt and hash combined as "salt:hash"
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = generateSalt();
            byte[] hash = pbkdf2(password, salt);

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return saltBase64 + ":" + hashBase64;
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify a password against a stored hash.
     * @param password Plain text password to verify
     * @param storedHash Stored hash in "salt:hash" format
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] actualHash = pbkdf2(password, salt);

            return slowEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] pbkdf2(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /**
     * Generate hash for default admin password for initial setup.
     * Run this method to get the hash for "admin123".
     */
    public static void main(String[] args) {
        String password = "admin123";
        String hash = hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("\nVerification: " + verifyPassword(password, hash));
    }
}
