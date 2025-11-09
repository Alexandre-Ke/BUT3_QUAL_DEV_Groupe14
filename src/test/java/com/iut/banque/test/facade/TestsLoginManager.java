package com.iut.banque.test.facade;

import com.iut.banque.constants.LoginConstants;
import com.iut.banque.cryptage.PasswordHasher;
import com.iut.banque.facade.LoginManager;
import com.iut.banque.interfaces.IDao;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.modele.Utilisateur;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestsLoginManager {

    @Mock
    private IDao dao;

    private LoginManager loginManager;

    @Before
    public void setUp() {
        loginManager = new LoginManager();
        loginManager.setDao(dao);
    }

    /** Fabrique un client avec un userId VALIDE + mot de passe HASHÉ dans l'objet retourné par la DAO */
    private Client makeClient(String userId, String rawPwd) throws Exception {
        // IDs valides : une lettre, un point, des lettres, au moins un chiffre non nul
        // Exemple correct : "j.doe1"
        Client c = new Client("Nom", "Prenom", "Adresse", true, userId, "ignored", "1234567890");
        c.setUserPwd(PasswordHasher.hashPassword(rawPwd)); // on stocke le hash comme en BDD
        return c;
    }

    /** Fabrique un gestionnaire avec mot de passe HASHÉ dans l'objet retourné par la DAO */
    private Gestionnaire makeManager(String userId, String rawPwd) throws Exception {
        Gestionnaire g = new Gestionnaire("Boss", "Man", "Siege", true, userId, "ignored");
        g.setUserPwd(PasswordHasher.hashPassword(rawPwd));
        return g;
    }

    @Test
    public void testTryLogin_client_success() throws Exception {
        Client userFromDb = makeClient("j.doe1", "secret");
        when(dao.getUserById("j.doe1")).thenReturn(userFromDb);

        int res = loginManager.tryLogin("j.doe1", "secret");

        assertEquals(LoginConstants.USER_IS_CONNECTED, res);
        assertSame(userFromDb, loginManager.getConnectedUser());
    }

    @Test
    public void testTryLogin_manager_success() throws Exception {
        Gestionnaire mgr = makeManager("g.manager1", "adminpass");
        when(dao.getUserById("g.manager1")).thenReturn(mgr);

        int res = loginManager.tryLogin("g.manager1", "adminpass");

        assertEquals(LoginConstants.MANAGER_IS_CONNECTED, res);
        assertSame(mgr, loginManager.getConnectedUser());
    }

    @Test
    public void testTryLogin_wrong_password_fails() throws Exception {
        Client userFromDb = makeClient("j.doe1", "secret");
        when(dao.getUserById("j.doe1")).thenReturn(userFromDb);

        int res = loginManager.tryLogin("j.doe1", "badpw");

        assertEquals(LoginConstants.LOGIN_FAILED, res);
        assertNull(loginManager.getConnectedUser());
    }

    @Test
    public void testTryLogin_unknown_user_fails() {
        when(dao.getUserById("a.unknown1")).thenReturn(null);

        int res = loginManager.tryLogin("a.unknown1", "whatever");

        assertEquals(LoginConstants.LOGIN_FAILED, res);
        assertNull(loginManager.getConnectedUser());
    }

    @Test
    public void testGetConnectedUser_and_logout() throws Exception {
        Client c = makeClient("j.doe1", "pw");
        loginManager.setCurrentUser(c);
        assertSame(c, loginManager.getConnectedUser());

        loginManager.logout();

        assertNull(loginManager.getConnectedUser());
        verify(dao, times(1)).disconnect();
    }

    @Test
    public void testSetCurrentUser_direct() throws Exception {
        Client c = makeClient("a.smith2", "pw");
        loginManager.setCurrentUser(c);
        assertSame(c, loginManager.getConnectedUser());
    }
}
