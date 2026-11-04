package co.edu.ucompensar.veterinaria.controller;

import co.edu.ucompensar.veterinaria.model.Mascota;
import co.edu.ucompensar.veterinaria.service.MascotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    /** RF-01: POST /api/mascotas -> 201 Created con la mascota y su id. */
    @PostMapping
    public ResponseEntity<Mascota> registrar(@RequestBody Mascota mascota) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaService.registrar(mascota));
    }

    /** RF-02: GET /api/mascotas/{id} -> 200 OK con la mascota y sus citas. */
    @GetMapping("/{id}")
    public ResponseEntity<Mascota> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.consultar(id));
    }

    /** RF-05: PUT /api/mascotas/{id} -> 200 OK con los datos del propietario actualizados. */
    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizar(@PathVariable Long id, @RequestBody Mascota datos) {
        return ResponseEntity.ok(mascotaService.actualizarPropietario(
                id, datos.getNombrePropietario(), datos.getTelefonoPropietario()));
    }
}
