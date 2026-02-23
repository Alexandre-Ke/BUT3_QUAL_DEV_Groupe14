package com.iut.banque.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.iut.banque.api.dto.AmountRequest;
import com.iut.banque.api.dto.CreateAccountRequest;
import com.iut.banque.api.dto.TransferRequest;
import com.iut.banque.modele.Compte;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.InsufficientFundsException;
import org.springframework.http.HttpStatus;
import com.iut.banque.service.BanqueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

  private final BanqueService banqueService;

  public AccountController(BanqueService banqueService) {
    this.banqueService = banqueService;
  }

  @GetMapping
  public List<Compte> list() {
    return banqueService.listAccounts();
  }

  @GetMapping("/{accountId}")
  public ResponseEntity<Compte> get(@PathVariable String accountId) {
    try {
      return ResponseEntity.ok(banqueService.getAccountById(accountId));
    } catch (IllegalOperationException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @PostMapping
  public ResponseEntity<Compte> create(@Valid @RequestBody CreateAccountRequest req) throws Exception {
    if (req.decouvertAutorise() == null) {
      return ResponseEntity.ok(banqueService.createAccountSansDecouvert(req.numeroCompte(), req.clientUserId()));
    }
    return ResponseEntity
        .ok(banqueService.createAccountAvecDecouvert(req.numeroCompte(), req.clientUserId(), req.decouvertAutorise()));
  }

  @PostMapping("/{accountId}/deposit")
  public ResponseEntity<?> deposit(@PathVariable String accountId, @Valid @RequestBody AmountRequest req) {
    try {
      banqueService.crediter(accountId, req.amount());
      return ResponseEntity.noContent().build();
    } catch (IllegalOperationException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (IllegalFormatException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/{accountId}/withdraw")
  public ResponseEntity<?> withdraw(@PathVariable String accountId, @Valid @RequestBody AmountRequest req) {
    try {
      banqueService.debiter(accountId, req.amount());
      return ResponseEntity.noContent().build();
    } catch (IllegalOperationException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (IllegalFormatException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (InsufficientFundsException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  @PostMapping("/transfer")
  public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest req) throws Exception {
    banqueService.transfer(req.fromAccountId(), req.toAccountId(), req.amount());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{accountId}")
  public ResponseEntity<?> delete(@PathVariable String accountId) {
    banqueService.deleteAccount(accountId);
    return ResponseEntity.noContent().build();
  }
}
