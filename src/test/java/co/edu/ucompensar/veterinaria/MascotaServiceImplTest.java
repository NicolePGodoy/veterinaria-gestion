package co.edu.ucompensar.veterinaria;

import co.edu.ucompensar.veterinaria.repository.MascotaRepository;
import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.service.MascotaServiceImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MascotaServiceImplTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @InjectMocks
    private MascotaServiceImpl mascotaService;


    Mascota crearMascota() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Pecas");
        mascota.setEspecie("Dalmata");
        mascota.setNombrePropietario("kevin");
        mascota.setTelefomopropietario("343434");
        return mascota;
    }

    @Test
    void registrar_mascota() {
        Mascota mascota = crearMascota();
        Mascota mascota2 = mascota;
        mascota2.setId(22l);

        when(mascotaRepository.save(mascota)).thenReturn(mascota2);

        Mascota mascotaBD = mascotaService.registrar(mascota);

        //recordar que el llamo como numero a la mascota con el numero L pero le toco covertitlo en String para que lograra encontrarlo
        Assert.assertEquals("22", mascotaBD.getNombre().toString());
        verify(mascotaRepository).save(mascota);
    }

    @Test
    void actualizar_mascota() {
        //LA MASCOTA DE LA BASE DE DATOS
        Mascota mascotaDB = crearMascota();
        mascotaDB.setId(22l);

        //ESTA ES LA ACTUALIZZaccion nuevos datos
        Mascota nuevaMascota = new Mascota();
        nuevaMascota.setNombre("Pecuea");

        when(mascotaRepository.findById(22l)).thenReturn(Optional.of(mascotaDB));
        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mascotaDB);
        //la mascota actualizada
        Mascota mascotaActualizada = mascotaService.actualizar(nuevaMascota, 22l);
        Assert.assertEquals("Pecuea", mascotaActualizada.getNombre());
        verify(mascotaRepository).save(mascotaDB);
    }
}
