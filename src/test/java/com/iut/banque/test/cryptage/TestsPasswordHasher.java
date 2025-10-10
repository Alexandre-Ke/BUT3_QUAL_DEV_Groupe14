package com.iut.banque.test.cryptage;

import com.iut.banque.cryptage.PasswordHasher;
import org.junit.Test;

import static org.junit.Assert.*;


public class TestsPasswordHasher {
    @Test
    public void testHashNotNull() {
        String hash = PasswordHasher.hashPassword("adminpass");
        assertNotNull("Le hash ne doit pas être nul", hash);
    }

    @Test
    public void testHashLength() {
        String hash = PasswordHasher.hashPassword("adminpass");
        assertEquals("Le hash SHA-256 doit contenir 64 caractères hexadécimaux",
                64, hash.length());
    }

    @Test
    public void testSamePasswordSameHash() {
        String hash1 = PasswordHasher.hashPassword("adminpass");
        String hash2 = PasswordHasher.hashPassword("adminpass");
        assertEquals("Deux hash du même mot de passe doivent être identiques", hash1, hash2);
    }

    @Test
    public void testDifferentPasswordsDifferentHash() {
        String hash1 = PasswordHasher.hashPassword("adminpass");
        String hash2 = PasswordHasher.hashPassword("wrongpass");
        assertNotEquals("Deux mots de passe différents doivent produire des hash différents", hash1, hash2);
    }

    @Test
    public void testVerifyPasswordCorrect() {
        String password = "secret123";
        String hash = PasswordHasher.hashPassword(password);
        assertTrue("Le mot de passe correct doit être validé",
                PasswordHasher.verifyPassword(password, hash));
    }

    @Test
    public void testVerifyPasswordIncorrect() {
        String password = "secret123";
        String hash = PasswordHasher.hashPassword(password);
        assertFalse("Un mauvais mot de passe ne doit pas être validé",
                PasswordHasher.verifyPassword("badpass", hash));
    }
}
