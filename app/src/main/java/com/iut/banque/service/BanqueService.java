package com.iut.banque.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.InsufficientFundsException;
import com.iut.banque.exceptions.TechnicalException;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.CompteAvecDecouvert;
import com.iut.banque.modele.CompteSansDecouvert;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.modele.Utilisateur;
import com.iut.banque.repository.ClientRepository;
import com.iut.banque.repository.CompteRepository;
import com.iut.banque.repository.GestionnaireRepository;
import com.iut.banque.repository.UtilisateurRepository;

@Service
public class BanqueService {

  private final UtilisateurRepository utilisateurRepository;
  private final ClientRepository clientRepository;
  private final GestionnaireRepository gestionnaireRepository;
  private final CompteRepository compteRepository;
  private final PasswordEncoder passwordEncoder;

  public BanqueService(
      UtilisateurRepository utilisateurRepository,
      ClientRepository clientRepository,
      GestionnaireRepository gestionnaireRepository,
      CompteRepository compteRepository,
      PasswordEncoder passwordEncoder) {
    this.utilisateurRepository = utilisateurRepository;
    this.clientRepository = clientRepository;
    this.gestionnaireRepository = gestionnaireRepository;
    this.compteRepository = compteRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Utilisateur getUserById(String userId) throws IllegalOperationException {
    return utilisateurRepository.findById(userId)
        .orElseThrow(() -> new IllegalOperationException("Utilisateur introuvable: " + userId));
  }

  public Compte getAccountById(String accountId) throws IllegalOperationException {
    return compteRepository.findById(accountId)
        .orElseThrow(() -> new IllegalOperationException("Compte introuvable: " + accountId));
  }

  public List<Compte> listAccounts() {
    return compteRepository.findAll();
  }

  /**
   * Liste des comptes appartenant à un utilisateur donné (userId).
   */
  public List<Compte> listAccountsForUser(String userId) {
    return compteRepository.findByOwnerUserId(userId);
  }

  public List<Client> listClients() {
    return clientRepository.findAll();
  }

  @Transactional
  public Client createClient(String nom, String prenom, String adresse, boolean homme,
      String userId, String rawPassword, String numeroClient)
      throws IllegalOperationException, IllegalFormatException {
    if (utilisateurRepository.existsById(userId)) {
      throw new IllegalOperationException("Identifiant déjà utilisé: " + userId);
    }
    String encoded = passwordEncoder.encode(rawPassword);
    Client c = new Client(nom, prenom, adresse, homme, userId, encoded, numeroClient);
    // le constructeur Client appelle setUserPwd / setUserId etc.
    return clientRepository.save(c);
  }

  @Transactional
  public Gestionnaire createManager(String nom, String prenom, String adresse, boolean homme,
      String userId, String rawPassword)
      throws IllegalOperationException, IllegalFormatException {
    if (utilisateurRepository.existsById(userId)) {
      throw new IllegalOperationException("Identifiant déjà utilisé: " + userId);
    }
    String encoded = passwordEncoder.encode(rawPassword);
    Gestionnaire g = new Gestionnaire(nom, prenom, adresse, homme, userId, encoded);
    return gestionnaireRepository.save(g);
  }

  @Transactional
  public Compte createAccountSansDecouvert(String numeroCompte, String clientUserId)
      throws TechnicalException, IllegalFormatException, IllegalOperationException {
    if (compteRepository.existsById(numeroCompte)) {
      throw new TechnicalException("Le numéro de compte existe déjà: " + numeroCompte);
    }
    Client client = clientRepository.findById(clientUserId)
        .orElseThrow(() -> new IllegalOperationException("Client introuvable: " + clientUserId));

    CompteSansDecouvert c = new CompteSansDecouvert(numeroCompte, 0, client);
    return compteRepository.save(c);
  }

  @Transactional
  public Compte createAccountAvecDecouvert(String numeroCompte, String clientUserId, double decouvertAutorise)
      throws TechnicalException, IllegalFormatException, IllegalOperationException {
    if (compteRepository.existsById(numeroCompte)) {
      throw new TechnicalException("Le numéro de compte existe déjà: " + numeroCompte);
    }
    Client client = clientRepository.findById(clientUserId)
        .orElseThrow(() -> new IllegalOperationException("Client introuvable: " + clientUserId));

    CompteAvecDecouvert c = new CompteAvecDecouvert(numeroCompte, 0, decouvertAutorise, client);
    return compteRepository.save(c);
  }

  @Transactional
  public void crediter(String numeroCompte, double montant) throws IllegalOperationException, IllegalFormatException {
    Compte c = getAccountById(numeroCompte);
    c.crediter(montant);
    compteRepository.save(c);
  }

  @Transactional
  public void debiter(String numeroCompte, double montant)
      throws IllegalOperationException, InsufficientFundsException, IllegalFormatException {
    Compte c = getAccountById(numeroCompte);
    c.debiter(montant);
    compteRepository.save(c);
  }

  @Transactional
  public void transfer(String fromAccountId, String toAccountId, double montant)
      throws IllegalOperationException, InsufficientFundsException, IllegalFormatException {
    if (fromAccountId.equals(toAccountId)) {
      throw new IllegalOperationException("Compte source et destination identiques");
    }
    Compte from = getAccountById(fromAccountId);
    Compte to = getAccountById(toAccountId);

    from.debiter(montant);
    to.crediter(montant);

    compteRepository.save(from);
    compteRepository.save(to);
  }

  @Transactional
  public void deleteAccount(String accountId) {
    if (!compteRepository.existsById(accountId))
      return;
    compteRepository.deleteById(accountId);
  }

  @Transactional
  public void deleteUser(String userId) {
    if (!utilisateurRepository.existsById(userId))
      return;
    utilisateurRepository.deleteById(userId);
  }
}
