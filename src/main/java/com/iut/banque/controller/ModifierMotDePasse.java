package com.iut.banque.controller;

import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.facade.BanqueFacade;
import com.iut.banque.modele.Utilisateur;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ModifierMotDePasse extends ActionSupport {

    private String oldPwd;
    private String newPwd;
    private String newPwdConfirm;
    private final BanqueFacade banque;

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

    // Dummy setters to prevent Struts warnings
    public void setUserIdDisplay(String userIdDisplay) { }
    public void setSubmit(String submit) { }
    public void setToken(String token) { }

    public String execute() {
        Utilisateur user = banque.getConnectedUser();
        if (user == null) {
            return "login";
        }

        if (oldPwd == null || oldPwd.isEmpty() || newPwd == null || newPwd.isEmpty()) {
            addActionError("Tous les champs sont obligatoires.");
            return "error";
        }

        if (oldPwd.equals(newPwd)) {
            addActionError("Le nouveau mot de passe ne peut pas être identique à l'ancien.");
            return "error";
        }

        if (!newPwd.equals(newPwdConfirm)) {
            addActionError("Le nouveau mot de passe et la confirmation ne correspondent pas.");
            return "error";
        }

        try {
            banque.updatePassword(user, oldPwd, newPwd);
            addActionMessage("Votre mot de passe a été modifié avec succès.");
            return "success";
        } catch (IllegalOperationException e) {
            addActionError(e.getMessage());
            return "error";
        } catch (Exception e) {
            e.printStackTrace();
            addActionError("Une erreur technique est survenue.");
            return "error";
        }
    }
}
