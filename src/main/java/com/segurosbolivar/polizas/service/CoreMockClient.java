package com.segurosbolivar.polizas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CoreMockClient implements CoreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreMockClient.class);

    @Override
    public void enviarActualizacion(Long polizaId) {
        registrarIntento("ACTUALIZACION", polizaId);
    }

    public void registrarIntento(String evento, Long polizaId) {
        LOGGER.info("CORE mock: se intentó enviar el evento {} para la póliza {}.", evento, polizaId);
    }
}
