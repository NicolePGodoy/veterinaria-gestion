package co.edu.ucompensar.veterinaria.controller;

import co.edu.ucompensar.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.ucompensar.veterinaria.exception.ReglaNegocioException;
import co.edu.ucompensar.veterinaria.model.Cita;
import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.service.CitaService;
import co.edu.ucompensar.veterinaria.exception.ManejadorErrores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.ucompensar.veterinaria.exception.ValidacionException;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class CitaControllerTest {

    @Mock
    private CitaService citaService;

    @InjectMocks
    private CitaController citaController;

    private MockMvc mockMvc;

    private static final String JSON_CITA = """
            {"fecha":"2030-01-15T10:00:00","motivo":"vacunación","veterinarioAsignado":"Dra. Rojas",
             "mascota":{"id":1}}
            """;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(citaController)
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    private Cita citaGuardada() {
        Mascota mascota = new Mascota();
        mascota.setId(1L);
        Cita cita = new Cita();
        cita.setId(7L);
        cita.setMascota(mascota);
        cita.setEstado("PENDIENTE");
        return cita;
    }

    @Test
    void agendar_datosValidos_responde201ConEstadoPendiente() throws Exception {
        when(citaService.agendar(any(Cita.class))).thenReturn(citaGuardada());

        mockMvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content(JSON_CITA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void agendar_mascotaInexistente_responde404() throws Exception {
        when(citaService.agendar(any(Cita.class)))
                .thenThrow(new RecursoNoEncontradoException("Mascota no existe: 1"));

        mockMvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content(JSON_CITA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Mascota no existe: 1"));
    }

    @Test
    void agendar_horarioOcupado_responde409() throws Exception {
        when(citaService.agendar(any(Cita.class)))
                .thenThrow(new ReglaNegocioException("La mascota ya tiene una cita en ese horario"));

        mockMvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content(JSON_CITA))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarEstado_pendienteAAtendida_responde200() throws Exception {
        // Arrange
        Cita cita = citaGuardada();
        cita.setEstado("ATENDIDA");
        when(citaService.cambiarEstado(7L, "ATENDIDA")).thenReturn(cita);

        // Act + Assert
        mockMvc.perform(put("/api/citas/7/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ATENDIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));
    }

    @Test
    void actualizarEstado_atendidaACancelada_responde409() throws Exception {
        // Arrange
        when(citaService.cambiarEstado(7L, "CANCELADA"))
                .thenThrow(new ReglaNegocioException("Una cita atendida no puede cancelarse"));

        // Act + Assert
        mockMvc.perform(put("/api/citas/7/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CANCELADA\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Una cita atendida no puede cancelarse"));
    }

    @Test
    void actualizarEstado_citaInexistente_responde404() throws Exception {
        // Arrange
        when(citaService.cambiarEstado(eq(99L), any()))
                .thenThrow(new RecursoNoEncontradoException("Cita no existe: 99"));

        // Act + Assert
        mockMvc.perform(put("/api/citas/99/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ATENDIDA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarEstado_estadoInvalido_responde400() throws Exception {
        // Arrange
        when(citaService.cambiarEstado(7L, "FINALIZADA"))
                .thenThrow(new ValidacionException("El estado debe ser PENDIENTE, ATENDIDA o CANCELADA"));

        // Act + Assert
        mockMvc.perform(put("/api/citas/7/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"FINALIZADA\"}"))
                .andExpect(status().isBadRequest());
    }
}