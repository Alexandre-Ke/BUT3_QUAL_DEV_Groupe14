package com.iut.banque.facade;

import com.iut.banque.constants.LoginConstants;
import com.iut.banque.cryptage.PasswordHasher;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.modele.Utilisateur;
import com.iut.banque.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification "simple" (compatibilité avec l'existant).
 *
 * Codes de retour (LoginConstants):
 * - USER_IS_CONNECTED (1)
 * - MANAGER_IS_CONNECTED (2)
 * - LOGIN_FAILED (-1)
 * - ERROR (-2)
 */
@Service
@Transactional(readOnly = true)
public class LoginManager {

  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;
  // utilisateur connecté en mémoire (simple compatibilité avec l'existant)
  private Utilisateur connectedUser;

  public LoginManager(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
    this.utilisateurRepository = utilisateurRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public int tryLogin(String userCde, String userPwd) {
    try {
      if (userCde == null || userPwd == null) {
        return LoginConstants.ERROR;
      }
      Utilisateur user = utilisateurRepository.findById(userCde).orElse(null);
      if (user == null) {
        return LoginConstants.LOGIN_FAILED;
      }

      if (!passwordMatches(userPwd, user.getUserPwd())) {
        return LoginConstants.LOGIN_FAILED;
      }
      // mémoriser l'utilisateur connecté
      this.connectedUser = user;
      return (user instanceof Gestionnaire)
          ? LoginConstants.MANAGER_IS_CONNECTED
          : LoginConstants.USER_IS_CONNECTED;
    } catch (Exception e) {
      return LoginConstants.ERROR;
    }
  }

  public Utilisateur getConnectedUser() {
    return this.connectedUser;
  }

  public void logout() {
    this.connectedUser = null;
  }

  @Transactional
  public void changePassword(String userId, String currentPassword, String newPassword) throws IllegalOperationException {
    if (userId == null || userId.isBlank()) {
      throw new IllegalOperationException("Utilisateur introuvable");
    }
    if (currentPassword == null || currentPassword.isBlank()) {
      throw new IllegalOperationException("Mot de passe actuel invalide");
    }
    if (newPassword == null || newPassword.isBlank()) {
      throw new IllegalOperationException("Le nouveau mot de passe est requis");
    }

    Utilisateur user = utilisateurRepository.findById(userId)
        .orElseThrow(() -> new IllegalOperationException("Utilisateur introuvable"));

    if (!passwordMatches(currentPassword, user.getUserPwd())) {
      throw new IllegalOperationException("Mot de passe actuel incorrect");
    }

    user.setUserPwd(passwordEncoder.encode(newPassword));
    utilisateurRepository.save(user);
  }

  private boolean passwordMatches(String rawPassword, String storedHash) {
    if (storedHash == null) {
      return false;
    }

    // Compatibilité migration:
    // 1) hash moderne (bcrypt / autre impl PasswordEncoder)
    try {
      if (passwordEncoder.matches(rawPassword, storedHash)) {
        return true;
      }
    } catch (RuntimeException ignored) {
      // format inconnu pour l'encodeur courant => fallback legacy SHA-256
    }

    // 2) hash legacy présent dans les scripts SQL
    String actualLegacyHash = PasswordHasher.hashPassword(rawPassword);
    return actualLegacyHash.equals(storedHash);
  }
}
