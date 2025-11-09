package com.iut.banque.test.facade;

import com.iut.banque.constants.LoginConstants;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.modele.*;
import com.iut.banque.facade.BanqueFacade;
import com.iut.banque.facade.BanqueManager;
import com.iut.banque.facade.LoginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestsBanqueFacade {

    @Mock private LoginManager loginManager;
    @Mock private BanqueManager banqueManager;

    @Mock private Compte compte;
    @Mock private CompteAvecDecouvert compteAvecDecouvert;
    @Mock private Client client;
    @Mock private Gestionnaire gestionnaire;
    @Mock private Utilisateur simpleUser;

    private BanqueFacade facade;

    @Before
    public void setUp() {
        facade = new BanqueFacade(loginManager, banqueManager);
    }

    @Test
    public void testGetConnectedUser_delegueAuLoginManager() {
        when(loginManager.getConnectedUser()).thenReturn(simpleUser);
        assertSame(simpleUser, facade.getConnectedUser());
        verify(loginManager).getConnectedUser();
    }

    @Test
    public void testTryLogin_managerDeclencheLoadClients() {
        when(loginManager.tryLogin("admin","pass")).thenReturn(LoginConstants.MANAGER_IS_CONNECTED);

        int res = facade.tryLogin("admin","pass");

        assertEquals(LoginConstants.MANAGER_IS_CONNECTED, res);
        verify(banqueManager).loadAllClients();
    }

    @Test
    public void testTryLogin_userNeChargePasClients() {
        when(loginManager.tryLogin("u","p")).thenReturn(LoginConstants.USER_IS_CONNECTED);

        int res = facade.tryLogin("u","p");

        assertEquals(LoginConstants.USER_IS_CONNECTED, res);
        verify(banqueManager, never()).loadAllClients();
    }

    @Test
    public void testCrediter_delegue() throws Exception {
        facade.crediter(compte, 10.0);
        verify(banqueManager).crediter(compte, 10.0);
    }

    @Test
    public void testDebiter_delegue() throws Exception {
        facade.debiter(compte, 5.0);
        verify(banqueManager).debiter(compte, 5.0);
    }

    @Test
    public void testLogout_delegue() {
        facade.logout();
        verify(loginManager).logout();
    }

    @Test
    public void testGetAllClients_delegue() {
        Map<String, Client> m = Collections.emptyMap();
        when(banqueManager.getAllClients()).thenReturn(m);

        assertSame(m, facade.getAllClients());
        verify(banqueManager).getAllClients();
    }

    @Test
    public void testCreateAccountSansDecouvert_autoriseSiGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(gestionnaire);

        facade.createAccount("FR0123456789", client);

        verify(banqueManager).createAccount("FR0123456789", client);
    }

    @Test
    public void testCreateAccountSansDecouvert_refuseSiPasGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(simpleUser);

        facade.createAccount("FR0123456789", client);

        verify(banqueManager, never()).createAccount(anyString(), any(Client.class));
    }

    @Test
    public void testCreateAccountAvecDecouvert_autoriseSiGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(gestionnaire);

        facade.createAccount("FR0123456789", client, 200.0);

        verify(banqueManager).createAccount("FR0123456789", client, 200.0);
    }

    @Test
    public void testCreateAccountAvecDecouvert_refuseSiPasGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(simpleUser);

        facade.createAccount("FR0123456789", client, 200.0);

        verify(banqueManager, never()).createAccount(anyString(), any(Client.class), anyDouble());
    }

    @Test
    public void testDeleteAccount_autoriseSiGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(gestionnaire);

        facade.deleteAccount(compte);

        verify(banqueManager).deleteAccount(compte);
    }

    @Test
    public void testDeleteAccount_refuseSiPasGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(simpleUser);

        facade.deleteAccount(compte);

        verify(banqueManager, never()).deleteAccount(any(Compte.class));
    }

    @Test
    public void testCreateManager_autoriseSiGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(gestionnaire);

        facade.createManager("g.id","pwd","N","P","Adr", true);

        verify(banqueManager).createManager("g.id","pwd","N","P","Adr", true);
    }

    @Test
    public void testCreateClient_autoriseSiGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(gestionnaire);

        facade.createClient("c.id","pwd","N","P","Adr", true,"1234567890");

        verify(banqueManager).createClient("c.id","pwd","N","P","Adr", true,"1234567890");
    }

    @Test
    public void testDeleteUser_autoriseSiGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(gestionnaire);

        facade.deleteUser(simpleUser);

        verify(banqueManager).deleteUser(simpleUser);
    }

    @Test
    public void testLoadClients_autoriseSiGestionnaire() {
        when(loginManager.getConnectedUser()).thenReturn(gestionnaire);

        facade.loadClients();

        verify(banqueManager).loadAllClients();
    }

    @Test
    public void testGetCompte_delegue() {
        when(banqueManager.getAccountById("FR000")).thenReturn(compte);

        assertSame(compte, facade.getCompte("FR000"));
        verify(banqueManager).getAccountById("FR000");
    }

    @Test
    public void testChangeDecouvert_autoriseSiGestionnaire() throws Exception {
        when(loginManager.getConnectedUser()).thenReturn(gestionnaire);

        facade.changeDecouvert(compteAvecDecouvert, 150.0);

        verify(banqueManager).changeDecouvert(compteAvecDecouvert, 150.0);
    }

    @Test
    public void testUpdatePassword_delegue() throws IllegalOperationException {
        facade.updatePassword(simpleUser, "old", "new");
        verify(banqueManager).updatePassword(simpleUser, "old", "new");
    }

    @Test
    public void testRechercheEtReinitMDP_delegue() {
        Utilisateur u = mock(Utilisateur.class);
        when(banqueManager.rechercherUtilisateurPourReinitialisation("id","n","p","num"))
                .thenReturn(u);

        assertSame(u, facade.rechercherUtilisateurPourReinitialisation("id","n","p","num"));
        verify(banqueManager).rechercherUtilisateurPourReinitialisation("id","n","p","num");

        facade.reinitialiserLeMotDePasse(u, "new");
        verify(banqueManager).reinitialiserLeMotDePasse(u, "new");
    }

    @Test
    public void testGetUserById_delegue() {
        Utilisateur u = mock(Utilisateur.class);
        when(banqueManager.getUserById("xyz")).thenReturn(u);

        assertSame(u, facade.getUserById("xyz"));
        verify(banqueManager).getUserById("xyz");
    }
}
