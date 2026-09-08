package com.tup.programacion3.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "LISTAPRECIOARTICULO")
public class ListaPrecioArticulo extends AuditoriaApp{
    @ManyToOne
    @JoinColumn(nullable = false)
    private ListaPrecio listaPrecio;
    @Column(nullable = false)
    private double precioVenta;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Articulo articulo;

    public ListaPrecioArticulo() {
    }

    public ListaPrecioArticulo(ListaPrecio listaPrecio, Articulo articulo, double precioVenta) {
        this.listaPrecio = listaPrecio;
        this.articulo = articulo;
        this.precioVenta = precioVenta;
    }

    public ListaPrecio getListaPrecio() {
        return listaPrecio;
    }

    public void setListaPrecio(ListaPrecio listaPrecio) {
        this.listaPrecio = listaPrecio;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    @Override
    public String toString() {
        return "ListaPrecioArticulo{" +
                "listaPrecio=" + listaPrecio +
                ", precioVenta=" + precioVenta +
                ", articulo=" + articulo +
                "} " + super.toString();
    }
}
