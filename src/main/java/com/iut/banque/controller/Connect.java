package com.iut.banque.controller;

import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;          // + logger
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.opensymphony.xwork2.ActionSupport;

import com.iut.banque.constants.LoginConstants;
import com.iut.banque.facade.BanqueFacade;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.Utilisateur;

public class Connect extends ActionSupport {

	private static final long serialVersionUID = 1L;

	// Résultats Struts factorisés (évite la duplication de littéraux)
	private static final String RESULT_SUCCESS = "SUCCESS";
	private static final String RESULT_SUCCESS_MANAGER = "SUCCESSMANAGER";
	private static final String RESULT_ERROR = "ERROR";

	// Logger SLF4J
	private static final Logger LOG = LoggerFactory.getLogger(Connect.class);

	private String userCde;
	private String userPwd;

	// S1948: rendre le champ transient (facade non sérialisable)
	private transient BanqueFacade banque;

	public Connect() {
		LOG.debug("Constructeur Connect");
		ApplicationContext context = WebApplicationContextUtils
				.getRequiredWebApplicationContext(ServletActionContext.getServletContext());
		this.banque = (BanqueFacade) context.getBean("banqueFacade");
	}

	public String login() {
		LOG.info("Tentative de login");

		if (userCde == null || userPwd == null) {
			return RESULT_ERROR;
		}
		userCde = userCde.trim();

		int loginResult;
		try {
			loginResult = banque.tryLogin(userCde, userPwd);
		} catch (Exception e) {
			LOG.error("Erreur lors du login", e);
			loginResult = LoginConstants.ERROR;
		}

		switch (loginResult) {
			case LoginConstants.USER_IS_CONNECTED:
				LOG.info("Utilisateur connecté");
				return RESULT_SUCCESS;
			case LoginConstants.MANAGER_IS_CONNECTED:
				LOG.info("Manager connecté");
				return RESULT_SUCCESS_MANAGER;
			case LoginConstants.LOGIN_FAILED:
				LOG.warn("Échec de connexion");
				return RESULT_ERROR;
			default:
				LOG.error("État de login inconnu: {}", loginResult);
				return RESULT_ERROR;
		}
	}

	public String getUserCde() { return userCde; }
	public void setUserCde(String userCde) { this.userCde = userCde; }

	public String getUserPwd() { return userPwd; }
	public void setUserPwd(String userPwd) { this.userPwd = userPwd; }

	public Utilisateur getConnectedUser() { return banque.getConnectedUser(); }

	public Map<String, Compte> getAccounts() {
		return ((Client) banque.getConnectedUser()).getAccounts();
	}

	public String logout() {
		LOG.info("Déconnexion");
		banque.logout();
		return RESULT_SUCCESS;
	}
}
