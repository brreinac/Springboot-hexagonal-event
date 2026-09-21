package com.segurosbolivar.polizas;

import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import com.segurosbolivar.polizas.entity.Poliza;
import com.segurosbolivar.polizas.entity.Riesgo;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.repository.PolizaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GestionPolizasIntegrationTest {

    private static final String API_KEY = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolizaRepository polizaRepository;

    @Test
    void listaPolizas() throws Exception {
        mockMvc.perform(get("/polizas").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void filtraPolizasPorTipo() throws Exception {
        mockMvc.perform(get("/polizas?tipo=INDIVIDUAL").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("INDIVIDUAL"));
    }

    @Test
    void filtraPolizasPorEstado() throws Exception {
        mockMvc.perform(get("/polizas?estado=CANCELADA").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("CANCELADA"));
    }

    @Test
    void consultaRiesgosDePoliza() throws Exception {
        Long polizaId = polizaColectivaActiva().getId();

        mockMvc.perform(get("/polizas/{id}/riesgos", polizaId).header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void renuevaPolizaActivaConIpc() throws Exception {
        Long polizaId = polizaIndividualActiva().getId();

        mockMvc.perform(post("/polizas/{id}/renovar", polizaId).header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RENOVADA"));

        Poliza renovada = polizaRepository.findById(polizaId).orElseThrow();
        assertThat(renovada.getCanonMensual()).isEqualByComparingTo("1050000.00");
        assertThat(renovada.getPrima()).isEqualByComparingTo("12600000.00");
    }

    @Test
    void noRenuevaPolizaCancelada() throws Exception {
        Long polizaId = polizaCancelada().getId();

        mockMvc.perform(post("/polizas/{id}/renovar", polizaId).header("x-api-key", API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede renovar una póliza cancelada."));
    }

    @Test
    void cancelaPoliza() throws Exception {
        Long polizaId = polizaIndividualActiva().getId();

        mockMvc.perform(post("/polizas/{id}/cancelar", polizaId).header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void cancelarPolizaCancelaSusRiesgos() throws Exception {
        Long polizaId = polizaColectivaActiva().getId();

        mockMvc.perform(post("/polizas/{id}/cancelar", polizaId).header("x-api-key", API_KEY))
                .andExpect(status().isOk());

        Poliza cancelada = polizaRepository.findById(polizaId).orElseThrow();
        assertThat(cancelada.getRiesgos()).allMatch(riesgo -> riesgo.getEstado() == EstadoRiesgo.CANCELADO);
    }

    @Test
    void agregaRiesgoAPolizaColectiva() throws Exception {
        Long polizaId = polizaColectivaActiva().getId();

        mockMvc.perform(post("/polizas/{id}/riesgos", polizaId)
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Apartamento 203\",\"descripcion\":\"Nuevo riesgo colectivo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.polizaId").value(polizaId));
    }

    @Test
    void noAgregaRiesgoAPolizaIndividual() throws Exception {
        Long polizaId = polizaIndividualActiva().getId();

        mockMvc.perform(post("/polizas/{id}/riesgos", polizaId)
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Riesgo inválido\",\"descripcion\":\"No debe crearse\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Solo se pueden agregar riesgos a pólizas COLECTIVAS."));
    }

    @Test
    void cancelaRiesgoActivo() throws Exception {
        Riesgo riesgo = primerRiesgoActivo(polizaColectivaActiva());

        mockMvc.perform(post("/riesgos/{id}/cancelar", riesgo.getId()).header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void respondeNotFoundCuandoElRecursoNoExiste() throws Exception {
        mockMvc.perform(get("/polizas/{id}/riesgos", 999999L).header("x-api-key", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaApiKeyInvalida() throws Exception {
        mockMvc.perform(get("/polizas").header("x-api-key", "incorrecta"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El header x-api-key es obligatorio o no es válido."));
    }

    @Test
    void registraEventoEnCoreMock() throws Exception {
        mockMvc.perform(post("/core-mock/evento")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Evento registrado en el CORE mock."));
    }

    private Poliza polizaIndividualActiva() {
        return buscarPoliza(TipoPoliza.INDIVIDUAL, EstadoPoliza.ACTIVA);
    }

    private Poliza polizaColectivaActiva() {
        return buscarPoliza(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA);
    }

    private Poliza polizaCancelada() {
        return buscarPoliza(TipoPoliza.COLECTIVA, EstadoPoliza.CANCELADA);
    }

    private Poliza buscarPoliza(TipoPoliza tipo, EstadoPoliza estado) {
        return polizaRepository.findAll().stream()
                .filter(poliza -> poliza.getTipo() == tipo && poliza.getEstado() == estado)
                .findFirst()
                .orElseThrow();
    }

    private Riesgo primerRiesgoActivo(Poliza poliza) {
        return poliza.getRiesgos().stream()
                .filter(riesgo -> riesgo.getEstado() == EstadoRiesgo.ACTIVO)
                .min(Comparator.comparing(Riesgo::getId))
                .orElseThrow();
    }
}
