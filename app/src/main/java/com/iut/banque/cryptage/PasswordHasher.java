package com.iut.banque.cryptage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    // Hash en SHA-256 et renvoie une chaîne hexadécimale (64 caractères)
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hashage SHA-256", e);
        }
    }

    // Vérifie si un mot de passe correspond à un hash attendu
    public static boolean verifyPassword(String rawPassword, String expectedHash) {
        if (expectedHash == null) {
            return false;
        }
        String hashedInput = hashPassword(rawPassword);
        return hashedInput.equalsIgnoreCase(expectedHash.trim());
    }
}
