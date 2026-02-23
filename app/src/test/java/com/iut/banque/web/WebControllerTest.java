package com.iut.banque.web;

import com.iut.banque.config.TestConfig;
import com.iut.banque.constants.LoginConstants;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.facade.LoginManager;
import com.iut.banque.modele.Compte;
import com.iut.banque.service.BanqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
@Import(TestConfig.class)
class WebControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BanqueService banqueService;

    @MockBean
    LoginManager loginManager;

    @Test
    void get_login_page_ok() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void accounts_requires_login_redirects_when_no_session() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void listAccounts_client_calls_service_listAccountsForUser() throws Exception {
        when(banqueService.listAccountsForUser("a.user")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/accounts")
                .sessionAttr("userId", "a.user")
                .sessionAttr("userType", "Client"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accounts"))
                .andExpect(view().name("accounts"));

        verify(banqueService, times(1)).listAccountsForUser("a.user");
        verify(banqueService, never()).listAccounts();
    }

    @Test
    void listAccounts_manager_calls_service_listAccounts() throws Exception {
        when(banqueService.listAccounts()).thenReturn(Collections.emptyList());
        when(banqueService.listClients()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/accounts")
                .sessionAttr("userId", "mgr")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accounts"))
                .andExpect(view().name("accounts"));

        verify(banqueService, times(1)).listAccounts();
        verify(banqueService, times(1)).listClients();
        verify(banqueService, never()).listAccountsForUser(anyString());
    }

    @Test
    void listAccounts_manager_with_filter_calls_service_listAccountsForUser() throws Exception {
        when(banqueService.listAccountsForUser("client1")).thenReturn(Collections.emptyList());
        when(banqueService.listClients()).thenReturn(Collections.emptyList());
        com.iut.banque.modele.Client selected = mock(com.iut.banque.modele.Client.class);
        when(selected.getUserId()).thenReturn("client1");
        when(selected.getPrenom()).thenReturn("Jean");
        when(selected.getNom()).thenReturn("Dupont");
        when(selected.getAdresse()).thenReturn("Rue 1");
        when(banqueService.getUserById("client1")).thenReturn(selected);

        mockMvc.perform(get("/accounts")
                .param("ownerUserId", "client1")
                .sessionAttr("userId", "mgr")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accounts"))
                .andExpect(model().attributeExists("selectedFilterUser"))
                .andExpect(view().name("accounts"));

        verify(banqueService, never()).listAccounts();
        verify(banqueService, times(1)).listAccountsForUser("client1");
        verify(banqueService, times(1)).listClients();
        verify(banqueService, times(1)).getUserById("client1");
    }

    @Test
    void get_accounts_new_requires_login() throws Exception {
        mockMvc.perform(get("/accounts/new"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void get_accounts_new_ok_when_manager_logged_in() throws Exception {
        mockMvc.perform(get("/accounts/new")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-new"));
    }

    @Test
    void get_accounts_new_forbidden_for_client() throws Exception {
        mockMvc.perform(get("/accounts/new")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));
    }

    @Test
    void post_create_account_redirects_on_success_for_manager() throws Exception {
        when(banqueService.createAccountSansDecouvert(anyString(), anyString()))
                .thenReturn(mock(com.iut.banque.modele.CompteSansDecouvert.class));

        mockMvc.perform(post("/accounts/new")
                .param("numeroCompte", "FR0123456789")
                .param("clientUserId", "client")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void post_create_account_forbidden_for_client() throws Exception {
        mockMvc.perform(post("/accounts/new")
                .param("numeroCompte", "FR0123456789")
                .param("clientUserId", "client")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        verify(banqueService, never()).createAccountSansDecouvert(anyString(), anyString());
        verify(banqueService, never()).createAccountAvecDecouvert(anyString(), anyString(), anyDouble());
    }

    @Test
    void deposit_redirects_when_service_throws_for_manager() throws Exception {
        doThrow(new IllegalArgumentException("Accès refusé"))
                .when(banqueService).crediter(eq("ACC1"), eq(50.0));

        mockMvc.perform(post("/accounts/ACC1/deposit")
                .param("amount", "50")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().is3xxRedirection());

        verify(banqueService, times(1)).crediter("ACC1", 50.0);
    }

    @Test
    void deposit_forbidden_for_client() throws Exception {
        mockMvc.perform(post("/accounts/ACC1/deposit")
                .param("amount", "50")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        verify(banqueService, never()).crediter(anyString(), anyDouble());
    }

    @Test
    void transfer_page_ok_when_logged_in() throws Exception {
        when(banqueService.listAccountsForUser("client")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transfer")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accounts"))
                .andExpect(view().name("transfer"));
    }

    @Test
    void post_transfer_redirects_when_called() throws Exception {
        Compte mockCompte = mock(Compte.class);
        Compte mockTo = mock(Compte.class);
        when(mockCompte.getOwner()).thenReturn(mock(com.iut.banque.modele.Client.class));
        when(mockCompte.getOwner().getUserId()).thenReturn("client");
        when(mockTo.getOwner()).thenReturn(mock(com.iut.banque.modele.Client.class));
        when(mockTo.getOwner().getUserId()).thenReturn("client");
        when(banqueService.getAccountById("A")).thenReturn(mockCompte);
        when(banqueService.getAccountById("B")).thenReturn(mockTo);
        doNothing().when(banqueService).transfer(anyString(), anyString(), anyDouble());

        mockMvc.perform(post("/transfer")
                .param("fromAccountId", "A")
                .param("toAccountId", "B")
                .param("amount", "1.0")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().is3xxRedirection());

        verify(banqueService, times(1)).transfer("A", "B", 1.0);
    }

    @Test
    void view_account_not_found_redirects_or_error() throws Exception {
        when(banqueService.getAccountById("X"))
                .thenThrow(new com.iut.banque.exceptions.IllegalOperationException("not found"));

        mockMvc.perform(get("/accounts/X")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-detail"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void index_redirects_to_accounts_when_logged_in() throws Exception {
        mockMvc.perform(get("/")
                .sessionAttr("userId", "user1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void index_redirects_to_login_when_not_logged_in() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void login_page_returns_login_view() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void login_page_redirects_when_logged_in() throws Exception {
        mockMvc.perform(get("/login")
                .sessionAttr("userId", "user1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void post_login_success() throws Exception {
        when(loginManager.tryLogin("testuser", "password123")).thenReturn(LoginConstants.USER_IS_CONNECTED);
        com.iut.banque.modele.Client mockUser = mock(com.iut.banque.modele.Client.class);
        when(mockUser.getPrenom()).thenReturn("Jean");
        when(mockUser.getNom()).thenReturn("Dupont");
        when(banqueService.getUserById("testuser")).thenReturn(mockUser);

        mockMvc.perform(post("/login")
                .param("userId", "testuser")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void post_login_failure() throws Exception {
        when(loginManager.tryLogin("baduser", "wrongpassword")).thenReturn(LoginConstants.LOGIN_FAILED);

        mockMvc.perform(post("/login")
                .param("userId", "baduser")
                .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void register_page_ok() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("allowManagerCreation", false));
    }

    @Test
    void register_page_manager_can_create_both_types() throws Exception {
        mockMvc.perform(get("/register")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Gestionnaire"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("allowManagerCreation", true));
    }

    @Test
    void register_client_success_returns_login_view() throws Exception {
        when(banqueService.createClient("Dupont", "Jean", "123 rue", true, "a.dupont1", "Pass1234", "1234567890"))
                .thenReturn(mock(com.iut.banque.modele.Client.class));

        mockMvc.perform(post("/register")
                .param("type", "CLIENT")
                .param("userId", "a.dupont1")
                .param("password", "Pass1234")
                .param("confirmPassword", "Pass1234")
                .param("nom", "Dupont")
                .param("prenom", "Jean")
                .param("adresse", "123 rue")
                .param("homme", "true")
                .param("numeroClient", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("success"));
    }

    @Test
    void register_public_request_with_manager_type_is_forced_to_client() throws Exception {
        when(banqueService.createClient("Dupont", "Jean", "123 rue", true, "a.dupont1", "Pass1234", "1234567890"))
                .thenReturn(mock(com.iut.banque.modele.Client.class));

        mockMvc.perform(post("/register")
                .param("type", "GESTIONNAIRE")
                .param("userId", "a.dupont1")
                .param("password", "Pass1234")
                .param("confirmPassword", "Pass1234")
                .param("nom", "Dupont")
                .param("prenom", "Jean")
                .param("adresse", "123 rue")
                .param("homme", "true")
                .param("numeroClient", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("success"));

        verify(banqueService, times(1)).createClient("Dupont", "Jean", "123 rue", true, "a.dupont1", "Pass1234", "1234567890");
        verify(banqueService, never()).createManager(anyString(), anyString(), anyString(), anyBoolean(), anyString(), anyString());
    }

    @Test
    void register_manager_can_create_manager() throws Exception {
        when(banqueService.createManager("Admin", "Boss", "Rue Admin", true, "manager2", "Pass1234"))
                .thenReturn(mock(com.iut.banque.modele.Gestionnaire.class));

        mockMvc.perform(post("/register")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Gestionnaire")
                .param("type", "GESTIONNAIRE")
                .param("userId", "manager2")
                .param("password", "Pass1234")
                .param("confirmPassword", "Pass1234")
                .param("nom", "Admin")
                .param("prenom", "Boss")
                .param("adresse", "Rue Admin")
                .param("homme", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        verify(banqueService, times(1)).createManager("Admin", "Boss", "Rue Admin", true, "manager2", "Pass1234");
    }

    @Test
    void register_rejects_password_confirmation_mismatch() throws Exception {
        mockMvc.perform(post("/register")
                .param("type", "CLIENT")
                .param("userId", "a.dupont1")
                .param("password", "Pass1234")
                .param("confirmPassword", "Nope1234")
                .param("nom", "Dupont")
                .param("prenom", "Jean")
                .param("adresse", "123 rue")
                .param("homme", "true")
                .param("numeroClient", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));

        verify(banqueService, never()).createClient(anyString(), anyString(), anyString(), anyBoolean(), anyString(), anyString(), anyString());
    }

    @Test
    void password_page_requires_login() throws Exception {
        mockMvc.perform(get("/password"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void password_page_ok_when_logged_in() throws Exception {
        mockMvc.perform(get("/password")
                .sessionAttr("userId", "client")
                .sessionAttr("userName", "Jean Dupont"))
                .andExpect(status().isOk())
                .andExpect(view().name("password-change"));
    }

    @Test
    void post_password_change_success() throws Exception {
        doNothing().when(loginManager).changePassword("client", "oldPassword", "newPassword1");

        mockMvc.perform(post("/password")
                .sessionAttr("userId", "client")
                .param("currentPassword", "oldPassword")
                .param("newPassword", "newPassword1")
                .param("confirmPassword", "newPassword1"))
                .andExpect(status().is3xxRedirection());

        verify(loginManager, times(1)).changePassword("client", "oldPassword", "newPassword1");
    }

    @Test
    void post_password_change_fails_when_confirmation_mismatch() throws Exception {
        mockMvc.perform(post("/password")
                .sessionAttr("userId", "client")
                .param("currentPassword", "oldPassword")
                .param("newPassword", "newPassword1")
                .param("confirmPassword", "newPassword2"))
                .andExpect(status().is3xxRedirection());

        verify(loginManager, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void post_password_change_fails_when_current_password_invalid() throws Exception {
        doThrow(new IllegalOperationException("Mot de passe actuel incorrect"))
                .when(loginManager).changePassword("client", "badOldPassword", "newPassword1");

        mockMvc.perform(post("/password")
                .sessionAttr("userId", "client")
                .param("currentPassword", "badOldPassword")
                .param("newPassword", "newPassword1")
                .param("confirmPassword", "newPassword1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void logout_invalidates_session() throws Exception {
        mockMvc.perform(get("/logout")
                .sessionAttr("userId", "user1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void get_accounts_new_requires_login_redirects() throws Exception {
        mockMvc.perform(get("/accounts/new"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void post_create_account_with_overdraft() throws Exception {
        when(banqueService.createAccountAvecDecouvert(anyString(), anyString(), anyDouble()))
                .thenReturn(mock(com.iut.banque.modele.CompteAvecDecouvert.class));

        mockMvc.perform(post("/accounts/new")
                .param("numeroCompte", "FR0987654321")
                .param("clientUserId", "client")
                .param("decouvertAutorise", "500.0")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void withdraw_from_account_success_for_manager() throws Exception {
        doNothing().when(banqueService).debiter(eq("ACC1"), eq(50.0));

        mockMvc.perform(post("/accounts/ACC1/withdraw")
                .param("amount", "50")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().is3xxRedirection());

        verify(banqueService, times(1)).debiter("ACC1", 50.0);
    }

    @Test
    void withdraw_forbidden_for_client() throws Exception {
        mockMvc.perform(post("/accounts/ACC1/withdraw")
                .param("amount", "50")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        verify(banqueService, never()).debiter(anyString(), anyDouble());
    }

    @Test
    void withdraw_requires_login() throws Exception {
        mockMvc.perform(post("/accounts/ACC1/withdraw")
                .param("amount", "50"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void transfer_requires_login() throws Exception {
        mockMvc.perform(post("/transfer")
                .param("fromAccountId", "A")
                .param("toAccountId", "B")
                .param("amount", "10"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void view_account_success() throws Exception {
        com.iut.banque.modele.Client owner = mock(com.iut.banque.modele.Client.class);
        when(owner.getUserId()).thenReturn("client");

        Compte mockCompte = mock(Compte.class);
        when(mockCompte.getOwner()).thenReturn(owner);
        when(banqueService.getAccountById("ACC1")).thenReturn(mockCompte);

        mockMvc.perform(get("/accounts/ACC1")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-detail"))
                .andExpect(model().attributeExists("compte"));
    }

    @Test
    void view_account_manager_can_access() throws Exception {
        com.iut.banque.modele.Client owner = mock(com.iut.banque.modele.Client.class);

        Compte mockCompte = mock(Compte.class);
        when(mockCompte.getOwner()).thenReturn(owner);
        when(banqueService.getAccountById("ACC1")).thenReturn(mockCompte);

        mockMvc.perform(get("/accounts/ACC1")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-detail"))
                .andExpect(model().attributeExists("compte"));
    }

    @Test
    void view_account_client_access_denied() throws Exception {
        com.iut.banque.modele.Client owner = mock(com.iut.banque.modele.Client.class);
        when(owner.getUserId()).thenReturn("other");

        Compte mockCompte = mock(Compte.class);
        when(mockCompte.getOwner()).thenReturn(owner);
        when(banqueService.getAccountById("ACC1")).thenReturn(mockCompte);

        mockMvc.perform(get("/accounts/ACC1")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-detail"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void listAccounts_manager_no_session() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void create_account_page_success_for_manager() throws Exception {
        mockMvc.perform(get("/accounts/new")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Manager"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-new"));
    }

    @Test
    void create_account_page_requires_session() throws Exception {
        mockMvc.perform(get("/accounts/new"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void submit_create_account_success_for_manager() throws Exception {
        when(banqueService.createAccountSansDecouvert("FR1234567890", "manager")).thenReturn(null);

        mockMvc.perform(post("/accounts/new")
                .sessionAttr("userId", "manager")
                .sessionAttr("userType", "Manager")
                .param("numeroCompte", "FR1234567890"))
                .andExpect(status().is3xxRedirection());

        verify(banqueService, times(1)).createAccountSansDecouvert("FR1234567890", "manager");
    }

    @Test
    void transfer_page_success() throws Exception {
        when(banqueService.listAccountsForUser("client")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transfer")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"));
    }

    @Test
    void transfer_page_requires_session() throws Exception {
        mockMvc.perform(get("/transfer"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void submit_transfer_success() throws Exception {
        Compte from = mock(Compte.class);
        Compte to = mock(Compte.class);
        com.iut.banque.modele.Client owner = mock(com.iut.banque.modele.Client.class);
        when(owner.getUserId()).thenReturn("client");
        when(from.getOwner()).thenReturn(owner);
        when(to.getOwner()).thenReturn(owner);
        when(banqueService.getAccountById("FROM")).thenReturn(from);
        when(banqueService.getAccountById("TO")).thenReturn(to);
        doNothing().when(banqueService).transfer("FROM", "TO", 100.0);

        mockMvc.perform(post("/transfer")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client")
                .param("fromAccountId", "FROM")
                .param("toAccountId", "TO")
                .param("amount", "100.0"))
                .andExpect(status().is3xxRedirection());

        verify(banqueService, times(1)).transfer("FROM", "TO", 100.0);
    }

    @Test
    void submit_transfer_insufficient_funds() throws Exception {
        Compte from = mock(Compte.class);
        Compte to = mock(Compte.class);
        com.iut.banque.modele.Client owner = mock(com.iut.banque.modele.Client.class);
        when(owner.getUserId()).thenReturn("client");
        when(from.getOwner()).thenReturn(owner);
        when(to.getOwner()).thenReturn(owner);
        when(banqueService.getAccountById("FROM")).thenReturn(from);
        when(banqueService.getAccountById("TO")).thenReturn(to);
        doThrow(new IllegalOperationException("Insufficient funds"))
                .when(banqueService).transfer("FROM", "TO", 10000.0);

        mockMvc.perform(post("/transfer")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client")
                .param("fromAccountId", "FROM")
                .param("toAccountId", "TO")
                .param("amount", "10000.0"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void submit_transfer_client_destination_not_owned_is_rejected() throws Exception {
        Compte from = mock(Compte.class);
        Compte to = mock(Compte.class);
        com.iut.banque.modele.Client mine = mock(com.iut.banque.modele.Client.class);
        com.iut.banque.modele.Client other = mock(com.iut.banque.modele.Client.class);
        when(mine.getUserId()).thenReturn("client");
        when(other.getUserId()).thenReturn("other");
        when(from.getOwner()).thenReturn(mine);
        when(to.getOwner()).thenReturn(other);
        when(banqueService.getAccountById("FROM")).thenReturn(from);
        when(banqueService.getAccountById("TO")).thenReturn(to);

        mockMvc.perform(post("/transfer")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client")
                .param("fromAccountId", "FROM")
                .param("toAccountId", "TO")
                .param("amount", "25.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transfer"));

        verify(banqueService, never()).transfer(anyString(), anyString(), anyDouble());
    }

    @Test
    void logout_success() throws Exception {
        mockMvc.perform(get("/logout")
                .sessionAttr("userId", "client")
                .sessionAttr("userType", "Client"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void logout_no_session() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void home_page_ok() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void error_page_ok() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().is5xxServerError());
    }
}
