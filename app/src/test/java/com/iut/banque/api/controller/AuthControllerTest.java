package com.iut.banque.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iut.banque.api.dto.LoginRequest;
import com.iut.banque.config.TestConfig;
import com.iut.banque.constants.LoginConstants;
import com.iut.banque.facade.LoginManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LoginManager loginManager;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void login_client_success() throws Exception {
        when(loginManager.tryLogin("client", "password"))
                .thenReturn(LoginConstants.USER_IS_CONNECTED);

        LoginRequest request = new LoginRequest("client", "password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LoginConstants.USER_IS_CONNECTED))
                .andExpect(jsonPath("$.message").value("OK_CLIENT"));

        verify(loginManager, times(1)).tryLogin("client", "password");
    }

    @Test
    void login_manager_success() throws Exception {
        when(loginManager.tryLogin("manager", "password"))
                .thenReturn(LoginConstants.MANAGER_IS_CONNECTED);

        LoginRequest request = new LoginRequest("manager", "password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LoginConstants.MANAGER_IS_CONNECTED))
                .andExpect(jsonPath("$.message").value("OK_MANAGER"));

        verify(loginManager, times(1)).tryLogin("manager", "password");
    }

    @Test
    void login_failure() throws Exception {
        when(loginManager.tryLogin("baduser", "badpass"))
                .thenReturn(LoginConstants.LOGIN_FAILED);

        LoginRequest request = new LoginRequest("baduser", "badpass");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LoginConstants.LOGIN_FAILED))
                .andExpect(jsonPath("$.message").value("LOGIN_FAILED"));

        verify(loginManager, times(1)).tryLogin("baduser", "badpass");
    }

    @Test
    void login_invalid_request() throws Exception {
        LoginRequest request = new LoginRequest(null, "password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
