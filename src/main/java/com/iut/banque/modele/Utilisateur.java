package com.iut.banque.modele;

import javax.persistence.*;

import com.iut.banque.cryptage.PasswordHasher;
// keep the throws on setUserId for subclasses (e.g., Client) that validate the format
import com.iut.banque.exceptions.IllegalFormatException;

/**
 * Classe représentant un utilisateur quelconque.
 */
@Entity
@Table(name = "Utilisateur")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 15)
public abstract class Utilisateur {

	@Id
	@Column(name = "userId")
	private String userId;

	@Column(name = "userPwd")
	private String userPwd;

	@Column(name = "nom")
	private String nom;

	@Column(name = "prenom")
	private String prenom;

	@Column(name = "adresse")
	private String adresse;

	@Column(name = "male")
	private boolean male;

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public String getUserId() {
		return userId;
	}

	/**
	 * Déclaré avec throws pour permettre aux sous-classes (ex: Client) de
	 * surcharger en levant IllegalFormatException.
	 */
	@SuppressWarnings("java:S1130") // méthode concrète ne lève pas l’exception ici
	public void setUserId(String userId) throws IllegalFormatException {
		this.userId = userId;
	}

	public String getUserPwd() {
		return userPwd;
	}

	public void setUserPwd(String userPwd) {
		if (userPwd == null) {
			this.userPwd = null;
			return;
		}
		if (userPwd.length() == 64 && userPwd.matches("[0-9a-f]{64}")) {
			this.userPwd = userPwd;
		} else {
			this.userPwd = PasswordHasher.hashPassword(userPwd);
		}
	}

	/** Constructeur avec paramètres (visibilité réduite) */
	protected Utilisateur(String nom, String prenom, String adresse, boolean male, String userId, String userPwd) {
		this.nom = nom;
		this.prenom = prenom;
		this.adresse = adresse;
		this.male = male;
		this.userId = userId;
		this.userPwd = userPwd;
	}

	/** Constructeur sans paramètre requis par Hibernate (visibilité réduite) */
	protected Utilisateur() {
		// no-op
	}

	@Override
	public String toString() {
		return "Utilisateur [userId=" + userId + ", nom=" + nom + ", prenom=" + prenom + ", adresse=" + adresse
				+ ", male=" + male + ", userPwd=" + userPwd + "]";
	}
}
