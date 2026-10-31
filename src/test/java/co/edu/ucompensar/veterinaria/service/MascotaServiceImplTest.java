package co.edu.ucompensar.veterinaria.service;

import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.repository.MascotaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.ucompensar.veterinaria.exception.ValidacionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotaServiceImplTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @InjectMocks
    private MascotaServiceImpl mascotaService;

    private Mascota mascotaValida() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Firulais");
        mascota.setEspecie("perro");
        mascota.setNombrePropietario("Ana Gómez");
        mascota.setTelefonoPropietario("3001234567");
        return mascota;
    }

    @Test
    void registrar_datosValidos_guardaMascota() {
        // Arrange
        Mascota entrada = mascotaValida();
        Mascota guardada = mascotaValida();
        guardada.setId(1L);
        when(mascotaRepository.save(entrada)).thenReturn(guardada);

        // Act
        Mascota resultado = mascotaService.registrar(entrada);

        // Assert
        assertEquals(1L, resultado.getId());
        verify(mascotaRepository).save(entrada);
    }

    @Test
    void registrar_nombreVacio_lanzaValidacionYNoGuarda() {
        // Arrange
        Mascota mascota = mascotaValida();
        mascota.setNombre("");

        // Act + Assert
        assertThrows(ValidacionException.class, () -> mascotaService.registrar(mascota));
        verify(mascotaRepository, never()).save(any());
    }

    @Test
    void registrar_especieVacia_lanzaValidacionYNoGuarda() {
        // Arrange
        Mascota mascota = mascotaValida();
        mascota.setEspecie("");

        // Act + Assert
        assertThrows(ValidacionException.class, () -> mascotaService.registrar(mascota));
        verify(mascotaRepository, never()).save(any());
    }
}
