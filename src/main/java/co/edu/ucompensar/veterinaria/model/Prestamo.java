package co.edu.ucompensar.veterinaria.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate fechaPrestamo;
    @OneToMany(mappedBy = "prestamo",fetch = FetchType.EAGER, cascade = CascadeType.ALL) //ayuda a evitar la creacion de una tabla adicional, cuando es una de muchos a  uno o de uno a muchos
    private List<DetallesPrestamo> destalles;
}
