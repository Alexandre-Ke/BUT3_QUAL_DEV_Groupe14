package com.iut.banque.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.iut.banque.api.dto.CreateUserRequest;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.modele.Client;
import com.iut.banque.modele.Gestionnaire;
import com.iut.banque.service.BanqueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

  private final BanqueService banqueService;

  public UserController(BanqueService banqueService) {
    this.banqueService = banqueService;
  }

  @PostMapping
  public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest req) throws Exception {
    String type = req.type().trim().toUpperCase();

    return switch (type) {
      case "CLIENT" -> {
        Client c = banqueService.createClient(
            req.nom(), req.prenom(), req.adresse(), req.homme(),
            req.userId(), req.password(), req.numeroClient());
        yield ResponseEntity.ok(c);
      }
      case "GESTIONNAIRE" -> {
        Gestionnaire g = banqueService.createManager(
            req.nom(), req.prenom(), req.adresse(), req.homme(),
            req.userId(), req.password());
        yield ResponseEntity.ok(g);
      }
      default -> ResponseEntity.badRequest().body("type doit être CLIENT ou GESTIONNAIRE");
    };
  }

  @GetMapping("/{userId}")
  public ResponseEntity<?> getUser(@PathVariable String userId) {
    try {
      return ResponseEntity.ok(banqueService.getUserById(userId));
    } catch (IllegalOperationException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<?> deleteUser(@PathVariable String userId) {
    banqueService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }
}
