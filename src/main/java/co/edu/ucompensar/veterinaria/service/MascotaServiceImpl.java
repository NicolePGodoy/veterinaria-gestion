package co.edu.ucompensar.veterinaria.service;

import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.repository.MascotaRepository;
import org.springframework.stereotype.Service;

@Service
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;

    public MascotaServiceImpl(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    public Mascota registrar(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }
}
