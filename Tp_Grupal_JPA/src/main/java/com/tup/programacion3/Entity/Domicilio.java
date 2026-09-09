package com.tup.programacion3.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DOMICILIOS")
public class Domicilio extends EntityId{
    private String nombreCalle;
    private String numeroCalle;

    public Domicilio() {
    }

    public Domicilio(String numeroCalle, String nombreCalle) {
        this.numeroCalle = numeroCalle;
        this.nombreCalle = nombreCalle;
    }

    public String getNombreCalle() {
        return nombreCalle;
    }

    public void setNombreCalle(String nombreCalle) {
        this.nombreCalle = nombreCalle;
    }

    public String getNumeroCalle() {
        return numeroCalle;
    }

    public void setNumeroCalle(String numeroCalle) {
        this.numeroCalle = numeroCalle;
    }

    @Override
    public String toString() {
        return "Domicilio{" +
                "nombreCalle='" + nombreCalle + '\'' +
                ", numeroCalle='" + numeroCalle + '\'' +
                "} " + super.toString();
    }
}
