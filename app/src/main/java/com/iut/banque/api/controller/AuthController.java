package com.iut.banque.api.controller;

import com.iut.banque.api.dto.LoginRequest;
import com.iut.banque.api.dto.LoginResponse;
import com.iut.banque.constants.LoginConstants;
import com.iut.banque.facade.LoginManager;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final LoginManager loginManager;

  public AuthController(LoginManager loginManager) {
    this.loginManager = loginManager;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    int code = loginManager.tryLogin(request.userId(), request.password());
    String msg = switch (code) {
      case LoginConstants.USER_IS_CONNECTED -> "OK_CLIENT";
      case LoginConstants.MANAGER_IS_CONNECTED -> "OK_MANAGER";
      case LoginConstants.LOGIN_FAILED -> "LOGIN_FAILED";
      default -> "ERROR";
    };
    return ResponseEntity.ok(new LoginResponse(code, msg));
  }
}
