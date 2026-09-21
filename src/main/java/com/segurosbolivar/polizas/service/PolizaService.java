package com.segurosbolivar.polizas.service;

import com.segurosbolivar.polizas.config.ConfiguracionPolizas;
import com.segurosbolivar.polizas.dto.PolizaResponse;
import com.segurosbolivar.polizas.dto.RiesgoResponse;
import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import com.segurosbolivar.polizas.entity.Poliza;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.exception.RecursoNoEncontradoException;
import com.segurosbolivar.polizas.exception.ReglaNegocioException;
import com.segurosbolivar.polizas.repository.PolizaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.RoundingMode;
import java.math.BigDecimal;

@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final ConfiguracionPolizas configuracionPolizas;
    private final CoreClient coreClient;

    public PolizaService(PolizaRepository polizaRepository,
                         ConfiguracionPolizas configuracionPolizas,
                         CoreClient coreClient) {
        this.polizaRepository = polizaRepository;
        this.configuracionPolizas = configuracionPolizas;
        this.coreClient = coreClient;
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

    @Transactional
    public PolizaResponse renovar(Long polizaId) {
        Poliza poliza = buscarPorId(polizaId);
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se puede renovar una póliza cancelada.");
        }
        if (poliza.getEstado() == EstadoPoliza.RENOVADA) {
            throw new ReglaNegocioException("La póliza ya fue renovada y no puede renovarse nuevamente.");
        }

        BigDecimal nuevoCanon = poliza.getCanonMensual()
                .multiply(BigDecimal.ONE.add(configuracionPolizas.getIpc()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal nuevaPrima = nuevoCanon.multiply(BigDecimal.valueOf(poliza.getPeriodoMeses()))
                .setScale(2, RoundingMode.HALF_UP);

        poliza.setCanonMensual(nuevoCanon);
        poliza.setPrima(nuevaPrima);
        poliza.setEstado(EstadoPoliza.RENOVADA);
        polizaRepository.save(poliza);
        coreClient.enviarActualizacion(poliza.getId());

        return PolizaResponse.desde(poliza);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = buscarPorId(polizaId);
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("La póliza ya se encuentra cancelada.");
        }

        poliza.setEstado(EstadoPoliza.CANCELADA);
        poliza.getRiesgos().forEach(riesgo -> riesgo.setEstado(EstadoRiesgo.CANCELADO));
        polizaRepository.save(poliza);
        coreClient.enviarActualizacion(poliza.getId());

        return PolizaResponse.desde(poliza);
    }

    protected Poliza buscarPorId(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la póliza con id " + id + "."));
    }
}
