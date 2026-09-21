package com.segurosbolivar.polizas.dto;

import java.time.Instant;

public record RespuestaError(
        Instant fecha,
        int estado,
        String error,
        String mensaje,
        String ruta
) {
}
