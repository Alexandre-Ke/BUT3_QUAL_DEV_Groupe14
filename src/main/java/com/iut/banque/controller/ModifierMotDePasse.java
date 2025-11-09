package com.iut.banque.controller;

import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.facade.BanqueFacade;
import com.iut.banque.modele.Utilisateur;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ModifierMotDePasse extends ActionSupport {

    private static final long serialVersionUID = 1L;

    // Constantes Sonar pour les retours d'action
    private static final String RESULT_ERROR = "error";
    private static final String RESULT_SUCCESS = "success";
    private static final String RESULT_LOGIN = "login";

    private String oldPwd;
    private String newPwd;
    private String newPwdConfirm;

    // banque non sérialisable
    private transient BanqueFacade banque;

    public ModifierMotDePasse() {
        ApplicationContext context = WebApplicationContextUtils
                .getRequiredWebApplicationContext(ServletActionContext.getServletContext());
        this.banque = (BanqueFacade) context.getBean("banqueFacade");
    }

    public String getOldPwd() {
        return oldPwd;
    }

    public void setOldPwd(String oldPwd) {
        this.oldPwd = oldPwd;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public void setNewPwd(String newPwd) {
        this.newPwd = newPwd;
    }

    public String getNewPwdConfirm() {
        return newPwdConfirm;
    }

    public void setNewPwdConfirm(String newPwdConfirm) {
        this.newPwdConfirm = newPwdConfirm;
    }

    public Utilisateur getConnectedUser() {
        return banque.getConnectedUser();
    }

    // Struts appelle ces setters même s'ils sont inutiles
    // Sonar exige un commentaire expliquant pourquoi ils sont vides

    /** Setter appelé automatiquement par Struts, non utilisé volontairement. */
    public void setUserIdDisplay(String userIdDisplay) { }

    /** Setter appelé automatiquement par Struts, non utilisé volontairement. */
    public void setSubmit(String submit) { }

    /** Setter appelé par Struts pour le token CSRF, non utilisé volontairement. */
    public void setToken(String token) { }

    @Override
    public String execute() {
        Utilisateur user = banque.getConnectedUser();
        if (user == null) {
            return RESULT_LOGIN;
        }

        if (oldPwd == null || oldPwd.isEmpty() || newPwd == null || newPwd.isEmpty()) {
            addActionError("Tous les champs sont obligatoires.");
            return RESULT_ERROR;
        }

        if (oldPwd.equals(newPwd)) {
            addActionError("Le nouveau mot de passe ne peut pas être identique à l'ancien.");
            return RESULT_ERROR;
        }

        if (!newPwd.equals(newPwdConfirm)) {
            addActionError("Le nouveau mot de passe et la confirmation ne correspondent pas.");
            return RESULT_ERROR;
        }

        try {
            banque.updatePassword(user, oldPwd, newPwd);
            addActionMessage("Votre mot de passe a été modifié avec succès.");
            return RESULT_SUCCESS;
        } catch (IllegalOperationException e) {
            addActionError(e.getMessage());
            return RESULT_ERROR;
        } catch (Exception e) {
            addActionError("Une erreur technique est survenue.");
            return RESULT_ERROR;
        }
    }
}
