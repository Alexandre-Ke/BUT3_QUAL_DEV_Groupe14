package com.iut.banque.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
                @NotBlank String type,
                @NotBlank String userId,
                @NotBlank String password,
                @NotBlank String nom,
                @NotBlank String prenom,
                @NotBlank String adresse,
                boolean homme,
                String numeroClient) {
}
