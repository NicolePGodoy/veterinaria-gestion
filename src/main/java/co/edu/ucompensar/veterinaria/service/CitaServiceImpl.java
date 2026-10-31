package co.edu.ucompensar.veterinaria.service;

import co.edu.ucompensar.veterinaria.model.Cita;
import co.edu.ucompensar.veterinaria.repository.CitaRepository;
import co.edu.ucompensar.veterinaria.repository.MascotaRepository;
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
        mascotaRepository.findById(mascotaId).orElseThrow();
        cita.setEstado(PENDIENTE);
        return citaRepository.save(cita);
    }
}
