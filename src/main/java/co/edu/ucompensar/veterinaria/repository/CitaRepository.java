package co.edu.ucompensar.veterinaria.repository;


import co.edu.ucompensar.veterinaria.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Long> {
}