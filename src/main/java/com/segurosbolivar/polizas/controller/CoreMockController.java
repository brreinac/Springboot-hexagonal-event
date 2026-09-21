package com.segurosbolivar.polizas.controller;

import com.segurosbolivar.polizas.dto.EventoCoreRequest;
import com.segurosbolivar.polizas.service.CoreMockClient;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private final CoreMockClient coreMockClient;

    public CoreMockController(CoreMockClient coreMockClient) {
        this.coreMockClient = coreMockClient;
    }

    @PostMapping("/evento")
    public ResponseEntity<Map<String, String>> registrarEvento(@Valid @RequestBody EventoCoreRequest request) {
        coreMockClient.registrarIntento(request.evento(), request.polizaId());
        return ResponseEntity.ok(Map.of("mensaje", "Evento registrado en el CORE mock."));
    }
}
