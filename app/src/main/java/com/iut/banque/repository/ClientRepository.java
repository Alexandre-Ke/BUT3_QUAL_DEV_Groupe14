package com.iut.banque.repository;

import com.iut.banque.modele.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {
  Optional<Client> findByNumeroClient(String numeroClient);
}
