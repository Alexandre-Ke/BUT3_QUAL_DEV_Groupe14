package com.iut.banque.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iut.banque.cryptage.PasswordHasher;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.TechnicalException;
import com.iut.banque.interfaces.IDao;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.CompteAvecDecouvert;
import com.iut.banque.modele.CompteSansDecouvert;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.modele.Utilisateur;

@Transactional
public class DaoHibernate implements IDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(DaoHibernate.class);

	private SessionFactory sessionFactory;

	public DaoHibernate() {
		LOGGER.debug("Création de la Dao");
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public CompteAvecDecouvert createCompteAvecDecouvert(double solde, String numeroCompte, double decouvertAutorise,
														 Client client)
			throws TechnicalException, IllegalFormatException, IllegalOperationException {
		Session session = sessionFactory.getCurrentSession();
		CompteAvecDecouvert compte = session.get(CompteAvecDecouvert.class, numeroCompte);
		if (compte != null) {
			throw new TechnicalException("Numéro de compte déjà utilisé.");
		}

		compte = new CompteAvecDecouvert(numeroCompte, solde, decouvertAutorise, client);
		client.addAccount(compte);
		session.save(compte);
		return compte;
	}

	@Override
	public CompteSansDecouvert createCompteSansDecouvert(double solde, String numeroCompte, Client client)
			throws TechnicalException, IllegalFormatException {
		Session session = sessionFactory.getCurrentSession();
		CompteSansDecouvert compte = session.get(CompteSansDecouvert.class, numeroCompte);
		if (compte != null) {
			throw new TechnicalException("Numéro de compte déjà utilisé.");
		}

		compte = new CompteSansDecouvert(numeroCompte, solde, client);
		session.save(compte);
		client.addAccount(compte);
		return compte;
	}

	@Override
	public void updateAccount(Compte c) {
		sessionFactory.getCurrentSession().update(c);
	}

	@Override
	public void deleteAccount(Compte c) throws TechnicalException {
		Session session = sessionFactory.getCurrentSession();
		if (c == null) {
			throw new TechnicalException("Ce compte n'existe plus");
		}
		session.delete(c);
	}

	@Override
	public Map<String, Compte> getAccountsByClientId(String id) {
		Session session = sessionFactory.getCurrentSession();
		Client client = session.get(Client.class, id);
		return (client != null) ? client.getAccounts() : Collections.emptyMap();
	}

	@Override
	public Compte getAccountById(String id) {
		return sessionFactory.getCurrentSession().get(Compte.class, id);
	}

	@Override
	public Utilisateur createUser(String nom, String prenom, String adresse, boolean male, String userId,
								  String userPwd, boolean manager, String numClient)
			throws TechnicalException, IllegalArgumentException, IllegalFormatException {
		Session session = sessionFactory.getCurrentSession();

		Utilisateur existing = session.get(Utilisateur.class, userId);
		if (existing != null) {
			throw new TechnicalException("User Id déjà utilisé.");
		}

		Utilisateur user = manager
				? new Gestionnaire(nom, prenom, adresse, male, userId, userPwd)
				: new Client(nom, prenom, adresse, male, userId, userPwd, numClient);

		session.save(user);
		return user;
	}

	@Override
	public void deleteUser(Utilisateur u) throws TechnicalException {
		Session session = sessionFactory.getCurrentSession();
		if (u == null) {
			throw new TechnicalException("Cet utilisateur n'existe plus");
		}
		session.delete(u);
	}

	@Override
	public void updateUser(Utilisateur u) {
		sessionFactory.getCurrentSession().update(u);
	}

	@Override
	public boolean isUserAllowed(String userId, String userPwd) {
		if (userId == null || userPwd == null) {
			return false;
		}
		userId = userId.trim();
		userPwd = userPwd.trim();
		if (userId.isEmpty() || userPwd.isEmpty()) {
			return false;
		}

		Session session = sessionFactory.openSession();
		try {
			Utilisateur user = session.get(Utilisateur.class, userId);
			if (user == null) {
				return false;
			}
			return PasswordHasher.hashPassword(userPwd).equals(user.getUserPwd().trim());
		} finally {
			session.close();
		}
	}

	@Override
	public Utilisateur getUserById(String id) {
		return sessionFactory.getCurrentSession().get(Utilisateur.class, id);
	}

	@Override
	public Map<String, Client> getAllClients() {
		Session session = sessionFactory.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<Object> res = session.createCriteria(Client.class).list();
		Map<String, Client> ret = new HashMap<>();
		for (Object client : res) {
			ret.put(((Client) client).getUserId(), (Client) client);
		}
		return ret;
	}

	@Override
	public Map<String, Gestionnaire> getAllGestionnaires() {
		Session session = sessionFactory.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<Object> res = session.createCriteria(Gestionnaire.class).list();
		Map<String, Gestionnaire> ret = new HashMap<>();
		for (Object gestionnaire : res) {
			ret.put(((Gestionnaire) gestionnaire).getUserId(), (Gestionnaire) gestionnaire);
		}
		return ret;
	}

	@Override
	public void disconnect() {
		LOGGER.debug("Déconnexion de la DAO.");
	}
}
