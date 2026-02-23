package com.iut.banque.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iut.banque.api.dto.AmountRequest;
import com.iut.banque.api.dto.CreateAccountRequest;
import com.iut.banque.api.dto.TransferRequest;
import com.iut.banque.config.TestConfig;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.InsufficientFundsException;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Compte;
import com.iut.banque.modele.CompteAvecDecouvert;
import com.iut.banque.modele.CompteSansDecouvert;
import com.iut.banque.service.BanqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(TestConfig.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BanqueService banqueService;

    @Autowired
    ObjectMapper objectMapper;

    private Client sampleClient() throws Exception {
        return new Client("Dupont", "Jean", "123 rue Main", true, "d.dupont1", "password", "1234567890");
    }

    @Test
    void list_accounts_returns_all() throws Exception {
        Client owner = sampleClient();
        Compte c1 = new CompteSansDecouvert("FR1234567890", 100.0, owner);
        Compte c2 = new CompteSansDecouvert("FR1234567891", 200.0, owner);
        when(banqueService.listAccounts()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk());

        verify(banqueService, times(1)).listAccounts();
    }

    @Test
    void list_accounts_returns_empty() throws Exception {
        when(banqueService.listAccounts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk());

        verify(banqueService, times(1)).listAccounts();
    }

    @Test
    void get_account_success() throws Exception {
        Compte compte = new CompteSansDecouvert("FR1234567890", 100.0, sampleClient());
        when(banqueService.getAccountById("ACC1")).thenReturn(compte);

        mockMvc.perform(get("/api/accounts/ACC1"))
                .andExpect(status().isOk());

        verify(banqueService, times(1)).getAccountById("ACC1");
    }

    @Test
    void get_account_not_found() throws Exception {
        when(banqueService.getAccountById("NOEXIST"))
                .thenThrow(new IllegalOperationException("Not found"));

        mockMvc.perform(get("/api/accounts/NOEXIST"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_account_sans_decouvert() throws Exception {
        Compte mockCompte = new CompteSansDecouvert("FR1234567890", 0.0, sampleClient());
        when(banqueService.createAccountSansDecouvert("FR123", "client1"))
                .thenReturn(mockCompte);

        CreateAccountRequest request = new CreateAccountRequest("FR123", "client1", null);
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(banqueService, times(1)).createAccountSansDecouvert("FR123", "client1");
    }

    @Test
    void create_account_avec_decouvert() throws Exception {
        Compte mockCompte = new CompteAvecDecouvert("FR1234567892", 0.0, 500.0, sampleClient());
        when(banqueService.createAccountAvecDecouvert("FR456", "client2", 500.0))
                .thenReturn(mockCompte);

        CreateAccountRequest request = new CreateAccountRequest("FR456", "client2", 500.0);
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(banqueService, times(1)).createAccountAvecDecouvert("FR456", "client2", 500.0);
    }

    @Test
    void deposit_success() throws Exception {
        doNothing().when(banqueService).crediter("ACC1", 100.0);

        AmountRequest request = new AmountRequest(100.0);
        mockMvc.perform(post("/api/accounts/ACC1/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(banqueService, times(1)).crediter("ACC1", 100.0);
    }

    @Test
    void deposit_account_not_found() throws Exception {
        doThrow(new IllegalOperationException("Account not found"))
                .when(banqueService).crediter("NOEXIST", 100.0);

        AmountRequest request = new AmountRequest(100.0);
        mockMvc.perform(post("/api/accounts/NOEXIST/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deposit_invalid_format() throws Exception {
        doThrow(new IllegalFormatException("Invalid format"))
                .when(banqueService).crediter("ACC1", -100.0);

        AmountRequest request = new AmountRequest(-100.0);
        mockMvc.perform(post("/api/accounts/ACC1/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_success() throws Exception {
        doNothing().when(banqueService).debiter("ACC1", 50.0);

        AmountRequest request = new AmountRequest(50.0);
        mockMvc.perform(post("/api/accounts/ACC1/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(banqueService, times(1)).debiter("ACC1", 50.0);
    }

    @Test
    void withdraw_account_not_found() throws Exception {
        doThrow(new IllegalOperationException("Account not found"))
                .when(banqueService).debiter("NOEXIST", 50.0);

        AmountRequest request = new AmountRequest(50.0);
        mockMvc.perform(post("/api/accounts/NOEXIST/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void withdraw_insufficient_funds() throws Exception {
        doThrow(new InsufficientFundsException("Insufficient funds"))
                .when(banqueService).debiter("ACC1", 5000.0);

        AmountRequest request = new AmountRequest(5000.0);
        mockMvc.perform(post("/api/accounts/ACC1/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void withdraw_invalid_format() throws Exception {
        doThrow(new IllegalFormatException("Invalid format"))
                .when(banqueService).debiter("ACC1", -50.0);

        AmountRequest request = new AmountRequest(-50.0);
        mockMvc.perform(post("/api/accounts/ACC1/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_success() throws Exception {
        doNothing().when(banqueService).transfer("FROM", "TO", 100.0);

        TransferRequest request = new TransferRequest("FROM", "TO", 100.0);
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(banqueService, times(1)).transfer("FROM", "TO", 100.0);
    }

    @Test
    void transfer_same_account() throws Exception {
        doThrow(new IllegalOperationException("Same account"))
                .when(banqueService).transfer("ACC1", "ACC1", 100.0);

        TransferRequest request = new TransferRequest("ACC1", "ACC1", 100.0);
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_account_success() throws Exception {
        doNothing().when(banqueService).deleteAccount("ACC1");

        mockMvc.perform(delete("/api/accounts/ACC1"))
                .andExpect(status().isNoContent());

        verify(banqueService, times(1)).deleteAccount("ACC1");
    }

    @Test
    void delete_account_not_found() throws Exception {
        doNothing().when(banqueService).deleteAccount("NOEXIST");

        mockMvc.perform(delete("/api/accounts/NOEXIST"))
                .andExpect(status().isNoContent());
    }
}
