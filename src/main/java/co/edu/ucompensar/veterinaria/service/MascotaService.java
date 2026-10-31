package co.edu.ucompensar.veterinaria.service;

import co.edu.ucompensar.veterinaria.model.Mascota;

public interface MascotaService {

    Mascota registrar(Mascota mascota);
    Mascota consultar(Long id);
}
