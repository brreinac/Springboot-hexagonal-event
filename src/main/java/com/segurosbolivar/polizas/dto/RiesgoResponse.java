package com.segurosbolivar.polizas.dto;

import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import com.segurosbolivar.polizas.entity.Riesgo;

public record RiesgoResponse(
        Long id,
        String nombre,
        String descripcion,
        EstadoRiesgo estado,
        Long polizaId
) {
    public static RiesgoResponse desde(Riesgo riesgo) {
        return new RiesgoResponse(
                riesgo.getId(),
                riesgo.getNombre(),
                riesgo.getDescripcion(),
                riesgo.getEstado(),
                riesgo.getPoliza().getId()
        );
    }
}
