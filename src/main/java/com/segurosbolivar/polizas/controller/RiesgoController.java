package com.segurosbolivar.polizas.controller;

import com.segurosbolivar.polizas.dto.CrearRiesgoRequest;
import com.segurosbolivar.polizas.dto.RiesgoResponse;
import com.segurosbolivar.polizas.service.RiesgoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RiesgoController {

    private final RiesgoService riesgoService;

    public RiesgoController(RiesgoService riesgoService) {
        this.riesgoService = riesgoService;
    }

    @PostMapping("/polizas/{id}/riesgos")
    public ResponseEntity<RiesgoResponse> agregar(@PathVariable Long id,
                                                  @Valid @RequestBody CrearRiesgoRequest request) {
        RiesgoResponse riesgo = riesgoService.agregar(id, request);
        URI ubicacion = URI.create("/polizas/" + id + "/riesgos/" + riesgo.id());
        return ResponseEntity.created(ubicacion).body(riesgo);
    }

    @PostMapping("/riesgos/{id}/cancelar")
    public ResponseEntity<RiesgoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(riesgoService.cancelar(id));
    }
}
