package com.iut.banque.test.facade;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.iut.banque.cryptage.PasswordHasher;
import com.iut.banque.modele.Utilisateur;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.facade.BanqueManager;

//@RunWith indique à JUnit de prendre le class runner de Spirng
@RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration permet de charger le context utilisé pendant les tests.
// Par défault (si aucun argument n'est précisé), cherche le fichier
/// src/com/iut/banque/test/TestsDaoHibernate-context.xml
@ContextConfiguration("/test/resources/TestsBanqueManager-context.xml")
@Transactional("transactionManager")
public class TestsBanqueManager {

	@Autowired
	private BanqueManager bm;

	// Tests de par rapport à l'ajout d'un client
	@Test
	public void TestCreationDunClient() {
		try {
			bm.loadAllClients();
			bm.createClient("t.test1", "password", "test1nom", "test1prenom", "test town", true, "4242424242");
		} catch (IllegalOperationException e) {
			e.printStackTrace();
			fail("IllegalOperationException récupérée : " + e.getStackTrace());
		} catch (Exception te) {
			te.printStackTrace();
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	@Test
	public void TestCreationDunClientAvecDeuxNumerosDeCompteIdentiques() {
		try {
			bm.loadAllClients();
			bm.createClient("t.test1", "password", "test1nom", "test1prenom", "test town", true, "0101010101");
			fail();
		} catch (IllegalOperationException e) {
		} catch (Exception te) {
			te.printStackTrace();
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	// Tests par rapport à la suppression de comptes
	@Test
	public void TestSuppressionDunCompteAvecDecouvertAvecSoldeZero() {
		try {

			bm.deleteAccount(bm.getAccountById("CADV000000"));
		} catch (IllegalOperationException e) {
			e.printStackTrace();
			fail("IllegalOperationException récupérée : " + e.getStackTrace());
		} catch (Exception te) {
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	@Test
	public void TestSuppressionDunCompteAvecDecouvertAvecSoldeDifferentDeZero() {
		try {
			bm.deleteAccount(bm.getAccountById("CADNV00000"));
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException e) {
		} catch (Exception te) {
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	@Test
	public void TestSuppressionDunCompteSansDecouvertAvecSoldeZero() {
		try {
			bm.deleteAccount(bm.getAccountById("CSDV000000"));
		} catch (IllegalOperationException e) {
			e.printStackTrace();
			fail("IllegalOperationException récupérée : " + e.getStackTrace());
		} catch (Exception te) {
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	@Test
	public void TestSuppressionDunCompteSansDecouvertAvecSoldeDifferentDeZero() {
		try {
			bm.deleteAccount(bm.getAccountById("CSDNV00000"));
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException e) {
		} catch (Exception te) {
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	// Tests en rapport avec la suppression d'utilisateurs
	@Test
	public void TestSuppressionDunUtilisateurSansCompte() {
		try {
			bm.loadAllClients();
			bm.deleteUser(bm.getUserById("g.pasdecompte"));
		} catch (IllegalOperationException e) {
			e.printStackTrace();
			fail("IllegalOperationException récupérée : " + e.getStackTrace());
		} catch (Exception te) {
			te.printStackTrace();
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	@Test
	public void TestSuppressionDuDernierManagerDeLaBaseDeDonnees() {
		bm.loadAllGestionnaires();
		try {
			bm.deleteUser(bm.getUserById("admin"));
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException e) {
		} catch (Exception te) {
			te.printStackTrace();
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	@Test
	public void TestSuppressionDunClientAvecComptesDeSoldeZero() {
		try {
			bm.loadAllClients();
			bm.deleteUser(bm.getUserById("g.descomptesvides"));
			if (bm.getAccountById("KL4589219196") != null || bm.getAccountById("KO7845154956") != null) {
				fail("Les comptes de l'utilisateur sont encore présents dans la base de données");
			}
		} catch (IllegalOperationException e) {
			e.printStackTrace();
			fail("IllegalOperationException récupérée : " + e.getStackTrace());
		} catch (Exception te) {
			te.printStackTrace();
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	@Test
	public void TestSuppressionDunClientAvecUnCompteDeSoldePositif() {
		try {
			bm.deleteUser(bm.getUserById("j.doe1"));
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException e) {
		} catch (Exception te) {
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	@Test
	public void TestSuppressionDunClientAvecUnCompteAvecDecouvertDeSoldeNegatif() {
		try {
			bm.deleteUser(bm.getUserById("j.doe1"));
			fail("Une IllegalOperationException aurait dû être récupérée");
		} catch (IllegalOperationException e) {
		} catch (Exception te) {
			fail("Une Exception " + te.getClass().getSimpleName() + " a été récupérée");
		}
	}

	// Tests en rapport avec la modification du mot de passe
	@Test
	public void TestModificationMotDePasseSucces() {
		try {
			bm.loadAllClients();
			String password = "password";
			String hashedPassword = PasswordHasher.hashPassword(password);
			bm.createClient("t.pwdsucces1", hashedPassword, "Test", "User", "Test Address", true, "1111111111");
			Utilisateur user = bm.getUserById("t.pwdsucces1");
			bm.updatePassword(user, password, "newPassword");
			Utilisateur updatedUser = bm.getUserById("t.pwdsucces1");
			if (!PasswordHasher.verifyPassword("newPassword", updatedUser.getUserPwd())) {
				fail("Le nouveau mot de passe n'a pas été correctement mis à jour dans la base de données.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Une exception a été levée lors de la modification du mot de passe : " + e.getMessage());
		}
	}

	@Test
	public void TestModificationMotDePasseAncienMotDePasseIncorrect() {
		try {
			bm.loadAllClients();
			String password = "password";
			String hashedPassword = PasswordHasher.hashPassword(password);
			bm.createClient("t.pwdincorrect1", hashedPassword, "Test", "User", "Test Address", true, "2222222222");
			Utilisateur user = bm.getUserById("t.pwdincorrect1");
			bm.updatePassword(user, "wrongPassword", "newPassword");
			fail("Une IllegalOperationException aurait dû être levée.");
		} catch (IllegalOperationException e) {
			// Test réussi
		} catch (Exception e) {
			e.printStackTrace();
			fail("Une exception inattendue a été levée : " + e.getClass().getSimpleName());
		}
	}

	// Tests en rapport avec la réinitialisation du mot de passe
	@Test
	public void TestRechercherUtilisateurPourReinitialisationSucces() {
		try {
			bm.loadAllClients();
			bm.createClient("j.reinitsucces1", "password", "Doe", "John", "123 Main St", true, "9999999999");
			Utilisateur user = bm.rechercherUtilisateurPourReinitialisation("j.reinitsucces1", "Doe", "John", "9999999999");
			assertNotNull("L'utilisateur aurait dû être trouvé.", user);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Une exception inattendue a été levée : " + e.getMessage());
		}
	}

	@Test
	public void TestRechercherUtilisateurPourReinitialisationEchec() {
		try {
			bm.loadAllClients();
			bm.createClient("j.reinitechec1", "password", "Doe", "John", "123 Main St", true, "8888888888");
			Utilisateur user = bm.rechercherUtilisateurPourReinitialisation("j.reinitechec1", "Doe", "Jane", "8888888888");
			assertNull("L'utilisateur n'aurait pas dû être trouvé.", user);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Une exception inattendue a été levée : " + e.getMessage());
		}
	}

	@Test
	public void TestReinitialiserLeMotDePasseSucces() {
		try {
			bm.loadAllClients();
			bm.createClient("j.reinitpwd1", "password", "Doe", "John", "123 Main St", true, "7777777777");
			Utilisateur user = bm.getUserById("j.reinitpwd1");
			bm.reinitialiserLeMotDePasse(user, "nouveauMotDePasseSuperSecret");
			Utilisateur updatedUser = bm.getUserById("j.reinitpwd1");
			if (!PasswordHasher.verifyPassword("nouveauMotDePasseSuperSecret", updatedUser.getUserPwd())) {
				fail("Le mot de passe n'a pas été correctement réinitialisé.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Une exception inattendue a été levée : " + e.getMessage());
		}
	}
}
