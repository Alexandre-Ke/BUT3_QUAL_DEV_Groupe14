package com.iut.banque.controller;

import com.iut.banque.facade.BanqueFacade;
import com.iut.banque.modele.Utilisateur;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.Map;

public class ReinitialiserMotDePasseController extends ActionSupport {

    private String userId;
    private String nom;
    private String prenom;
    private String numeroClient;
    private String nouveauMotDePasse;
    private String confirmationMotDePasse;
    private String message;
    private BanqueFacade banque;

    public ReinitialiserMotDePasseController() {
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(ServletActionContext.getServletContext());
        this.banque = (BanqueFacade) context.getBean("banqueFacade");
    }

    public String etape1() {
        return "etape1";
    }

    public String verifierIdentite() {
        Utilisateur user = banque.rechercherUtilisateurPourReinitialisation(userId, nom, prenom, numeroClient);
        if (user != null) {
            Map<String, Object> session = ActionContext.getContext().getSession();
            session.put("userIdPourReinitialisation", user.getUserId());
            return "etape2";
        } else {
            message = "Les informations fournies ne correspondent à aucun utilisateur.";
            return "etape1";
        }
    }

    public String changerMotDePasse() {
        Map<String, Object> session = ActionContext.getContext().getSession();
        String userIdPourReinitialisation = (String) session.get("userIdPourReinitialisation");

        if (userIdPourReinitialisation == null) {
            message = "Session expirée ou invalide. Veuillez recommencer.";
            return "etape1";
        }

        if (nouveauMotDePasse == null || !nouveauMotDePasse.equals(confirmationMotDePasse)) {
            message = "Les mots de passe ne correspondent pas.";
            return "etape2";
        }

        Utilisateur user = banque.getUserById(userIdPourReinitialisation);
        banque.reinitialiserLeMotDePasse(user, nouveauMotDePasse);
        session.remove("userIdPourReinitialisation");
        
        // Mettre un message de succès dans la session pour l'afficher sur la page de login
        Map<String, Object> application = ActionContext.getContext().getApplication();
        application.put("messageReinitialisation", "Votre mot de passe a été réinitialisé avec succès.");

        return SUCCESS;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNumeroClient() { return numeroClient; }
    public void setNumeroClient(String numeroClient) { this.numeroClient = numeroClient; }
    public String getNouveauMotDePasse() { return nouveauMotDePasse; }
    public void setNouveauMotDePasse(String nouveauMotDePasse) { this.nouveauMotDePasse = nouveauMotDePasse; }
    public String getConfirmationMotDePasse() { return confirmationMotDePasse; }
    public void setConfirmationMotDePasse(String confirmationMotDePasse) { this.confirmationMotDePasse = confirmationMotDePasse; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
