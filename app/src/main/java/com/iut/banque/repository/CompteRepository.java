package com.iut.banque.repository;

import com.iut.banque.modele.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompteRepository extends JpaRepository<Compte, String> {

    // Retourne tous les comptes appartenant à l'utilisateur identifié par userId
    java.util.List<Compte> findByOwnerUserId(String userId);
}
