package com.iut.banque.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
                @NotBlank String numeroCompte,
                @NotBlank String clientUserId,
                Double decouvertAutorise) {
}
