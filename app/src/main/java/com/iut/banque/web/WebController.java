package com.iut.banque.web;

import com.iut.banque.constants.LoginConstants;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.facade.LoginManager;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.Utilisateur;
import com.iut.banque.service.BanqueService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Contrôleur pour les pages web (Thymeleaf)
 */
@Controller
public class WebController {

    private final BanqueService banqueService;
    private final LoginManager loginManager;

    public WebController(BanqueService banqueService, LoginManager loginManager) {
        this.banqueService = banqueService;
        this.loginManager = loginManager;
    }

    // ============ HELPERS ============

    private boolean isClient(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        return userType != null && userType.toLowerCase().contains("client");
    }

    private boolean isManager(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (userType == null) {
            return false;
        }
        String normalized = userType.toLowerCase();
        return normalized.contains("gestionnaire") || normalized.contains("manager");
    }

    private String requireUserId(HttpSession session) {
        return (String) session.getAttribute("userId");
    }

    // ============ LOGIN ============

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/accounts";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/accounts";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String userId,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        try {
            int loginCode = loginManager.tryLogin(userId, password);
            if (loginCode == LoginConstants.USER_IS_CONNECTED || loginCode == LoginConstants.MANAGER_IS_CONNECTED) {
                Utilisateur user = banqueService.getUserById(userId);
                session.setAttribute("userId", userId);
                session.setAttribute("userName", user.getPrenom() + " " + user.getNom());
                session.setAttribute("userType", user.getClass().getSimpleName());
                return "redirect:/accounts";
            }
        } catch (Exception ignored) {
        }
        model.addAttribute("error", "Identifiant ou mot de passe incorrect");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null && !isManager(session)) {
            return "redirect:/accounts";
        }
        model.addAttribute("allowManagerCreation", isManager(session));
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String type,
            @RequestParam String userId,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String adresse,
            @RequestParam(defaultValue = "true") boolean homme,
            @RequestParam(required = false) String numeroClient,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean managerCreationAllowed = isManager(session);
        String normalizedType = managerCreationAllowed && type != null
                ? type.trim().toUpperCase()
                : "CLIENT";
        model.addAttribute("allowManagerCreation", managerCreationAllowed);

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "La confirmation du mot de passe est invalide");
            return "register";
        }
        if (password.length() < 8) {
            model.addAttribute("error", "Le mot de passe doit contenir au moins 8 caractères");
            return "register";
        }

        try {
            if ("GESTIONNAIRE".equals(normalizedType)) {
                banqueService.createManager(nom, prenom, adresse, homme, userId, password);
            } else {
                if (numeroClient == null || numeroClient.isBlank()) {
                    model.addAttribute("error", "Le numéro client est requis pour un compte client");
                    return "register";
                }
                banqueService.createClient(nom, prenom, adresse, homme, userId, password, numeroClient.trim());
            }
            if (managerCreationAllowed) {
                redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès");
                return "redirect:/accounts";
            }
            model.addAttribute("success", "Compte créé avec succès. Connectez-vous.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/password")
    public String changePasswordPage(HttpSession session, Model model) {
        String userId = requireUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userId", userId);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "password-change";
    }

    @PostMapping("/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userId = requireUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "La confirmation du nouveau mot de passe est invalide");
            return "redirect:/password";
        }
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Le nouveau mot de passe doit contenir au moins 8 caractères");
            return "redirect:/password";
        }
        if (newPassword.equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("error", "Le nouveau mot de passe doit être différent de l'ancien");
            return "redirect:/password";
        }

        try {
            loginManager.changePassword(userId, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié avec succès");
            return "redirect:/accounts";
        } catch (IllegalOperationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur technique: " + e.getMessage());
            return "redirect:/password";
        }
    }

    // ============ COMPTES ============

    @GetMapping("/accounts")
    public String listAccounts(
            @RequestParam(required = false) String ownerUserId,
            HttpSession session,
            Model model) {
        String userId = requireUserId(session);
        if (userId == null)
            return "redirect:/login";

        try {
            boolean client = isClient(session);
            List<Compte> accounts;
            String normalizedOwnerId = ownerUserId == null ? null : ownerUserId.trim();

            if (client) {
                accounts = banqueService.listAccountsForUser(userId);
            } else if (normalizedOwnerId != null && !normalizedOwnerId.isBlank()) {
                accounts = banqueService.listAccountsForUser(normalizedOwnerId);
                try {
                    Utilisateur selectedUser = banqueService.getUserById(normalizedOwnerId);
                    model.addAttribute("selectedFilterUser", selectedUser);
                } catch (IllegalOperationException e) {
                    model.addAttribute("warning", "Utilisateur filtré introuvable");
                }
            } else {
                accounts = banqueService.listAccounts();
            }

            model.addAttribute("accounts", accounts);
            model.addAttribute("userId", userId);
            model.addAttribute("userName", session.getAttribute("userName"));
            model.addAttribute("userType", session.getAttribute("userType"));
            model.addAttribute("isManager", !client);
            model.addAttribute("selectedOwnerUserId", normalizedOwnerId);
            if (!client) {
                List<Client> clients = banqueService.listClients();
                model.addAttribute("filterUsers", clients == null ? List.of() : clients);
            }
            return "accounts";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des comptes");
            return "accounts";
        }
    }

    @GetMapping("/accounts/{accountId}")
    public String viewAccount(
            @PathVariable String accountId,
            HttpSession session,
            Model model) {
        String userId = requireUserId(session);
        if (userId == null)
            return "redirect:/login";

        try {
            Compte compte = banqueService.getAccountById(accountId);

            if (isClient(session)) {
                if (compte.getOwner() == null || !userId.equals(compte.getOwner().getUserId())) {
                    model.addAttribute("error", "Accès refusé");
                    return "account-detail";
                }
            }

            model.addAttribute("compte", compte);
            model.addAttribute("userId", userId);
            model.addAttribute("userName", session.getAttribute("userName"));
            model.addAttribute("canManageOperations", isManager(session));
            return "account-detail";
        } catch (IllegalOperationException e) {
            model.addAttribute("error", "Compte non trouvé");
            return "account-detail";
        }
    }

    @GetMapping("/accounts/new")
    public String createAccountPage(HttpSession session, Model model) {
        String userId = requireUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (!isManager(session)) {
            return "redirect:/accounts";
        }

        model.addAttribute("userId", userId);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userType", session.getAttribute("userType"));

        return "account-new";
    }

    @PostMapping("/accounts/new")
    public String createAccount(
            @RequestParam String numeroCompte,
            @RequestParam(required = false) String clientUserId,
            @RequestParam(required = false) String decouvertAutorise,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String sessionUserId = requireUserId(session);
        if (sessionUserId == null)
            return "redirect:/login";
        if (!isManager(session)) {
            redirectAttributes.addFlashAttribute("error", "Action réservée aux gestionnaires");
            return "redirect:/accounts";
        }

        // si clientUserId non fourni, utiliser l'utilisateur connecté
        String ownerId = (clientUserId == null || clientUserId.isBlank())
                ? sessionUserId
                : clientUserId.trim();

        try {
            if (!Compte.checkFormatNumeroCompte(numeroCompte)) {
                redirectAttributes.addFlashAttribute("error",
                        "Le numéro de compte ne respecte pas le format attendu (ex: FR0123456789)");
                return "redirect:/accounts/new";
            }

            if (decouvertAutorise == null || decouvertAutorise.isBlank()) {
                banqueService.createAccountSansDecouvert(numeroCompte, ownerId);
            } else {
                double dec = Double.parseDouble(decouvertAutorise.replace(',', '.'));
                banqueService.createAccountAvecDecouvert(numeroCompte, ownerId, dec);
            }

            redirectAttributes.addFlashAttribute("success", "Compte créé : " + numeroCompte);
            return "redirect:/accounts";
        } catch (IllegalOperationException | IllegalFormatException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/new";
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Format du découvert invalide");
            return "redirect:/accounts/new";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur technique: " + e.getMessage());
            return "redirect:/accounts/new";
        }
    }

    // ============ OPÉRATIONS SUR LES COMPTES ============

    @PostMapping("/accounts/{accountId}/deposit")
    public String deposit(
            @PathVariable String accountId,
            @RequestParam Double amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userId = requireUserId(session);
        if (userId == null)
            return "redirect:/login";
        if (!isManager(session)) {
            redirectAttributes.addFlashAttribute("error", "Action réservée aux gestionnaires");
            return "redirect:/accounts";
        }

        try {
            banqueService.crediter(accountId, amount);
            redirectAttributes.addFlashAttribute("success", "Dépôt de " + amount + " € effectué avec succès");
            return "redirect:/accounts/" + accountId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/" + accountId;
        }
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public String withdraw(
            @PathVariable String accountId,
            @RequestParam Double amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userId = requireUserId(session);
        if (userId == null)
            return "redirect:/login";
        if (!isManager(session)) {
            redirectAttributes.addFlashAttribute("error", "Action réservée aux gestionnaires");
            return "redirect:/accounts";
        }

        try {
            banqueService.debiter(accountId, amount);
            redirectAttributes.addFlashAttribute("success", "Retrait de " + amount + " € effectué avec succès");
            return "redirect:/accounts/" + accountId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/" + accountId;
        }
    }

    @GetMapping("/transfer")
    public String transferPage(HttpSession session, Model model) {
        String userId = requireUserId(session);
        if (userId == null)
            return "redirect:/login";

        try {
            List<Compte> accounts = isClient(session)
                    ? banqueService.listAccountsForUser(userId)
                    : banqueService.listAccounts();

            model.addAttribute("accounts", accounts);
            model.addAttribute("userId", userId);
            model.addAttribute("userName", session.getAttribute("userName"));
            return "transfer";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des comptes");
            return "transfer";
        }
    }

    @PostMapping("/transfer")
    public String transfer(
            @RequestParam String fromAccountId,
            @RequestParam String toAccountId,
            @RequestParam Double amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userId = requireUserId(session);
        if (userId == null)
            return "redirect:/login";

        if (isClient(session)) {
            try {
                Compte from = banqueService.getAccountById(fromAccountId);
                Compte to = banqueService.getAccountById(toAccountId);
                if (from.getOwner() == null || !userId.equals(from.getOwner().getUserId())) {
                    redirectAttributes.addFlashAttribute("error", "Accès refusé");
                    return "redirect:/transfer";
                }
                if (to.getOwner() == null || !userId.equals(to.getOwner().getUserId())) {
                    redirectAttributes.addFlashAttribute("error", "Le compte destinataire doit vous appartenir");
                    return "redirect:/transfer";
                }
            } catch (IllegalOperationException ex) {
                redirectAttributes.addFlashAttribute("error", "Compte introuvable");
                return "redirect:/transfer";
            }
        }

        try {
            banqueService.transfer(fromAccountId, toAccountId, amount);
            redirectAttributes.addFlashAttribute("success", "Virement de " + amount + " € effectué avec succès");
            return "redirect:/accounts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/transfer";
        }
    }
}
