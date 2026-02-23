package com.iut.banque.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateClientRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank String adresse,
        boolean homme,
        @NotBlank String userId,
        @NotBlank String password,
        @NotBlank String numeroClient) {
}
