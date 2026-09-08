package com.tup.programacion3.Entity;

import jakarta.persistence.*;

@Entity
@Table (name = "TIPOMONEDA")
public class TipoMoneda extends AuditoriaApp{

    @Column(nullable = false)
    private String codigoAfip;
    @Column(nullable = false)
    private String denominacion;
    @Column(nullable = false)
    private String simbolo;

    public TipoMoneda() {
    }

    public TipoMoneda(String codigoAfip, String simbolo, String denominacion) {
        this.codigoAfip = codigoAfip;
        this.simbolo = simbolo;
        this.denominacion = denominacion;
    }

    @Override
    public String toString() {
        return "TipoMoneda{" +
                "codigoAfip='" + codigoAfip + '\'' +
                ", denominacion='" + denominacion + '\'' +
                ", simbolo='" + simbolo + '\'' +
                "} " + super.toString();
    }
}
