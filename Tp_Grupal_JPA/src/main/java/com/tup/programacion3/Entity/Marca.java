package com.tup.programacion3.Entity;

import jakarta.persistence.*;
@Entity
@Table(name = "MARCAS")
public class Marca extends AuditoriaApp{
@Column(nullable = false)
    private String denominacion;
@Column(nullable = false)
    private Integer codigo;

    public Marca() {
    }

    public Marca(String denominacion, Integer codigo) {
        super();
        this.denominacion = denominacion;
        this.codigo = codigo;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return "Marca{" +
                "denominacion='" + denominacion + '\'' +
                ", codigo=" + codigo +
                "} " + super.toString();
    }
}
