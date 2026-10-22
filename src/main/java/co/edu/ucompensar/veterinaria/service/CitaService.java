package co.edu.ucompensar.veterinaria.service;


import co.edu.ucompensar.veterinaria.model.Cita;

import java.util.List;

public interface CitaService {
    Cita registrar(Cita cita);
    Cita actualizar(Cita cita, Long id);
    boolean eliminar(Long id);

    Cita obtenerPorId(Long id);
    List<Cita> listarTodo();
}