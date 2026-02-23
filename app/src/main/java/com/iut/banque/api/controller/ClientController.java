package com.iut.banque.api.controller;

import com.iut.banque.api.dto.CreateClientRequest;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.TechnicalException;
import com.iut.banque.facade.BanqueManager;
import com.iut.banque.modele.Client;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

  private final BanqueManager banqueManager;

  public ClientController(BanqueManager banqueManager) {
    this.banqueManager = banqueManager;
  }

  @GetMapping
  public List<Client> list() {
    return banqueManager.getAllClients();
  }

  @GetMapping("/{userId}")
  public Client get(@PathVariable String userId) {
    return (Client) banqueManager.getUserById(userId);
  }

  @PostMapping
  public Client create(@Valid @RequestBody CreateClientRequest req)
      throws IllegalFormatException, TechnicalException {
    return banqueManager.createClient(
        req.nom(), req.prenom(), req.adresse(), req.homme(),
        req.userId(), req.password(), req.numeroClient());
  }
}
