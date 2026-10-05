package com.floodrelief;

import com.floodrelief.service.PasswordSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordSecurityTest {

    @Test
    @DisplayName("Password hashing produces salted hash and verifies correctly")
    public void testHashAndVerify() {
        String password = "adminSecretPassword123";
        String hash = PasswordSecurity.hashPassword(password);

        assertNotNull(hash);
        assertTrue(hash.contains(":"), "Hash must contain salt delimiter");

        assertTrue(PasswordSecurity.verifyPassword(password, hash), "Password should verify with matching hash");
        assertFalse(PasswordSecurity.verifyPassword("wrongPassword", hash), "Wrong password should fail verification");
    }

    @Test
    @DisplayName("Hashing same password twice produces unique salts")
    public void testSaltUniqueness() {
        String password = "testPassword";
        String hash1 = PasswordSecurity.hashPassword(password);
        String hash2 = PasswordSecurity.hashPassword(password);

        assertNotEquals(hash1, hash2, "Hashes must have different random salts");
        assertTrue(PasswordSecurity.verifyPassword(password, hash1));
        assertTrue(PasswordSecurity.verifyPassword(password, hash2));
    }
}
