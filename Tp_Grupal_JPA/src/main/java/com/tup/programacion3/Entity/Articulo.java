package com.tup.programacion3.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "ARTICULOSS")
public class Articulo extends AuditoriaApp{
    @ManyToOne
    @JoinColumn(name = "Rubro_id")
    private Rubro rubro;
    @Column(nullable = false)
    private String codigo;
    @Column(nullable = false)
    private String denominacion;
    @ManyToOne
    @JoinColumn(name = "Marca_id")
    private Marca marca;

    public Articulo() {
    }

    public Articulo(Rubro rubro, Marca marca, String denominacion, String codigo) {
        super();
        this.rubro = rubro;
        this.marca = marca;
        this.denominacion = denominacion;
        this.codigo = codigo;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return "Articulo{" +
                "codigo='" + codigo + '\'' +
                ", denominacion='" + denominacion + '\'' +
                ", rubro_id=" + (rubro != null ? rubro.getId() : "null") +
                ", marca_id=" + (marca != null ? marca.getId() : "null") +
                "} " + super.toString();
    }
}
