package com.iut.banque.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iut.banque.api.dto.CreateClientRequest;
import com.iut.banque.config.TestConfig;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.TechnicalException;
import com.iut.banque.facade.BanqueManager;
import com.iut.banque.modele.Client;
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

@WebMvcTest(ClientController.class)
@Import(TestConfig.class)
class ClientControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BanqueManager banqueManager;

    @Autowired
    ObjectMapper objectMapper;

    private Client sampleClient() throws Exception {
        return new Client("Dupont", "Jean", "123 rue Main", true, "d.dupont1", "password", "1234567890");
    }

    @Test
    void list_clients_returns_all() throws Exception {
        Client c1 = sampleClient();
        Client c2 = new Client("Martin", "Claire", "456 rue Paris", false, "c.martin2", "password", "1234567891");
        when(banqueManager.getAllClients()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk());

        verify(banqueManager, times(1)).getAllClients();
    }

    @Test
    void list_clients_returns_empty() throws Exception {
        when(banqueManager.getAllClients()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk());

        verify(banqueManager, times(1)).getAllClients();
    }

    @Test
    void get_client_success() throws Exception {
        Client mockClient = sampleClient();
        when(banqueManager.getUserById("client1")).thenReturn(mockClient);

        mockMvc.perform(get("/api/clients/client1"))
                .andExpect(status().isOk());

        verify(banqueManager, times(1)).getUserById("client1");
    }

    @Test
    void create_client_success() throws Exception {
        Client mockClient = sampleClient();
        when(banqueManager.createClient(anyString(), anyString(), anyString(), anyBoolean(),
                anyString(), anyString(), anyString()))
                .thenReturn(mockClient);

        CreateClientRequest request = new CreateClientRequest(
                "Dupont", "Jean", "123 rue Main", true, "client1", "password", "CLIENT001");

        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(banqueManager, times(1)).createClient(
                "Dupont", "Jean", "123 rue Main", true, "client1", "password", "CLIENT001");
    }

    @Test
    void create_client_invalid_format() throws Exception {
        when(banqueManager.createClient(anyString(), anyString(), anyString(), anyBoolean(),
                anyString(), anyString(), anyString()))
                .thenThrow(new IllegalFormatException("Invalid format"));

        CreateClientRequest request = new CreateClientRequest(
                "Dupont", "Jean", "123 rue Main", true, "client1", "password", "INVALID");

        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_client_technical_error() throws Exception {
        when(banqueManager.createClient(anyString(), anyString(), anyString(), anyBoolean(),
                anyString(), anyString(), anyString()))
                .thenThrow(new TechnicalException("Database error"));

        CreateClientRequest request = new CreateClientRequest(
                "Dupont", "Jean", "123 rue Main", true, "client1", "password", "CLIENT001");

        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_client_missing_fields() throws Exception {
        CreateClientRequest request = new CreateClientRequest(
                null, "Jean", "address", true, "client1", "password", "CLIENT001");

        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
