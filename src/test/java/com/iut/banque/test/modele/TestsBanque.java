package com.iut.banque.test.modele;

import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.modele.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestsBanque {

    private Banque banque;
    private CompteSansDecouvert cSans;
    private CompteAvecDecouvert cAvec;

    @Before
    public void setUp() throws Exception {
        banque = new Banque();

        Map<String, Client> clients = new HashMap<>();
        Map<String, Gestionnaire> gestionnaires = new HashMap<>();
        Map<String, Compte> comptes = new HashMap<>();

        // IDs valides
        Client c1 = new Client("John", "Doe", "20 rue Bouvier", true, "j.doe1", "password", "1234567890");
        Client c2 = new Client("Alice", "Smith", "21 rue Bouvier", false, "a.smith2", "password", "0987654321");
        clients.put(c1.getUserId(), c1);
        clients.put(c2.getUserId(), c2);

        Gestionnaire g1 = new Gestionnaire("Boss", "Man", "Siège", true, "admin", "adminpass");
        gestionnaires.put(g1.getUserId(), g1);

        // Comptes valides
        cSans = new CompteSansDecouvert("FR0123456789", 100.0, c1);
        cAvec = new CompteAvecDecouvert("FR9876543210", -50.0, 200.0, c1);
        comptes.put(cSans.getNumeroCompte(), cSans);
        comptes.put(cAvec.getNumeroCompte(), cAvec);

        banque.setClients(clients);
        banque.setGestionnaires(gestionnaires);
        banque.setAccounts(comptes);
    }

    @Test
    public void testGettersSettersMaps() {
        assertEquals(2, banque.getClients().size());        // 2 clients ajoutés
        assertEquals(1, banque.getGestionnaires().size());  // 1 gestionnaire ajouté
        assertEquals(2, banque.getAccounts().size());       // 2 comptes ajoutés
    }

    @Test
    public void testCrediterDelegation_ok() throws Exception {
        banque.crediter(cSans, 40.0);
        assertEquals(140.0, cSans.getSolde(), 0.001);
    }

    @Test(expected = IllegalFormatException.class)
    public void testCrediterDelegation_negatif_refuse() throws Exception {
        banque.crediter(cSans, -1.0);
    }

    @Test
    public void testDebiterDelegation_ok() throws Exception {
        banque.debiter(cSans, 30.0);
        assertEquals(70.0, cSans.getSolde(), 0.001);
    }

    @Test
    public void testDeleteUser_supprimeDeLaMap() {
        assertTrue(banque.getClients().containsKey("j.doe1"));
        banque.deleteUser("j.doe1");
        assertFalse(banque.getClients().containsKey("j.doe1"));
    }

    @Test
    public void testChangeDecouvert_ok() throws Exception {
        banque.changeDecouvert(cAvec, 250.0);
        assertEquals(250.0, cAvec.getDecouvertAutorise(), 0.001);
    }

    @Test(expected = IllegalFormatException.class)
    public void testChangeDecouvert_negatif_refuse() throws Exception {
        banque.changeDecouvert(cAvec, -10.0);
    }
}
