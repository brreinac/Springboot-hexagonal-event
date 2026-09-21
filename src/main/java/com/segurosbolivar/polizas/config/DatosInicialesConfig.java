package com.segurosbolivar.polizas.config;

import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import com.segurosbolivar.polizas.entity.Poliza;
import com.segurosbolivar.polizas.entity.Riesgo;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.repository.PolizaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarPolizasDeEjemplo(PolizaRepository polizaRepository) {
        return args -> {
            if (polizaRepository.count() > 0) {
                return;
            }

            Poliza individual = poliza(TipoPoliza.INDIVIDUAL, EstadoPoliza.ACTIVA, 12, "1000000.00");
            individual.agregarRiesgo(riesgo("Apartamento 101", "Riesgo de arrendamiento individual", EstadoRiesgo.ACTIVO));

            Poliza colectiva = poliza(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA, 12, "1500000.00");
            colectiva.agregarRiesgo(riesgo("Apartamento 201", "Riesgo colectivo de arrendamiento", EstadoRiesgo.ACTIVO));
            colectiva.agregarRiesgo(riesgo("Apartamento 202", "Riesgo colectivo de arrendamiento", EstadoRiesgo.ACTIVO));

            Poliza cancelada = poliza(TipoPoliza.COLECTIVA, EstadoPoliza.CANCELADA, 6, "900000.00");
            cancelada.agregarRiesgo(riesgo("Local 301", "Riesgo de póliza cancelada", EstadoRiesgo.CANCELADO));
            cancelada.agregarRiesgo(riesgo("Local 302", "Riesgo de póliza cancelada", EstadoRiesgo.CANCELADO));

            polizaRepository.save(individual);
            polizaRepository.save(colectiva);
            polizaRepository.save(cancelada);
        };
    }

    private Poliza poliza(TipoPoliza tipo, EstadoPoliza estado, int periodoMeses, String canon) {
        BigDecimal canonMensual = new BigDecimal(canon);
        return new Poliza(tipo, estado, periodoMeses, canonMensual,
                canonMensual.multiply(BigDecimal.valueOf(periodoMeses)));
    }

    private Riesgo riesgo(String nombre, String descripcion, EstadoRiesgo estado) {
        return new Riesgo(nombre, descripcion, estado);
    }
}
