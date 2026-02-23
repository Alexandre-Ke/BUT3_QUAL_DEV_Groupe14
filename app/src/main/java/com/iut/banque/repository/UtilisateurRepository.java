package com.iut.banque.repository;

import com.iut.banque.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
}
