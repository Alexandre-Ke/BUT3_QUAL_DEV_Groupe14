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

    @Test
    public void testHashIsHexAndLowercase() {
        String hash = PasswordHasher.hashPassword("adminpass");
        assertEquals("Un hash SHA-256 doit faire 64 caractères", 64, hash.length());
        assertTrue("Le hash doit être en hexadécimal minuscule",
                hash.matches("[0-9a-f]{64}"));
    }

    @Test
    public void testHashIsNotPlainPassword() {
        String pwd = "adminpass";
        String hash = PasswordHasher.hashPassword(pwd);
        assertNotEquals("Le hash ne doit jamais être égal au mot de passe en clair", pwd, hash);
    }

    @Test
    public void testEmptyPasswordIsHashed() {
        String hash = PasswordHasher.hashPassword("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(PasswordHasher.verifyPassword("", hash));
    }

    @Test
    public void testVeryLongPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10_000; i++) sb.append('x');
        String longPwd = sb.toString();

        String hash = PasswordHasher.hashPassword(longPwd);
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(PasswordHasher.verifyPassword(longPwd, hash));
    }

    @Test
    public void testVerifyPasswordWrongPassword() {
        String hash = PasswordHasher.hashPassword("correct");
        assertFalse("Un mauvais mot de passe ne doit pas être validé",
                PasswordHasher.verifyPassword("wrong", hash));
    }

    @Test(expected = NullPointerException.class)
    public void testVerifyPasswordNullPasswordThrowsException() {
        String hash = PasswordHasher.hashPassword("secret");
        PasswordHasher.verifyPassword(null, hash);
    }


    @Test
    public void testDeterminismSameInputSameHash() {
        String h1 = PasswordHasher.hashPassword("same");
        String h2 = PasswordHasher.hashPassword("same");
        assertEquals("Le même mot de passe doit donner le même hash", h1, h2);
    }

    @Test
    public void testDifferentInputsDifferentHashes() {
        String h1 = PasswordHasher.hashPassword("p1");
        String h2 = PasswordHasher.hashPassword("p2");
        assertNotEquals("Deux mots de passe différents doivent donner des hash différents", h1, h2);
    }
}
