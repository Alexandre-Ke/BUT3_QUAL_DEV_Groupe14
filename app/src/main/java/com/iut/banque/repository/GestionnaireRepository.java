package com.iut.banque.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iut.banque.modele.Gestionnaire;

public interface GestionnaireRepository extends JpaRepository<Gestionnaire, String> {
}
