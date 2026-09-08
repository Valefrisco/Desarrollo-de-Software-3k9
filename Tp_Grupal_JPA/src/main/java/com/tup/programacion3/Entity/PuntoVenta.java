package com.tup.programacion3.Entity;

import jakarta.persistence.*;

public class PuntoVenta extends AuditoriaApp{
    @Column(nullable = false)
    private int numero;
    private String descripcion;
    private String tipoEmision;
    private String domicilioComercial;

    public PuntoVenta() {
    }

    public PuntoVenta(int numero, String domicilioComercial, String tipoEmision, String descripcion) {
        this.numero = numero;
        this.domicilioComercial = domicilioComercial;
        this.tipoEmision = tipoEmision;
        this.descripcion = descripcion;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getDomicilioComercial() {
        return domicilioComercial;
    }

    public void setDomicilioComercial(String domicilioComercial) {
        this.domicilioComercial = domicilioComercial;
    }

    public String getTipoEmision() {
        return tipoEmision;
    }

    public void setTipoEmision(String tipoEmision) {
        this.tipoEmision = tipoEmision;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "PuntoVenta{" +
                "numero=" + numero +
                ", descripcion='" + descripcion + '\'' +
                ", tipoEmision='" + tipoEmision + '\'' +
                ", domicilioComercial='" + domicilioComercial + '\'' +
                "} " + super.toString();
    }
}
