package co.edu.ucompensar.veterinaria.service;

import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.repository.MascotaRepository;
import co.edu.ucompensar.veterinaria.exception.ValidacionException;
import co.edu.ucompensar.veterinaria.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

@Service
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;

    public MascotaServiceImpl(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    public Mascota registrar(Mascota mascota) {
        exigirTexto(mascota.getNombre(), "El nombre es obligatorio");
        exigirTexto(mascota.getEspecie(), "La especie es obligatoria");
        exigirTexto(mascota.getNombrePropietario(), "El nombre del propietario es obligatorio");
        exigirTexto(mascota.getTelefonoPropietario(), "El teléfono del propietario es obligatorio");
        return mascotaRepository.save(mascota);
    }

    @Override
    public Mascota consultar(Long id) {
        return buscarPorId(id);
    }

    @Override
    public Mascota actualizarPropietario(Long id, String nombrePropietario, String telefonoPropietario) {
        Mascota mascota = buscarPorId(id);
        mascota.setNombrePropietario(nombrePropietario);
        mascota.setTelefonoPropietario(telefonoPropietario);
        return mascotaRepository.save(mascota);
    }

    private void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException(mensaje);
        }
    }

    private Mascota buscarPorId(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no existe: " + id));
    }
}
