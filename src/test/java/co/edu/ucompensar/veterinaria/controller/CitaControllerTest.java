package co.edu.ucompensar.veterinaria.controller;

import co.edu.ucompensar.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.ucompensar.veterinaria.exception.ReglaNegocioException;
import co.edu.ucompensar.veterinaria.model.Cita;
import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
}