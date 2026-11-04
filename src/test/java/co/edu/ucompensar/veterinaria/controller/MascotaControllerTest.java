package co.edu.ucompensar.veterinaria.controller;

import co.edu.ucompensar.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.ucompensar.veterinaria.exception.ValidacionException;
import co.edu.ucompensar.veterinaria.model.Cita;
import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.service.MascotaService;
import java.util.List;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MascotaControllerTest {

    @Mock
    private MascotaService mascotaService;

    @InjectMocks
    private MascotaController mascotaController;

    private MockMvc mockMvc;

    private static final String JSON_MASCOTA = """
            {"nombre":"Firulais","especie":"perro","raza":"criollo","fechaNacimiento":"2021-05-10",
             "nombrePropietario":"Ana Gómez","telefonoPropietario":"3001234567"}
            """;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(mascotaController)
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    private Mascota mascotaGuardada() {
        Mascota mascota = new Mascota();
        mascota.setId(1L);
        mascota.setNombre("Firulais");
        mascota.setEspecie("perro");
        mascota.setNombrePropietario("Ana Gómez");
        mascota.setTelefonoPropietario("3001234567");
        return mascota;
    }

    @Test
    void registrar_datosValidos_responde201ConId() throws Exception {
        // Arrange
        when(mascotaService.registrar(any(Mascota.class))).thenReturn(mascotaGuardada());

        // Act + Assert
        mockMvc.perform(post("/api/mascotas").contentType(MediaType.APPLICATION_JSON).content(JSON_MASCOTA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Firulais"));
    }

    @Test
    void registrar_datosInvalidos_responde400ConMensaje() throws Exception {
        // Arrange
        when(mascotaService.registrar(any(Mascota.class)))
                .thenThrow(new ValidacionException("El nombre es obligatorio"));

        // Act + Assert
        mockMvc.perform(post("/api/mascotas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El nombre es obligatorio"));
    }

    @Test
    void registrar_jsonMalFormado_responde400() throws Exception {
        mockMvc.perform(post("/api/mascotas").contentType(MediaType.APPLICATION_JSON).content("{nombre:"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mascotaService);
    }

    @Test
    void consultar_existente_responde200ConSusCitas() throws Exception {
        // Arrange
        Mascota mascota = mascotaGuardada();
        Cita cita = new Cita();
        cita.setId(7L);
        cita.setEstado("PENDIENTE");
        cita.setMascota(mascota);
        mascota.setCitas(List.of(cita));
        when(mascotaService.consultar(1L)).thenReturn(mascota);

        // Act + Assert
        mockMvc.perform(get("/api/mascotas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Firulais"))
                .andExpect(jsonPath("$.citas[0].id").value(7))
                .andExpect(jsonPath("$.citas[0].estado").value("PENDIENTE"));
    }

    @Test
    void consultar_inexistente_responde404() throws Exception {
        // Arrange
        when(mascotaService.consultar(99L)).thenThrow(new RecursoNoEncontradoException("Mascota no existe: 99"));

        // Act + Assert
        mockMvc.perform(get("/api/mascotas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Mascota no existe: 99"));
    }

    @Test
    void consultar_idNoNumerico_responde400() throws Exception {
        mockMvc.perform(get("/api/mascotas/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_existente_responde200ConDatosNuevos() throws Exception {
        // Arrange
        Mascota actualizada = mascotaGuardada();
        actualizada.setNombrePropietario("Luis Pérez");
        when(mascotaService.actualizarPropietario(1L, "Luis Pérez", "3109876543")).thenReturn(actualizada);

        // Act + Assert
        mockMvc.perform(put("/api/mascotas/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePropietario\":\"Luis Pérez\",\"telefonoPropietario\":\"3109876543\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombrePropietario").value("Luis Pérez"));
    }

    @Test
    void actualizar_inexistente_responde404() throws Exception {
        // Arrange
        when(mascotaService.actualizarPropietario(eq(99L), any(), any()))
                .thenThrow(new RecursoNoEncontradoException("Mascota no existe: 99"));

        // Act + Assert
        mockMvc.perform(put("/api/mascotas/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePropietario\":\"Luis Pérez\",\"telefonoPropietario\":\"3109876543\"}"))
                .andExpect(status().isNotFound());
    }
}
