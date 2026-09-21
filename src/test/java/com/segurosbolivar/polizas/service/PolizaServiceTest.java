package com.segurosbolivar.polizas.service;

import com.segurosbolivar.polizas.config.ConfiguracionPolizas;
import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.Poliza;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.exception.ReglaNegocioException;
import com.segurosbolivar.polizas.repository.PolizaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private ConfiguracionPolizas configuracionPolizas;

    @Mock
    private CoreClient coreClient;

    @InjectMocks
    private PolizaService polizaService;

    @Test
    void renovarCalculaCanonYPrimaSegunElIpcConfigurado() {
        Poliza poliza = poliza(EstadoPoliza.ACTIVA);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));
        when(configuracionPolizas.getIpc()).thenReturn(new BigDecimal("0.05"));

        polizaService.renovar(1L);

        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
        assertThat(poliza.getCanonMensual()).isEqualByComparingTo("1050000.00");
        assertThat(poliza.getPrima()).isEqualByComparingTo("12600000.00");
        verify(polizaRepository).save(poliza);
    }

    @Test
    void renovarNoPermiteUnaPolizaCancelada() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza(EstadoPoliza.CANCELADA)));

        assertThatThrownBy(() -> polizaService.renovar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("No se puede renovar una póliza cancelada.");

        verify(polizaRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(coreClient, never()).enviarActualizacion(org.mockito.ArgumentMatchers.any());
    }

    private Poliza poliza(EstadoPoliza estado) {
        return new Poliza(TipoPoliza.INDIVIDUAL, estado, 12,
                new BigDecimal("1000000.00"), new BigDecimal("12000000.00"));
    }
}
