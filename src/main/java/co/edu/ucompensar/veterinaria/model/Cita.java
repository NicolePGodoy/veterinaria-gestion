package co.edu.ucompensar.veterinaria.model;

import jakarta.persistence.*;

    @Entity
    public class Cita {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(optional = false)
        private Mascota mascota;

    }
}
