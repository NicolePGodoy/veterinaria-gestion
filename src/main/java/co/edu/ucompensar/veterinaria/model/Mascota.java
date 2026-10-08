package co.edu.ucompensar.veterinaria.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String especie;
    private String nombre;
    private  String NombrePropetario;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombrePropetario() {
        return NombrePropetario;
    }

    public void setNombrePropetario(String nombrePropetario) {
        NombrePropetario = nombrePropetario;
    }

    public String getTelefonoPropietario() {
        return TelefonoPropietario;
    }

    public void setTelefonoPropietario(String telefonoPropietario) {
        TelefonoPropietario = telefonoPropietario;
    }

    private  String TelefonoPropietario;

}
