package com.segurosbolivar.polizas.dto;

import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.Poliza;
import com.segurosbolivar.polizas.entity.TipoPoliza;

import java.math.BigDecimal;

public record PolizaResponse(
        Long id,
        TipoPoliza tipo,
        EstadoPoliza estado,
        Integer periodoMeses,
        BigDecimal canonMensual,
        BigDecimal prima,
        int cantidadRiesgos
) {
    public static PolizaResponse desde(Poliza poliza) {
        return new PolizaResponse(
                poliza.getId(),
                poliza.getTipo(),
                poliza.getEstado(),
                poliza.getPeriodoMeses(),
                poliza.getCanonMensual(),
                poliza.getPrima(),
                poliza.getRiesgos().size()
        );
    }
}
