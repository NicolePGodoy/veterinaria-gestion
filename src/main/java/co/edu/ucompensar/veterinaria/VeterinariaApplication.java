package co.edu.ucompensar.veterinaria;

import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.repository.MascotaRepository;
import co.edu.ucompensar.veterinaria.service.MascotaServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VeterinariaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeterinariaApplication.class, args);

        MascotaRepository mascotaRepository;
        MascotaServiceImpl mascotaService =new MascotaServiceImpl(mascotaRepository);

        Mascota mascota =new Mascota();
        mascota.setId(1L);
        mascota.setN
        mascota.setEspecie();
    }

}
