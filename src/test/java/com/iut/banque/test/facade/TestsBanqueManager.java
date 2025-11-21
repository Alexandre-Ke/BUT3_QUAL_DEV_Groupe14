package com.iut.banque.test.facade;

import com.iut.banque.cryptage.PasswordHasher;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.TechnicalException;
import com.iut.banque.facade.BanqueManager;
import com.iut.banque.interfaces.IDao;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.CompteAvecDecouvert;
import com.iut.banque.modele.CompteSansDecouvert;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.modele.Utilisateur;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Version mockée de TestsBanqueManager :
 * - plus de Spring
 * - plus de dépendance à la base de données
 * - tests unitaires de BanqueManager avec Mockito.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestsBanqueManager {

	@Mock
	private IDao dao;

	@InjectMocks
	private BanqueManager bm;

	// ----------------------------------------
	// Helpers
	// ----------------------------------------

	private Client createClient(String userId, String numeroClient) throws Exception{
		return new Client("NOM", "PRENOM", "ADRESSE", true, userId, "PASS", numeroClient);
	}

	private CompteSansDecouvert createCompteSansDecouvert(String numero, double solde, Client owner) throws Exception{
		CompteSansDecouvert c = new CompteSansDecouvert(numero, solde, owner);
		owner.addAccount(c);
		return c;
	}

	private CompteAvecDecouvert createCompteAvecDecouvert(String numero, double solde, double decouvert, Client owner) throws Exception{
		CompteAvecDecouvert c = new CompteAvecDecouvert(numero, solde, decouvert, owner);
		owner.addAccount(c);
		return c;
	}

	// ----------------------------------------
	// Tests création de client
	// ----------------------------------------

	@Test
	public void TestCreationDunClient() throws Exception {
		when(dao.getAllClients()).thenReturn(Collections.emptyMap());

		bm.loadAllClients();
		bm.createClient("t.test1", "password", "test1nom", "test1prenom",
				"test town", true, "4242424242");

		verify(dao).getAllClients();
		verify(dao).createUser("test1nom", "test1prenom", "test town",
				true, "t.test1", "password", false, "4242424242");
	}

	@Test
	public void TestCreationDunClientAvecDeuxNumerosDeCompteIdentiques() throws Exception {
		Client existing = createClient("c.exist1", "0101010101");
		Map<String, Client> clients = new HashMap<>();
		clients.put(existing.getUserId(), existing);
		when(dao.getAllClients()).thenReturn(clients);

		bm.loadAllClients();

		try {
			bm.createClient("t.test1", "password", "test1nom", "test1prenom",
					"test town", true, "0101010101");
			fail("Une IllegalOperationException aurait dû être levée");
		} catch (IllegalOperationException ignored) {
		}

		verify(dao, never()).createUser(anyString(), anyString(), anyString(),
				anyBoolean(), anyString(), anyString(), anyBoolean(), anyString());
	}

	// ----------------------------------------
	// Tests suppression de comptes
	// ----------------------------------------

	@Test
	public void TestSuppressionDunCompteAvecDecouvertAvecSoldeZero() throws Exception {
		Client client = createClient("c.cli1", "1111111111");
		CompteAvecDecouvert compte = createCompteAvecDecouvert("CA0000000000", 0.0, 100.0, client);

		doNothing().when(dao).deleteAccount(compte);

		bm.deleteAccount(compte);

		verify(dao).deleteAccount(compte);
	}

	@Test
	public void TestSuppressionDunCompteAvecDecouvertAvecSoldeDifferentDeZero() throws Exception {
		Client client = createClient("c.cli1", "1111111111");
		CompteAvecDecouvert compte = createCompteAvecDecouvert("CA0000000000", 50.0, 100.0, client);

		try {
			bm.deleteAccount(compte);
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException ignored) {
		}

		verify(dao, never()).deleteAccount(any(Compte.class));
	}

	@Test
	public void TestSuppressionDunCompteSansDecouvertAvecSoldeZero() throws Exception {
		Client client = createClient("c.cli1", "1111111111");
		CompteSansDecouvert compte = createCompteSansDecouvert("CA0000000000", 0.0, client);

		doNothing().when(dao).deleteAccount(compte);

		bm.deleteAccount(compte);

		verify(dao).deleteAccount(compte);
	}

	@Test
	public void TestSuppressionDunCompteSansDecouvertAvecSoldeDifferentDeZero() throws Exception {
		Client client = createClient("c.cli1", "1111111111");
		CompteSansDecouvert compte = createCompteSansDecouvert("CA0000000000", 10.0, client);

		try {
			bm.deleteAccount(compte);
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException ignored) {
		}

		verify(dao, never()).deleteAccount(any(Compte.class));
	}

	// ----------------------------------------
	// Tests suppression d'utilisateurs
	// ----------------------------------------

//	@Test
//	public void TestSuppressionDunUtilisateurSansCompte() throws Exception {
//		Client client = createClient("g.pasdecompte1", "5544554455");
//		// pas de comptes => suppression possible
//		doNothing().when(dao).deleteUser(client);
//
//		bm.deleteUser(client);
//
//		verify(dao).deleteUser(client);
//	}

	@Test
	public void TestSuppressionDuDernierManagerDeLaBaseDeDonnees() throws Exception {
		Gestionnaire admin = new Gestionnaire("Smith", "Joe",
				"123, grande rue, Metz", true, "admin", "adminpass");

		Map<String, Gestionnaire> gestionnaires = new HashMap<>();
		gestionnaires.put("admin", admin);
		when(dao.getAllGestionnaires()).thenReturn(gestionnaires);

		bm.loadAllGestionnaires();

		try {
			bm.deleteUser(admin);
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException ignored) {
		}

		verify(dao, never()).deleteUser(admin);
	}

//	@Test
//	public void TestSuppressionDunClientAvecComptesDeSoldeZero() throws Exception {
//		Client client = createClient("g.descomptesvides1", "0000000002");
//		CompteSansDecouvert c1 = createCompteSansDecouvert("KL4589219196", 0.0, client);
//		CompteSansDecouvert c2 = createCompteSansDecouvert("KO7845154956", 0.0, client);
//
//		doNothing().when(dao).deleteAccount(c1);
//		doNothing().when(dao).deleteAccount(c2);
//		doNothing().when(dao).deleteUser(client);
//
//		bm.deleteUser(client);
//
//		verify(dao).deleteAccount(c1);
//		verify(dao).deleteAccount(c2);
//		verify(dao).deleteUser(client);
//	}

	@Test
	public void TestSuppressionDunClientAvecUnCompteDeSoldePositif() throws Exception {
		Client client = createClient("j.doe1", "1111111111");
		CompteSansDecouvert cPos = createCompteSansDecouvert("CA0000000000", 100.0, client);

		try {
			bm.deleteUser(client);
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException ignored) {
		}

		verify(dao, never()).deleteAccount(cPos);
		verify(dao, never()).deleteUser(client);
	}

	@Test
	public void TestSuppressionDunClientAvecUnCompteAvecDecouvertDeSoldeNegatif() throws Exception {
		Client client = createClient("j.doe1", "1234567890");
		CompteAvecDecouvert cNeg = createCompteAvecDecouvert("CA0000000000", -10.0, 100.0, client);

		try {
			bm.deleteUser(client);
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException ignored) {
		}

		verify(dao, never()).deleteAccount(cNeg);
		verify(dao, never()).deleteUser(client);
	}

	// ----------------------------------------
	// Tests modification du mot de passe
	// ----------------------------------------

	@Test
	public void TestModificationMotDePasseSucces() throws Exception {
		String oldPwd = "password";
		String hashedOld = PasswordHasher.hashPassword(oldPwd);

		Client user = createClient("t.pwdsucces1", "1111111111");
		user.setUserPwd(hashedOld);

		doNothing().when(dao).updateUser(user);

		bm.updatePassword(user, oldPwd, "newPassword");

		assertTrue(PasswordHasher.verifyPassword("newPassword", user.getUserPwd()));
		verify(dao).updateUser(user);
	}

	@Test
	public void TestModificationMotDePasseAncienMotDePasseIncorrect() throws Exception {
		String oldPwd = "password";
		String hashedOld = PasswordHasher.hashPassword(oldPwd);

		Client user = createClient("t.pwdincorrect1", "2222222222");
		user.setUserPwd(hashedOld);

		try {
			bm.updatePassword(user, "wrongPassword", "newPassword");
			fail("Une IllegalOperationException aurait dû être levée.");
		} catch (IllegalOperationException ignored) {
		}

		verify(dao, never()).updateUser(user);
	}

	// ----------------------------------------
	// Tests réinitialisation du mot de passe
	// ----------------------------------------

	@Test
	public void TestRechercherUtilisateurPourReinitialisationSucces() throws Exception {
		Client user = createClient("j.reinitsucces1", "9999999999");
		user.setNom("Doe");
		user.setPrenom("John");

		when(dao.getUserById("j.reinitsucces1")).thenReturn(user);

		Utilisateur found = bm.rechercherUtilisateurPourReinitialisation(
				"j.reinitsucces1", "Doe", "John", "9999999999");

		assertNotNull("L'utilisateur aurait dû être trouvé.", found);
	}

	@Test
	public void TestRechercherUtilisateurPourReinitialisationEchec() throws Exception {
		Client user = createClient("j.reinitechec1", "8888888888");
		user.setNom("Doe");
		user.setPrenom("John");

		when(dao.getUserById("j.reinitechec1")).thenReturn(user);

		Utilisateur found = bm.rechercherUtilisateurPourReinitialisation(
				"j.reinitechec1", "Doe", "Jane", "8888888888");

		assertNull("L'utilisateur n'aurait pas dû être trouvé.", found);
	}

	@Test
	public void TestReinitialiserLeMotDePasseSucces() throws Exception {
		Client user = createClient("j.reinitpwd1", "7777777777");
		user.setUserPwd(PasswordHasher.hashPassword("old"));

		doNothing().when(dao).updateUser(user);

		bm.reinitialiserLeMotDePasse(user, "nouveauMotDePasseSuperSecret");

		assertTrue(PasswordHasher.verifyPassword("nouveauMotDePasseSuperSecret", user.getUserPwd()));
		verify(dao).updateUser(user);
	}
}
