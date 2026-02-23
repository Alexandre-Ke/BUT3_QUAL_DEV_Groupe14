package com.iut.banque.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.modele.Utilisateur;
import com.iut.banque.repository.UtilisateurRepository;

@Service
public class AuthService {

  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
    this.utilisateurRepository = utilisateurRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Utilisateur authenticate(String userId, String rawPassword) throws IllegalOperationException {
    Optional<Utilisateur> userOpt = utilisateurRepository.findById(userId);
    if (userOpt.isEmpty()) {
      throw new IllegalOperationException("Identifiants invalides");
    }
    Utilisateur user = userOpt.get();
    String stored = user.getUserPwd();
    if (stored == null || !passwordEncoder.matches(rawPassword, stored)) {
      throw new IllegalOperationException("Identifiants invalides");
    }
    return user;
  }

  public static String userType(Utilisateur u) {
    if (u instanceof Gestionnaire)
      return "GESTIONNAIRE";
    if (u instanceof Client)
      return "CLIENT";
    return "UTILISATEUR";
  }
}
