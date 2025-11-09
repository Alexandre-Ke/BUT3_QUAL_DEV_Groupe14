package com.iut.banque.test.modele;

import com.iut.banque.cryptage.PasswordHasher;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.modele.Gestionnaire;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests unitaires de la classe Gestionnaire.
 */
public class TestsGestionnaire {

    @Test
    public void testConstructeurComplet_valide() throws IllegalFormatException {
        Gestionnaire g = new Gestionnaire("NOM", "PRENOM", "ADRESSE", true, "admin1", "PASS");
        assertEquals("NOM", g.getNom());
        assertEquals("PRENOM", g.getPrenom());
        assertEquals("ADRESSE", g.getAdresse());
        assertTrue(g.isMale());
        assertEquals("admin1", g.getUserId());
        // Le constructeur parent pose le mot de passe tel quel (non hashé)
        assertEquals("PASS", g.getUserPwd());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructeurComplet_userIdVide_declencheIAE() throws IllegalFormatException {
        new Gestionnaire("N", "P", "ADR", true, "", "PASS");
    }

    @Test
    public void testSettersGetters_apresConstructeurVide() throws IllegalFormatException {
        Gestionnaire g = new Gestionnaire();
        g.setNom("Durand");
        g.setPrenom("Jeanne");
        g.setAdresse("1 rue Test");
        g.setMale(false);
        g.setUserId("g.durand1");             // Utilisateur#setUserId déclare IllegalFormatException
        g.setUserPwd("monSecret");             // Utilisateur#setUserPwd hash si nécessaire

        assertEquals("Durand", g.getNom());
        assertEquals("Jeanne", g.getPrenom());
        assertEquals("1 rue Test", g.getAdresse());
        assertFalse(g.isMale());
        assertEquals("g.durand1", g.getUserId());

        String stored = g.getUserPwd();
        assertNotNull(stored);
        assertEquals("Le hash SHA-256 doit faire 64 caractères", 64, stored.length());
        assertTrue("Le hash doit valider le mot de passe d'origine",
                PasswordHasher.verifyPassword("monSecret", stored));
    }

    @Test
    public void testToString_contientInfosUtiles() throws IllegalFormatException {
        Gestionnaire g = new Gestionnaire("NOM", "PRENOM", "ADRESSE", false, "admin42", "PASS");
        String s = g.toString();
        assertTrue(s.contains("Gestionnaire"));
        assertTrue(s.contains("admin42"));
        assertTrue(s.contains("NOM"));
        assertTrue(s.contains("PRENOM"));
    }
}
