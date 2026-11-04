package co.edu.ucompensar.veterinaria.controller;

import co.edu.ucompensar.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.ucompensar.veterinaria.exception.ReglaNegocioException;
import co.edu.ucompensar.veterinaria.exception.ValidacionException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce las excepciones del servicio a los códigos HTTP de los diagramas de secuencia:
 * datos inválidos -> 400, no encontrado -> 404, regla de negocio violada -> 409.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<Map<String, String>> validacion(ValidacionException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> reglaNegocio(ReglaNegocioException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> peticionMalFormada(Exception e) {
        return respuesta(HttpStatus.BAD_REQUEST, "La petición tiene datos con formato inválido");
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
