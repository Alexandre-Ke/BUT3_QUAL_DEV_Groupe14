package com.iut.banque.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @NotBlank String fromAccountId,
        @NotBlank String toAccountId,
        @NotNull @Positive Double amount) {
}
