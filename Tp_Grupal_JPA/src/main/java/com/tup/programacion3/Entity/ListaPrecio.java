package com.tup.programacion3.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "LISTAPRECIO")
public class ListaPrecio extends AuditoriaApp{

    @Column(nullable = false)
    private String codigo;
    @Column(nullable = false)
    private String denominacion;

    public ListaPrecio() {
    }

    public ListaPrecio(String codigo, String denominacion) {
        super();
        this.codigo = codigo;
        this.denominacion = denominacion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    @Override
    public String toString() {
        return "ListaPrecio{" +
                "codigo='" + codigo + '\'' +
                ", denominacion='" + denominacion + '\'' +
                "} " + super.toString();
    }
}
