package com.tup.programacion3.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CONDICIONIVA")
public class CondicionIva extends AuditoriaApp{
    @Column(nullable = false)
    private int codigoAfip;
    @Column(nullable = false)
    private String denominacion;

    public CondicionIva() {
    }

    public CondicionIva(int codigoAfip, String denominacion) {
        this.codigoAfip = codigoAfip;
        this.denominacion = denominacion;
    }

    public int getCodigoAfip() {
        return codigoAfip;
    }

    public void setCodigoAfip(int codigoAfip) {
        this.codigoAfip = codigoAfip;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    @Override
    public String toString() {
        return "CondicionIva{" +
                "codigoAfip=" + codigoAfip +
                ", denominacion='" + denominacion + '\'' +
                "} " + super.toString();
    }
}
