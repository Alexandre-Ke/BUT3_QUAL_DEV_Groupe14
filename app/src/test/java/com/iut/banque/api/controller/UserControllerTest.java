package com.iut.banque.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iut.banque.api.dto.CreateUserRequest;
import com.iut.banque.config.TestConfig;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.service.BanqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BanqueService banqueService;

    @Autowired
    ObjectMapper objectMapper;

    private Client sampleClient() throws Exception {
        return new Client("Dupont", "Jean", "123 rue Main", true, "d.dupont1", "password", "1234567890");
    }

    private Gestionnaire sampleManager() throws Exception {
        return new Gestionnaire("Admin", "Boss", "456 rue Admin", true, "manager1", "password");
    }

    @Test
    void create_client_success() throws Exception {
        Client mockClient = sampleClient();
        when(banqueService.createClient(anyString(), anyString(), anyString(), anyBoolean(),
                anyString(), anyString(), anyString()))
                .thenReturn(mockClient);

        CreateUserRequest request = new CreateUserRequest(
                "CLIENT", "client1", "password", "Dupont", "Jean", "123 rue Main", true, "CLIENT001");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(banqueService, times(1)).createClient(
                "Dupont", "Jean", "123 rue Main", true, "client1", "password", "CLIENT001");
    }

    @Test
    void create_manager_success() throws Exception {
        Gestionnaire mockManager = sampleManager();
        when(banqueService.createManager(anyString(), anyString(), anyString(), anyBoolean(),
                anyString(), anyString()))
                .thenReturn(mockManager);

        CreateUserRequest request = new CreateUserRequest(
                "GESTIONNAIRE", "manager1", "password", "Admin", "Boss", "456 rue Admin", true, null);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(banqueService, times(1)).createManager(
                "Admin", "Boss", "456 rue Admin", true, "manager1", "password");
    }

    @Test
    void create_user_invalid_type() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "INVALID", "user1", "password", "Test", "User", "address", true, null);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_client_already_exists() throws Exception {
        when(banqueService.createClient(anyString(), anyString(), anyString(), anyBoolean(),
                eq("existing"), anyString(), anyString()))
                .thenThrow(new IllegalOperationException("User already exists"));

        CreateUserRequest request = new CreateUserRequest(
                "CLIENT", "existing", "password", "Dupont", "Jean", "123 rue Main", true, "CLIENT001");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_user_success() throws Exception {
        Client mockClient = sampleClient();
        when(banqueService.getUserById("client1")).thenReturn(mockClient);

        mockMvc.perform(get("/api/users/client1"))
                .andExpect(status().isOk());

        verify(banqueService, times(1)).getUserById("client1");
    }

    @Test
    void get_user_not_found() throws Exception {
        when(banqueService.getUserById("noexist"))
                .thenThrow(new IllegalOperationException("User not found"));

        mockMvc.perform(get("/api/users/noexist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_user_success() throws Exception {
        doNothing().when(banqueService).deleteUser("user1");

        mockMvc.perform(delete("/api/users/user1"))
                .andExpect(status().isNoContent());

        verify(banqueService, times(1)).deleteUser("user1");
    }

    @Test
    void delete_user_not_found() throws Exception {
        doNothing().when(banqueService).deleteUser("noexist");

        mockMvc.perform(delete("/api/users/noexist"))
                .andExpect(status().isNoContent());
    }

    @Test
    void create_user_missing_required_fields() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "CLIENT", "user1", "password", null, "Jean", "address", true, "CLIENT001");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
