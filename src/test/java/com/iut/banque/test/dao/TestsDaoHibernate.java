package com.iut.banque.test.dao;

import com.iut.banque.dao.DaoHibernate;
import com.iut.banque.exceptions.*;
import com.iut.banque.modele.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestsDaoHibernate {

	private DaoHibernate dao;
	private SessionFactory sessionFactory;
	private Session session;

	@Before
	public void setup() {
		dao = new DaoHibernate();
		sessionFactory = mock(SessionFactory.class);
		session = mock(Session.class);

		dao.setSessionFactory(sessionFactory);

		when(sessionFactory.getCurrentSession()).thenReturn(session);
		when(sessionFactory.openSession()).thenReturn(session);
	}

	@Test
	public void testCreateUser_Manager() throws Exception {
		String userId = "manager1";
		String pwd = "PASS";

		when(session.get(Utilisateur.class, userId)).thenReturn(null);

		Gestionnaire expected = new Gestionnaire(
				"NOM", "PRENOM", "ADRESSE", true, userId, pwd
		);

		Utilisateur created = dao.createUser(
				"NOM", "PRENOM", "ADRESSE", true, userId, pwd, true, null
		);

		assertEquals(Gestionnaire.class, created.getClass());
		verify(session).save(any(Gestionnaire.class));
	}

	@Test
	public void testCreateUser_Client() throws Exception {
		String userId = "p.n1";
		String pwd = "PASS";

		when(session.get(Utilisateur.class, userId)).thenReturn(null);

		Client expected = new Client(
				"NOM", "PRENOM", "ADRESSE", true, userId, pwd, "1111111111"
		);

		Utilisateur created = dao.createUser(
				"NOM", "PRENOM", "ADRESSE", true, userId, pwd, false, "1111111111"
		);

		assertEquals(Client.class, created.getClass());
		verify(session).save(any(Client.class));
	}

	@Test
	public void testDeleteUser() throws Exception {
		Client u = new Client("NOM", "PRENOM", "ADR", true, "p.j2", "PWD", "1111111111");

		dao.deleteUser(u);
		verify(session).delete(u);
	}

	@Test
	public void testGetUserById() throws IllegalFormatException {
		Client c = new Client("N", "P", "A", true, "p.j2", "PWD", "1111111111");
		when(session.get(Utilisateur.class, "id")).thenReturn(c);

		Utilisateur u = dao.getUserById("id");
		assertNotNull(u);
		assertEquals("p.j2", u.getUserId());
	}
}
