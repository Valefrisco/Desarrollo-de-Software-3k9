package com.tup.programacion3.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "RUBROS")
public class Rubro extends AuditoriaApp{

@Column(nullable = false)
    private String denominacion;
@Column(nullable = false)
    private Integer codigo;

    public Rubro() {
    }

    public Rubro(String denominacion, Integer codigo) {
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
        return "Rubro{" +
                "denominacion='" + denominacion + '\'' +
                ", codigo=" + codigo +
                "} " + super.toString();
    }
}
