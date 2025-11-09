package com.iut.banque.test.modele;

import com.iut.banque.modele.Utilisateur;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestsUtilisateurPwd {

    // Petit stub concret de Utilisateur pour pouvoir l’instancier
    private static class U extends Utilisateur {
        U() { super(); }
        U(String nom, String prenom, String adr, boolean m, String id, String pwd) {
            super(nom, prenom, adr, m, id, pwd);
        }
    }

    @Test
    public void testSetUserPwd_hashAutomatique() {
        U u = new U();
        u.setUserPwd("secret123");
        // 64 chars -> hash SHA-256 hex attendu
        assertEquals(64, u.getUserPwd().length());
    }

    @Test
    public void testSetUserPwd_preHashAccepteTelQuel() {
        U u = new U();
        String preHash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        u.setUserPwd(preHash);
        assertEquals(preHash, u.getUserPwd());
    }

    @Test
    public void testSetUserPwd_null_gardeNull() {
        U u = new U();
        u.setUserPwd(null);
        assertEquals(null, u.getUserPwd());
    }
}
