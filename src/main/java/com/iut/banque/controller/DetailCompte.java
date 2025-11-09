package com.iut.banque.controller;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.InsufficientFundsException;
import com.iut.banque.facade.BanqueFacade;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.Gestionnaire;
import com.opensymphony.xwork2.ActionSupport;

public class DetailCompte extends ActionSupport {

	private static final long serialVersionUID = 1L;

	// --- Constantes pour éviter les littéraux dupliqués
	private static final String SUCCESS = "SUCCESS";
	private static final String ERROR = "ERROR";
	private static final String NOT_ENOUGH_FUNDS = "NOTENOUGHFUNDS";
	private static final String NEGATIVE_AMOUNT = "NEGATIVEAMOUNT";
	private static final String NEGATIVE_OVERDRAFT = "NEGATIVEOVERDRAFT";
	private static final String INCOMPATIBLE_OVERDRAFT = "INCOMPATIBLEOVERDRAFT";
	private static final String TECHNICAL = "TECHNICAL";
	private static final String BUSINESS = "BUSINESS";

	private static final Logger LOGGER = LoggerFactory.getLogger(DetailCompte.class);

	// Non sérialisables -> transient pour les actions Struts
	protected transient BanqueFacade banque;
	protected transient Compte compte;

	private String montant;
	private String error;

	public DetailCompte() {
		LOGGER.debug("Construction DetailCompte");
		ApplicationContext context = WebApplicationContextUtils
				.getRequiredWebApplicationContext(ServletActionContext.getServletContext());
		this.banque = (BanqueFacade) context.getBean("banqueFacade");
	}

	public String getError() {
		if (error == null) {
			return "";
		}
		switch (error) {
			case TECHNICAL:
				return "Erreur interne. Verifiez votre saisie puis réessayer. Contactez votre conseiller si le problème persiste.";
			case BUSINESS:
				return "Fonds insuffisants.";
			case NEGATIVE_AMOUNT:
				return "Veuillez rentrer un montant positif.";
			case NEGATIVE_OVERDRAFT:
				return "Veuillez rentrer un découvert positif.";
			case INCOMPATIBLE_OVERDRAFT:
				return "Le nouveau découvert est incompatible avec le solde actuel.";
			default:
				return "";
		}
	}

	public void setError(String error) {
		this.error = (error == null) ? "" : error;
	}

	public String getMontant() {
		return montant;
	}

	public void setMontant(String montant) {
		this.montant = montant;
	}

	/**
	 * Retourne le compte s’il appartient au client connecté (ou si un gestionnaire est connecté)
	 */
	public Compte getCompte() {
		if (banque.getConnectedUser() instanceof Gestionnaire) {
			return compte;
		}
		if (banque.getConnectedUser() instanceof Client
				&& compte != null
				&& ((Client) banque.getConnectedUser()).getAccounts().containsKey(compte.getNumeroCompte())) {
			return compte;
		}
		return null;
	}

	public void setCompte(Compte compte) {
		this.compte = compte;
	}

	/** Débiter le compte */
	public String debit() {
		final Compte selectedCompte = getCompte();
		if (selectedCompte == null) {
			return ERROR;
		}
		try {
			banque.debiter(selectedCompte, Double.parseDouble(montant.trim()));
			return SUCCESS;
		} catch (NumberFormatException e) {
			LOGGER.warn("Montant invalide pour debit: '{}'", montant, e);
			return ERROR;
		} catch (InsufficientFundsException e) {
			LOGGER.info("Fonds insuffisants pour {}", selectedCompte.getNumeroCompte(), e);
			return NOT_ENOUGH_FUNDS;
		} catch (IllegalFormatException e) {
			LOGGER.info("Montant négatif pour debit: '{}'", montant, e);
			return NEGATIVE_AMOUNT;
		}
	}

	/** Créditer le compte */
	public String credit() {
		final Compte selectedCompte = getCompte();
		if (selectedCompte == null) {
			return ERROR;
		}
		try {
			banque.crediter(selectedCompte, Double.parseDouble(montant.trim()));
			return SUCCESS;
		} catch (NumberFormatException e) {
			LOGGER.warn("Montant invalide pour credit: '{}'", montant, e);
			return ERROR;
		} catch (IllegalFormatException e) {
			LOGGER.info("Montant négatif pour credit: '{}'", montant, e);
			return NEGATIVE_AMOUNT;
		}
	}
}
