package com.iut.banque.facade;

import com.iut.banque.cryptage.PasswordHasher;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.InsufficientFundsException;
import com.iut.banque.exceptions.TechnicalException;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.CompteAvecDecouvert;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.modele.Utilisateur;
import com.iut.banque.repository.ClientRepository;
import com.iut.banque.repository.CompteRepository;
import com.iut.banque.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BanqueManager {

  private final ClientRepository clientRepository;
  private final CompteRepository compteRepository;
  private final UtilisateurRepository utilisateurRepository;

  public BanqueManager(ClientRepository clientRepository,
      CompteRepository compteRepository,
      UtilisateurRepository utilisateurRepository) {
    this.clientRepository = clientRepository;
    this.compteRepository = compteRepository;
    this.utilisateurRepository = utilisateurRepository;
  }

  /** Méthode utilisée pour les tests unitaires (historique). */
  @Transactional(readOnly = true)
  public Compte getAccountById(String id) {
    return compteRepository.findById(id).orElse(null);
  }

  /** Méthode utilisée pour les tests unitaires (historique). */
  @Transactional(readOnly = true)
  public Utilisateur getUserById(String userId) {
    return utilisateurRepository.findById(userId).orElse(null);
  }

  @Transactional(readOnly = true)
  public List<Client> getAllClients() {
    return clientRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<Gestionnaire> getAllManagers() {
    // Gestionnaire hérite d'Utilisateur (SINGLE_TABLE).
    return utilisateurRepository.findAll().stream()
        .filter(u -> u instanceof Gestionnaire)
        .map(u -> (Gestionnaire) u)
        .toList();
  }

  public Client createClient(String nom, String prenom, String adresse, boolean homme,
      String userId, String userPwd, String numeroClient)
      throws IllegalArgumentException, IllegalFormatException, TechnicalException {

    if (utilisateurRepository.existsById(userId)) {
      throw new TechnicalException("Identifiant déjà utilisé.");
    }
    if (clientRepository.findByNumeroClient(numeroClient).isPresent()) {
      throw new TechnicalException("Numéro client déjà utilisé.");
    }

    Client client = new Client(nom, prenom, adresse, homme, userId, userPwd, numeroClient);
    return clientRepository.save(client);
  }

  public Gestionnaire createManager(String nom, String prenom, String adresse, boolean homme,
      String userId, String userPwd)
      throws IllegalArgumentException, IllegalFormatException, TechnicalException {

    if (utilisateurRepository.existsById(userId)) {
      throw new TechnicalException("Identifiant déjà utilisé.");
    }
    Gestionnaire gestionnaire = new Gestionnaire(nom, prenom, adresse, homme, userId, userPwd);
    return (Gestionnaire) utilisateurRepository.save(gestionnaire);
  }

  public void createAccount(String numeroCompte, Client client) throws TechnicalException, IllegalFormatException {
    if (compteRepository.existsById(numeroCompte)) {
      throw new TechnicalException("Numéro de compte déjà utilisé.");
    }
    // solde initial = 0
    Compte compte = new com.iut.banque.modele.CompteSansDecouvert(numeroCompte, 0, client);
    client.addAccount(compte);
    clientRepository.save(client);
    compteRepository.save(compte);
  }

  public void createAccount(String numeroCompte, Client client, double decouvertAutorise)
      throws TechnicalException, IllegalFormatException, IllegalOperationException {

    if (compteRepository.existsById(numeroCompte)) {
      throw new TechnicalException("Numéro de compte déjà utilisé.");
    }
    CompteAvecDecouvert compte = new CompteAvecDecouvert(numeroCompte, 0, decouvertAutorise, client);
    client.addAccount(compte);
    clientRepository.save(client);
    compteRepository.save(compte);
  }

  public void deleteAccount(Compte c) throws IllegalOperationException {
    if (c == null) {
      return;
    }
    compteRepository.deleteById(c.getNumeroCompte());
  }

  public void deleteUser(String userId) throws IllegalOperationException {
    if (userId == null) {
      return;
    }
    utilisateurRepository.deleteById(userId);
  }

  public void debiter(Compte compte, double montant) throws InsufficientFundsException, IllegalFormatException {
    compte.debiter(montant);
    compteRepository.save(compte);
  }

  public void crediter(Compte compte, double montant) throws IllegalFormatException {
    compte.crediter(montant);
    compteRepository.save(compte);
  }

  public void changeDecouvert(CompteAvecDecouvert compte, double nouveauDecouvert)
      throws IllegalFormatException, IllegalOperationException {
    compte.setDecouverAutorise(nouveauDecouvert);
    compteRepository.save(compte);
  }

  public void updatePassword(String userId, String newPassword) throws TechnicalException {
    Utilisateur user = utilisateurRepository.findById(userId)
        .orElseThrow(() -> new TechnicalException("Utilisateur introuvable."));
    user.setUserPwd(PasswordHasher.hashPassword(newPassword));
    utilisateurRepository.save(user);
  }

  @Transactional(readOnly = true)
  public Utilisateur rechercherUtilisateurPourReinitialisation(String userId) {
    return utilisateurRepository.findById(userId).orElse(null);
  }

  public void reinitialiserLeMotDePasse(String userId, String newPassword) throws TechnicalException {
    updatePassword(userId, newPassword);
  }

  public void loadAllClients() {
  }

  public void loadAllGestionnaires() {
  }
}
