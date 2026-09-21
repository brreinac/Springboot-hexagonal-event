package com.segurosbolivar.polizas.service;

import com.segurosbolivar.polizas.dto.PolizaResponse;
import com.segurosbolivar.polizas.dto.RiesgoResponse;
import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.Poliza;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.exception.RecursoNoEncontradoException;
import com.segurosbolivar.polizas.repository.PolizaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;

    public PolizaService(PolizaRepository polizaRepository) {
        this.polizaRepository = polizaRepository;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        return polizaRepository.buscarPorFiltros(tipo, estado).stream()
                .map(PolizaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> consultarRiesgos(Long polizaId) {
        Poliza poliza = buscarPorId(polizaId);
        return poliza.getRiesgos().stream()
                .map(RiesgoResponse::desde)
                .toList();
    }

    protected Poliza buscarPorId(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la póliza con id " + id + "."));
    }
}
