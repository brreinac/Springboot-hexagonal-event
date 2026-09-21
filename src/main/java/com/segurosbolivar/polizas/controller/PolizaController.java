package com.segurosbolivar.polizas.controller;

import com.segurosbolivar.polizas.dto.PolizaResponse;
import com.segurosbolivar.polizas.dto.RiesgoResponse;
import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.service.PolizaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/polizas")
public class PolizaController {

    private final PolizaService polizaService;

    public PolizaController(PolizaService polizaService) {
        this.polizaService = polizaService;
    }

    @GetMapping
    public ResponseEntity<List<PolizaResponse>> listar(
            @RequestParam(required = false) TipoPoliza tipo,
            @RequestParam(required = false) EstadoPoliza estado) {
        return ResponseEntity.ok(polizaService.listar(tipo, estado));
    }

    @GetMapping("/{id}/riesgos")
    public ResponseEntity<List<RiesgoResponse>> consultarRiesgos(@PathVariable Long id) {
        return ResponseEntity.ok(polizaService.consultarRiesgos(id));
    }
}
