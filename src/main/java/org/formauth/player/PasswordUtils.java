package org.formauth.player;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Generate random salt
     * 
     * @return Base64 encoded salt string
     */
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * SHA-256
     * 
     * @param password password
     * @param salt salt
     * @return hashed password
     */
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Encrypt password for storage
     * Format: salt:hashedPassword
     * 
     * @param plainPassword plain text password to encrypt
     * @return encrypted password string
     */
    public static String encryptPassword(String plainPassword) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(plainPassword, salt);
        return salt + ":" + hashedPassword;
    }
    
    /**
     * Verify password against stored encrypted password
     * 
     * @param plainPassword plain text password to verify
     * @param storedPassword stored encrypted password
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        try {
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {
                return plainPassword.equals(storedPassword);
            }
            
            String salt = parts[0];
            String hashedStoredPassword = parts[1];
            
            String hashedInputPassword = hashPassword(plainPassword, salt);
            
            return hashedInputPassword.equals(hashedStoredPassword);
        } catch (Exception e) {
            return false;
        }
    }
} 
