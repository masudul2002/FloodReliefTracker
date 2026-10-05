package com.floodrelief.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PBKDF2 Password Hashing and Constant-Time Verification.
 * Located in com.floodrelief.service as per architectural roadmap.
 */
public class PasswordSecurity {
    private static final Logger logger = LoggerFactory.getLogger(PasswordSecurity.class);
    private static final int ITERATIONS = 12000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            HexFormat hex = HexFormat.of();
            return hex.formatHex(salt) + ":" + hex.formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error hashing password: {}", e.getMessage());
            throw new RuntimeException("Password hashing error", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            HexFormat hex = HexFormat.of();
            byte[] salt = hex.parseHex(parts[0]);
            byte[] expectedHash = hex.parseHex(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            // Constant-time comparison
            int diff = expectedHash.length ^ testHash.length;
            for (int i = 0; i < expectedHash.length && i < testHash.length; i++) {
                diff |= expectedHash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            logger.error("Error verifying password: {}", e.getMessage());
            return false;
        }
    }
}
