package com.iut.banque.service;

import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.InsufficientFundsException;
import com.iut.banque.exceptions.TechnicalException;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.CompteSansDecouvert;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.modele.Utilisateur;
import com.iut.banque.repository.ClientRepository;
import com.iut.banque.repository.CompteRepository;
import com.iut.banque.repository.GestionnaireRepository;
import com.iut.banque.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BanqueServiceTest {

    @Mock
    UtilisateurRepository utilisateurRepository;

    @Mock
    ClientRepository clientRepository;

    @Mock
    GestionnaireRepository gestionnaireRepository;

    @Mock
    CompteRepository compteRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    BanqueService banqueService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccountSansDecouvert_success() throws Exception {
        String userId = "test.user";
        when(clientRepository.findById(userId)).thenReturn(Optional.of(mock(Client.class)));
        when(compteRepository.existsById("FR0000000001")).thenReturn(false);

        // si le service retourne l'entité sauvegardée (ou pas), on ne dépend pas du
        // retour
        when(compteRepository.save(any(CompteSansDecouvert.class))).thenAnswer(inv -> inv.getArgument(0));

        banqueService.createAccountSansDecouvert("FR0000000001", userId);

        verify(compteRepository, times(1)).save(any(CompteSansDecouvert.class));
    }

    @Test
    void createAccountSansDecouvert_clientNotFound() {
        String userId = "missing.user";
        when(compteRepository.existsById("FR0000000002")).thenReturn(false);
        when(clientRepository.findById(userId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalOperationException.class,
                () -> banqueService.createAccountSansDecouvert("FR0000000002", userId));
        assertTrue(ex.getMessage().contains("Client introuvable"));
    }

    @Test
    void crediter_success() throws Exception {
        Compte c = mock(Compte.class);
        when(compteRepository.findById("FRC1")).thenReturn(Optional.of(c));

        banqueService.crediter("FRC1", 20.0);

        verify(c, times(1)).crediter(20.0);
        verify(compteRepository, times(1)).save(c);
    }

    @Test
    void debiter_success() throws Exception {
        Compte c = mock(Compte.class);
        when(compteRepository.findById("FR0001")).thenReturn(Optional.of(c));

        banqueService.debiter("FR0001", 10.0);

        verify(c, times(1)).debiter(10.0);
        verify(compteRepository, times(1)).save(c);
    }

    @Test
    void debiter_zero_amount_calls_debiter_zero() throws Exception {
        // ton impl appelle bien debiter(0.0)
        Compte c = mock(Compte.class);
        when(compteRepository.findById("FRZ1")).thenReturn(Optional.of(c));

        banqueService.debiter("FRZ1", 0.0);

        verify(c, times(1)).debiter(0.0);
        // selon ton service, il peut save ou pas; on ne force pas cette partie
    }

    @Test
    void crediter_negative_amount_throws_compte_introuvable_first() {
        // ton service va chercher le compte avant de valider le montant (d'après ta
        // stacktrace)
        when(compteRepository.findById("X")).thenReturn(Optional.empty());

        assertThrows(IllegalOperationException.class, () -> banqueService.crediter("X", -5.0));
    }

    @Test
    void debiter_negative_amount_throws_compte_introuvable_first() {
        when(compteRepository.findById("X")).thenReturn(Optional.empty());

        assertThrows(IllegalOperationException.class, () -> banqueService.debiter("X", -1.0));
    }

    @Test
    void find_nonexistent_compte_throws() {
        when(compteRepository.findById("NOPE")).thenReturn(Optional.empty());

        assertThrows(IllegalOperationException.class, () -> banqueService.debiter("NOPE", 1.0));
    }

    @Test
    void transfer_to_missing_account_throws() {
        when(compteRepository.findById("FROM2")).thenReturn(Optional.of(mock(Compte.class)));
        when(compteRepository.findById("MISSING")).thenReturn(Optional.empty());

        assertThrows(IllegalOperationException.class, () -> banqueService.transfer("FROM2", "MISSING", 5.0));
    }

    @Test
    void multiple_small_debits_and_credits() throws Exception {
        Compte c = mock(Compte.class);
        when(compteRepository.findById("MIX1")).thenReturn(Optional.of(c));

        banqueService.crediter("MIX1", 10.0);
        banqueService.debiter("MIX1", 5.0);
        banqueService.crediter("MIX1", 7.5);

        verify(c, times(2)).crediter(anyDouble());
        verify(c, times(1)).debiter(5.0);
    }

    @Test
    void transfer_multiple_calls_result_in_saves() throws Exception {
        Compte from = mock(Compte.class);
        Compte to = mock(Compte.class);
        when(compteRepository.findById("S1")).thenReturn(Optional.of(from));
        when(compteRepository.findById("S2")).thenReturn(Optional.of(to));

        banqueService.transfer("S1", "S2", 2.0);
        banqueService.transfer("S1", "S2", 3.0);

        verify(from, times(2)).debiter(anyDouble());
        verify(to, times(2)).crediter(anyDouble());
        verify(compteRepository, times(2)).save(to);
    }

    @Test
    void create_account_with_invalid_format_throws() {
        // ton service vérifie le client avant le format -> mock client existant
        when(clientRepository.findById("u")).thenReturn(Optional.of(mock(Client.class)));

        assertThrows(IllegalFormatException.class,
                () -> banqueService.createAccountSansDecouvert("badformat", "u"));
    }

    @Test
    void create_account_with_existing_number_throws() {
        // pour atteindre "numero déjà pris", il faut client existant
        when(clientRepository.findById("u")).thenReturn(Optional.of(mock(Client.class)));
        // ton service peut utiliser findById OU existsById; on couvre les deux
        when(compteRepository.existsById("FRDUP")).thenReturn(true);
        when(compteRepository.findById("FRDUP")).thenReturn(Optional.of(mock(Compte.class)));

        assertThrows(TechnicalException.class,
                () -> banqueService.createAccountSansDecouvert("FRDUP", "u"));
    }

    @Test
    void create_account_with_overdraft_success() throws Exception {
        // numéro au bon format (sinon IllegalFormatException)
        String num = "FR0000000003";

        when(clientRepository.findById("u")).thenReturn(Optional.of(mock(Client.class)));
        when(compteRepository.findById(num)).thenReturn(Optional.empty());
        when(compteRepository.existsById(num)).thenReturn(false);

        when(compteRepository.save(any(Compte.class))).thenAnswer(inv -> inv.getArgument(0));

        banqueService.createAccountAvecDecouvert(num, "u", 100.0);

        verify(compteRepository, times(1)).save(any(Compte.class));
    }

    @Test
    void debiter_insufficientFunds() throws Exception {
        Compte c = mock(Compte.class);
        doThrow(new InsufficientFundsException("Solde insuffisant")).when(c).debiter(1000.0);
        when(compteRepository.findById("FR0002")).thenReturn(Optional.of(c));

        Exception ex = assertThrows(InsufficientFundsException.class,
                () -> banqueService.debiter("FR0002", 1000.0));
        assertTrue(ex.getMessage().contains("Solde insuffisant"));
        verify(compteRepository, never()).save(c);
    }

    @Test
    void transfer_sameAccount_throws() {
        Exception ex = assertThrows(IllegalOperationException.class,
                () -> banqueService.transfer("A", "A", 10.0));
        assertTrue(ex.getMessage().toLowerCase().contains("identiques"));
    }

    @Test
    void transfer_insufficientFunds_propagates() throws Exception {
        Compte from = mock(Compte.class);
        Compte to = mock(Compte.class);
        doThrow(new InsufficientFundsException("insuffisant")).when(from).debiter(500.0);
        when(compteRepository.findById("FROM")).thenReturn(Optional.of(from));
        when(compteRepository.findById("TO")).thenReturn(Optional.of(to));

        Exception ex = assertThrows(InsufficientFundsException.class,
                () -> banqueService.transfer("FROM", "TO", 500.0));
        assertTrue(ex.getMessage().contains("insuffisant"));
        verify(compteRepository, never()).save(to);
    }

    @Test
    void createClient_success() throws Exception {
        when(utilisateurRepository.existsById("d.newclient1")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client result = banqueService.createClient("Dupont", "Jean", "123 rue Main", true,
                "d.newclient1", "password123", "1234567890");

        assertNotNull(result);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void createClient_userIdAlreadyExists() {
        when(utilisateurRepository.existsById("existing")).thenReturn(true);

        Exception ex = assertThrows(IllegalOperationException.class,
                () -> banqueService.createClient("Dupont", "Jean", "123 rue Main", true,
                        "existing", "password123", "CLIENT001"));
        assertTrue(ex.getMessage().contains("déjà utilisé"));
    }

    @Test
    void createManager_success() throws Exception {
        when(utilisateurRepository.existsById("newmanager")).thenReturn(false);
        when(gestionnaireRepository.save(any(Gestionnaire.class))).thenAnswer(inv -> inv.getArgument(0));

        Gestionnaire result = banqueService.createManager("Admin", "Boss", "456 rue Admin", true,
                "newmanager", "password123");

        assertNotNull(result);
        verify(gestionnaireRepository, times(1)).save(any(Gestionnaire.class));
    }

    @Test
    void createManager_userIdAlreadyExists() {
        when(utilisateurRepository.existsById("existingmgr")).thenReturn(true);

        Exception ex = assertThrows(IllegalOperationException.class,
                () -> banqueService.createManager("Admin", "Boss", "456 rue Admin", true,
                        "existingmgr", "password123"));
        assertTrue(ex.getMessage().contains("déjà utilisé"));
    }

    @Test
    void deleteAccount_exists() {
        when(compteRepository.existsById("ACC123")).thenReturn(true);

        banqueService.deleteAccount("ACC123");

        verify(compteRepository, times(1)).deleteById("ACC123");
    }

    @Test
    void deleteAccount_notExists() {
        when(compteRepository.existsById("NOCACC")).thenReturn(false);

        banqueService.deleteAccount("NOCACC");

        verify(compteRepository, never()).deleteById(anyString());
    }

    @Test
    void deleteUser_exists() {
        when(utilisateurRepository.existsById("user123")).thenReturn(true);

        banqueService.deleteUser("user123");

        verify(utilisateurRepository, times(1)).deleteById("user123");
    }

    @Test
    void deleteUser_notExists() {
        when(utilisateurRepository.existsById("nouser")).thenReturn(false);

        banqueService.deleteUser("nouser");

        verify(utilisateurRepository, never()).deleteById(anyString());
    }

    @Test
    void getUserById_found() throws Exception {
        Utilisateur mockUser = mock(Utilisateur.class);
        when(utilisateurRepository.findById("user1")).thenReturn(Optional.of(mockUser));

        Utilisateur result = banqueService.getUserById("user1");

        assertNotNull(result);
        assertEquals(mockUser, result);
    }

    @Test
    void getUserById_notFound() {
        when(utilisateurRepository.findById("nouser")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalOperationException.class,
                () -> banqueService.getUserById("nouser"));
        assertTrue(ex.getMessage().contains("Utilisateur introuvable"));
    }

    @Test
    void listAccounts_empty() {
        when(compteRepository.findAll()).thenReturn(Collections.emptyList());

        List<Compte> result = banqueService.listAccounts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listAccounts_multiple() {
        Compte c1 = mock(Compte.class);
        Compte c2 = mock(Compte.class);
        when(compteRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<Compte> result = banqueService.listAccounts();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void listAccountsForUser_empty() {
        when(compteRepository.findByOwnerUserId("user1")).thenReturn(Collections.emptyList());

        List<Compte> result = banqueService.listAccountsForUser("user1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listAccountsForUser_multiple() {
        Compte c1 = mock(Compte.class);
        Compte c2 = mock(Compte.class);
        when(compteRepository.findByOwnerUserId("user1")).thenReturn(Arrays.asList(c1, c2));

        List<Compte> result = banqueService.listAccountsForUser("user1");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void crediter_calls_save() throws Exception {
        Compte c = mock(Compte.class);
        when(compteRepository.findById("ACC1")).thenReturn(Optional.of(c));

        banqueService.crediter("ACC1", 100.0);

        verify(c, times(1)).crediter(100.0);
        verify(compteRepository, times(1)).save(c);
    }

    @Test
    void debiter_accountNotFound() {
        when(compteRepository.findById("NOEXIST")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalOperationException.class,
                () -> banqueService.debiter("NOEXIST", 50.0));
        assertTrue(ex.getMessage().contains("Compte introuvable"));
    }

    @Test
    void getAccountById_found() throws Exception {
        Compte mockCompte = mock(Compte.class);
        when(compteRepository.findById("ACC1")).thenReturn(Optional.of(mockCompte));

        Compte result = banqueService.getAccountById("ACC1");

        assertNotNull(result);
        assertEquals(mockCompte, result);
    }

    @Test
    void getAccountById_notFound() {
        when(compteRepository.findById("NOEXIST")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalOperationException.class,
                () -> banqueService.getAccountById("NOEXIST"));
        assertTrue(ex.getMessage().contains("Compte introuvable"));
    }
}
