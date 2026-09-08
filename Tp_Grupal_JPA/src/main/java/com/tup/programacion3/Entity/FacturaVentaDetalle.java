package com.tup.programacion3.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "FACTURAVENTADETALLE")
public class FacturaVentaDetalle extends EntityId {
    @ManyToOne
    @JoinColumn(nullable = false)
    private FacturaVenta factura;
    @ManyToOne
    @JoinColumn(nullable = false)
    private ListaPrecioArticulo listaPrecioArticulo;
    private String descripcion;
    @Column(nullable = false)
    private double cantidad;
    @Column(nullable = false)
    private double precioUnitario;
    private double porcentajeBonificacion;
    private double importeNeto;
    private double importeIva;
    @Column(nullable = false)
    private double importeSubtotal;

    public FacturaVentaDetalle() {
    }

    public FacturaVentaDetalle(FacturaVenta factura, double importeNeto, double porcentajeBonificacion, double precioUnitario, ListaPrecioArticulo listaPrecioArticulo, String descripcion, double cantidad, double importeIva, double importeSubtotal) {
        this.factura = factura;
        this.importeNeto = importeNeto;
        this.porcentajeBonificacion = porcentajeBonificacion;
        this.precioUnitario = precioUnitario;
        this.listaPrecioArticulo = listaPrecioArticulo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.importeIva = importeIva;
        this.importeSubtotal = importeSubtotal;
    }

    public FacturaVenta getFactura() {
        return factura;
    }

    public void setFactura(FacturaVenta factura) {
        this.factura = factura;
    }

    public double getImporteSubtotal() {
        return importeSubtotal;
    }

    public void setImporteSubtotal(double importeSubtotal) {
        this.importeSubtotal = importeSubtotal;
    }

    public double getImporteIva() {
        return importeIva;
    }

    public void setImporteIva(double importeIva) {
        this.importeIva = importeIva;
    }

    public double getImporteNeto() {
        return importeNeto;
    }

    public void setImporteNeto(double importeNeto) {
        this.importeNeto = importeNeto;
    }

    public double getPorcentajeBonificacion() {
        return porcentajeBonificacion;
    }

    public void setPorcentajeBonificacion(double porcentajeBonificacion) {
        this.porcentajeBonificacion = porcentajeBonificacion;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public ListaPrecioArticulo getListaPrecioArticulo() {
        return listaPrecioArticulo;
    }

    public void setListaPrecioArticulo(ListaPrecioArticulo listaPrecioArticulo) {
        this.listaPrecioArticulo = listaPrecioArticulo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "FacturaVentaDetalle{" +
                "factura_id=" + (factura != null ? factura.getId() : null) +
                ", listaPrecioArticulo=" + listaPrecioArticulo +
                ", descripcion='" + descripcion + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", porcentajeBonificacion=" + porcentajeBonificacion +
                ", importeNeto=" + importeNeto +
                ", importeIva=" + importeIva +
                ", importeSubtotal=" + importeSubtotal +
                "} " + super.toString();
    }
}
