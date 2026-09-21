package com.segurosbolivar.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearRiesgoRequest(
        @NotBlank(message = "El nombre del riesgo es obligatorio.")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
        String nombre,

        @NotBlank(message = "La descripción del riesgo es obligatoria.")
        @Size(max = 300, message = "La descripción no puede superar 300 caracteres.")
        String descripcion
) {
}
