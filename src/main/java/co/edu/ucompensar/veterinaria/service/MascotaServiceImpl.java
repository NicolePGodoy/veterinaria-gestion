package co.edu.ucompensar.veterinaria.service;

import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.repository.MascotaRepository;
import org.springframework.stereotype.Service;
import co.edu.ucompensar.veterinaria.expetios.ValidationException;

import java.util.List;
@Service
public class MascotaServiceImpl implements MascotaService {

    private MascotaRepository mascotaRepository;
    public MascotaServiceImpl(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }
    @Override
    public Mascota registrar(Mascota mascota) {

        if (mascota.getNombre() == null || mascota.getNombre().isBlank()) {
            throw new ValidationException("El nombre es obligatorio");
        }
        if (mascota.getNombre() == null || mascota.getNombre().isBlank()) {
            throw new ValidationException("La especie es  obligatoria");
        return mascotaRepository.save(mascota);
    }

    @Override
    public Mascota actualizar(Mascota mascota, Long id) {
        return mascota1 = mascotaRepository.findById(Id).get();
        mascota1
    }

    @Override
    public boolean eliminar(Long id) {
        Mascota mascota1 = ObtenerPorId(Id);
        if (mascota1 != null)
            return mascota1 = mascotaRepository.findById();
    }

    @Override
    public Mascota ObtenerPorId(Long id) {
        return null;
    }

    @Override
    public List<Mascota> listarTodo() {
        return List.of();
    }
}
// implementar peticiones que se realizan , metodos