package com.iut.banque.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AmountRequest(
        @NotNull @Positive Double amount) {
}
