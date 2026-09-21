package com.segurosbolivar.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventoCoreRequest(
        @NotBlank(message = "El evento es obligatorio.") String evento,
        @NotNull(message = "El polizaId es obligatorio.") Long polizaId
) {
}
