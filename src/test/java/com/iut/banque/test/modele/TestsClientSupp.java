package com.iut.banque.test.modele;

import com.iut.banque.modele.Client;
import com.iut.banque.modele.CompteAvecDecouvert;
import com.iut.banque.modele.CompteSansDecouvert;
import com.iut.banque.exceptions.IllegalFormatException;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestsClientSupp {

    @Test
    public void testIdentity_formatBasique() throws Exception {
        Client c = new Client("NOM", "PRENOM", "ADRESSE", true, "n.prenom1", "password", "1234567890");
        assertEquals("PRENOM NOM (1234567890)", c.getIdentity());
    }

    @Test
    public void testSetUserId_formatValide() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        // ne lève pas d'exception et conserve la valeur
        c.setUserId("b.bb2");
        assertEquals("b.bb2", c.getUserId());
    }

    @Test(expected = IllegalFormatException.class)
    public void testSetUserId_formatInvalide_exception() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        c.setUserId("badFormat"); // pas du type a.aaaa1
    }

    @Test
    public void testSetNumeroClient_valide() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        c.setNumeroClient("0987654321");
        assertEquals("0987654321", c.getNumeroClient());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNumeroClient_null_refuse() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        c.setNumeroClient(null);
    }

    @Test(expected = IllegalFormatException.class)
    public void testSetNumeroClient_mauvaisFormat_exception() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        c.setNumeroClient("12345"); // pas 10 chiffres
    }

    @Test
    public void testAddAccount_ajoutePuisRecupere() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        CompteSansDecouvert csd = new CompteSansDecouvert("FR0000000001", 10.0, c);
        c.addAccount(csd);
        assertTrue(c.getAccounts().containsKey("FR0000000001"));
        assertEquals("FR0000000001", c.getAccounts().get("FR0000000001").getNumeroCompte());
    }

    @Test
    public void testAddAccount_ecraseMemeNumero() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        CompteSansDecouvert csd1 = new CompteSansDecouvert("FR0000000001", 10.0, c);
        CompteAvecDecouvert cad2 = new CompteAvecDecouvert("FR0000000001", 20.0, 100.0, c); // même id
        c.addAccount(csd1);
        c.addAccount(cad2); // remplace l’entrée
        assertEquals(1, c.getAccounts().size());
        assertEquals(20.0, c.getAccounts().get("FR0000000001").getSolde(), 0.0001);
    }

    @Test
    public void testGetComptesAvecSoldeNonNul_videPuisUnPuisDeux() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        // vide
        assertEquals(0, c.getComptesAvecSoldeNonNul().size());
        // +1 compte non nul
        c.addAccount(new CompteSansDecouvert("SA0000000001", 1, c));
        assertEquals(1, c.getComptesAvecSoldeNonNul().size());
        // +1 autre compte non nul
        c.addAccount(new CompteAvecDecouvert("AV0000000002", -10, 100, c));
        assertEquals(2, c.getComptesAvecSoldeNonNul().size());
    }

    @Test
    public void testPossedeComptesADecouvert_zeroEtPositif_false() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        c.addAccount(new CompteSansDecouvert("SA0000000003", 0, c));
        c.addAccount(new CompteAvecDecouvert("AV0000000004", 50, 100, c));
        assertFalse(c.possedeComptesADecouvert());
    }

    @Test
    public void testPossedeComptesADecouvert_negatif_true() throws Exception {
        Client c = new Client("N", "P", "A", true, "a.aa1", "pwd", "1234567890");
        c.addAccount(new CompteSansDecouvert("SA0000000005", 0, c));
        c.addAccount(new CompteAvecDecouvert("AV0000000006", -1, 100, c));
        assertTrue(c.possedeComptesADecouvert());
    }

    @Test
    public void testToString_contientChampsPrincipaux() throws Exception {
        Client c = new Client("NomX", "PrenomY", "AdrZ", true, "n.y1", "pwd", "1234567890");
        String s = c.toString();
        assertTrue(s.contains("NomX"));
        assertTrue(s.contains("PrenomY"));
        assertTrue(s.contains("1234567890"));
    }
}
