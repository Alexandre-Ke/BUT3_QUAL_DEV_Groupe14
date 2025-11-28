package com.iut.banque.test.modele;

import com.iut.banque.modele.Utilisateur;
import com.iut.banque.cryptage.PasswordHasher;
import com.iut.banque.exceptions.IllegalFormatException;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

/**
 * Tests supplémentaires pour la classe Utilisateur.
 */
public class TestsUtilisateur {

    /**
     * Implémentation concrète minimale pour tester l'abstraite Utilisateur.
     */
    private static class UtilisateurConcret extends Utilisateur {

        protected UtilisateurConcret() {
            super();
        }

        protected UtilisateurConcret(String nom, String prenom, String adresse,
                                     boolean male, String userId, String userPwd) {
            super(nom, prenom, adresse, male, userId, userPwd);
        }

        // aucune logique en plus, on réutilise tout de Utilisateur
    }

    // ------------------------------------------------------------------------
    // Constructeurs
    // ------------------------------------------------------------------------

    @Test
    public void testConstructeurSansParam_valeursParDefaut() {
        UtilisateurConcret u = new UtilisateurConcret();
        assertNull(u.getNom());
        assertNull(u.getPrenom());
        assertNull(u.getAdresse());
        assertFalse(u.isMale());
        assertNull(u.getUserId());
        assertNull(u.getUserPwd());
    }

    @Test
    public void testConstructeurAvecParam_champsInitialises() {
        UtilisateurConcret u = new UtilisateurConcret(
                "NOM",
                "PRENOM",
                "ADRESSE",
                true,
                "id.user1",
                "hashOuPwd"
        );

        assertEquals("NOM", u.getNom());
        assertEquals("PRENOM", u.getPrenom());
        assertEquals("ADRESSE", u.getAdresse());
        assertTrue(u.isMale());
        assertEquals("id.user1", u.getUserId());
        assertEquals("hashOuPwd", u.getUserPwd());
    }

    // ------------------------------------------------------------------------
    // Getters / setters simples
    // ------------------------------------------------------------------------

    @Test
    public void testSetGetNom() {
        UtilisateurConcret u = new UtilisateurConcret();
        u.setNom("Martin");
        assertEquals("Martin", u.getNom());
    }

    @Test
    public void testSetGetPrenom() {
        UtilisateurConcret u = new UtilisateurConcret();
        u.setPrenom("Alice");
        assertEquals("Alice", u.getPrenom());
    }

    @Test
    public void testSetGetAdresse() {
        UtilisateurConcret u = new UtilisateurConcret();
        u.setAdresse("42 rue du Test");
        assertEquals("42 rue du Test", u.getAdresse());
    }

    @Test
    public void testSetGetMale_truePuisFalse() {
        UtilisateurConcret u = new UtilisateurConcret();
        u.setMale(true);
        assertTrue(u.isMale());
        u.setMale(false);
        assertFalse(u.isMale());
    }

    // ------------------------------------------------------------------------
    // setUserId
    // ------------------------------------------------------------------------

    @Test
    public void testSetUserId_simple() throws IllegalFormatException {
        UtilisateurConcret u = new UtilisateurConcret();
        u.setUserId("test.id1");
        assertEquals("test.id1", u.getUserId());
    }

    // ------------------------------------------------------------------------
    // setUserPwd
    // ------------------------------------------------------------------------

    @Test
    public void testSetUserPwd_nullDonneNull() {
        UtilisateurConcret u = new UtilisateurConcret();
        u.setUserPwd(null);
        assertNull(u.getUserPwd());
    }

    @Test
    public void testSetUserPwd_hashDejaPresentConserveTelQuel() {
        UtilisateurConcret u = new UtilisateurConcret();

        // 64 caractères hexadécimaux (format détecté comme hash déjà présent)
        String existingHash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        assertEquals(64, existingHash.length());
        assertTrue(existingHash.matches("[0-9a-f]{64}"));
        assertTrue(Pattern.matches("[0-9a-f]{64}", existingHash));

        u.setUserPwd(existingHash);

        assertEquals(existingHash, u.getUserPwd());
    }

    @Test
    public void testSetUserPwd_motDePasseClair_produitUnHashHexa64() {
        UtilisateurConcret u = new UtilisateurConcret();

        String plain = "MonSuperMotDePasse!";

        u.setUserPwd(plain);

        String hashed = u.getUserPwd();
        assertNotNull(hashed);
        // le mot de passe stocké ne doit plus être le clair
        assertNotEquals(plain, hashed);
        // longueur de 64 caractères
        assertEquals(64, hashed.length());
        // uniquement des chiffres/lettres hexa
        assertTrue(hashed.matches("[0-9a-f]{64}"));
    }


    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString_contientChampsPrincipaux() {
        UtilisateurConcret u = new UtilisateurConcret(
                "NomX",
                "PrenomY",
                "AdrZ",
                true,
                "id.user1",
                "hash123"
        );

        String s = u.toString();
        assertTrue(s.contains("NomX"));
        assertTrue(s.contains("PrenomY"));
        assertTrue(s.contains("AdrZ"));
        assertTrue(s.contains("id.user1"));
        assertTrue(s.contains("hash123"));
    }
}
