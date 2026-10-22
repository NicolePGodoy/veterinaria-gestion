package co.edu.ucompensar.veterinaria.expetios;

public class ValidationException extends RuntimeException{

    public ValidationException(String mensaje){
        super(mensaje);
    }
}
