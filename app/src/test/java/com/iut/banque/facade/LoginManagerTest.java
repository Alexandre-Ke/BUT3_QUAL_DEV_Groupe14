package com.iut.banque.facade;

import com.iut.banque.constants.LoginConstants;
import com.iut.banque.cryptage.PasswordHasher;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginManagerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginManager loginManager;

    @Test
    void change_password_persists_and_new_password_can_login() throws Exception {
        String encodedPassword = "$2a$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOpqrstuvwxyz1234";
        Gestionnaire user = new Gestionnaire("Smith", "Joe", "Rue 1", true, "admin",
                PasswordHasher.hashPassword("Admin123!"));
        when(utilisateurRepository.findById("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Admin123!", user.getUserPwd())).thenReturn(false);
        when(passwordEncoder.encode("Nouveau123!")).thenReturn(encodedPassword);
        when(passwordEncoder.matches("Nouveau123!", encodedPassword)).thenReturn(true);
        when(passwordEncoder.matches("Admin123!", encodedPassword)).thenReturn(false);

        int before = loginManager.tryLogin("admin", "Admin123!");
        assertEquals(LoginConstants.MANAGER_IS_CONNECTED, before);

        loginManager.changePassword("admin", "Admin123!", "Nouveau123!");

        verify(utilisateurRepository, times(1)).save(user);
        assertEquals(LoginConstants.LOGIN_FAILED, loginManager.tryLogin("admin", "Admin123!"));
        assertEquals(LoginConstants.MANAGER_IS_CONNECTED, loginManager.tryLogin("admin", "Nouveau123!"));
    }

    @Test
    void change_password_rejects_wrong_current_password() throws Exception {
        Gestionnaire user = new Gestionnaire("Smith", "Joe", "Rue 1", true, "admin",
                PasswordHasher.hashPassword("Admin123!"));
        when(utilisateurRepository.findById("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("MauvaisMdp", user.getUserPwd())).thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> loginManager.changePassword("admin", "MauvaisMdp", "Nouveau123!"));
    }
}
