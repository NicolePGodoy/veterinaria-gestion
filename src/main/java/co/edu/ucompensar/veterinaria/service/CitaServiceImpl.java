package co.edu.ucompensar.veterinaria.service;

import co.edu.ucompensar.veterinaria.model.Cita;
import co.edu.ucompensar.veterinaria.repository.CitaRepository;
import co.edu.ucompensar.veterinaria.repository.MascotaRepository;
import co.edu.ucompensar.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.ucompensar.veterinaria.exception.ReglaNegocioException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class CitaServiceImpl implements CitaService {

    private static final String PENDIENTE = "PENDIENTE";

    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;

    public CitaServiceImpl(MascotaRepository mascotaRepository, CitaRepository citaRepository) {
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita agendar(Cita cita) {
        Long mascotaId = cita.getMascota().getId();
        mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no existe: " + mascotaId));
        if (!cita.getFecha().isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("La fecha de la cita debe ser futura");
        }
        cita.setEstado(PENDIENTE);
        return citaRepository.save(cita);
    }
}
