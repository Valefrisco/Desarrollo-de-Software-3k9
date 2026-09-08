package com.tup.programacion3.Entity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Entity
@Table (name = "FACTURAVENTA")
public class FacturaVenta extends AuditoriaApp{
    private Long numero;
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaEmision;
    @ManyToOne
    @JoinColumn(nullable = false)
    private PuntoVenta puntoVenta;
    private double importeCobrado;
    private double importeSaldo;
    @Column(nullable = false)
    private double importeTotal;
    private String cae;
    @Temporal(TemporalType.DATE)
    private Date caeFechaVencimiento;
    private String resultadoAfip;
    private String motivoRechazo;
    @Column(nullable = false)
    private String estado;
    @Temporal(TemporalType.DATE)
    private Date fechaAnulacion;
    private String observaciones;
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL)
    private List<FacturaVentaDetalle> detalles = new ArrayList<>();

    public FacturaVenta() {
    }

    public FacturaVenta(Long numero, Date fechaEmision, PuntoVenta puntoVenta, double importeCobrado, double importeSaldo, double importeTotal, String cae, Date caeFechaVencimiento, String resultadoAfip, String motivoRechazo, String estado, Date fechaAnulacion, String observaciones, List<FacturaVentaDetalle> detalles) {
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.puntoVenta = puntoVenta;
        this.importeCobrado = importeCobrado;
        this.importeSaldo = importeSaldo;
        this.importeTotal = importeTotal;
        this.cae = cae;
        this.caeFechaVencimiento = caeFechaVencimiento;
        this.resultadoAfip = resultadoAfip;
        this.motivoRechazo = motivoRechazo;
        this.estado = estado;
        this.fechaAnulacion = fechaAnulacion;
        this.observaciones = observaciones;
        this.detalles = detalles;
    }

    public void addDetalle(FacturaVentaDetalle detalle) { //NO PIDE HACER ESTE METODO PERO BUENO LO AGREGUE PORQUE PARECE LOGICO
    if (this.detalles == null) {
        this.detalles = new ArrayList<>();
    }
    this.detalles.add(detalle);
    detalle.setFactura(this); 
}
    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Date getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(Date fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getCae() {
        return cae;
    }

    public void setCae(String cae) {
        this.cae = cae;
    }

    public double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public double getImporteSaldo() {
        return importeSaldo;
    }

    public void setImporteSaldo(double importeSaldo) {
        this.importeSaldo = importeSaldo;
    }

    public double getImporteCobrado() {
        return importeCobrado;
    }

    public void setImporteCobrado(double importeCobrado) {
        this.importeCobrado = importeCobrado;
    }

    public PuntoVenta getPuntoVenta() {
        return puntoVenta;
    }

    public void setPuntoVenta(PuntoVenta puntoVenta) {
        this.puntoVenta = puntoVenta;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Date getCaeFechaVencimiento() {
        return caeFechaVencimiento;
    }

    public void setCaeFechaVencimiento(Date caeFechaVencimiento) {
        this.caeFechaVencimiento = caeFechaVencimiento;
    }

    public String getResultadoAfip() {
        return resultadoAfip;
    }

    public void setResultadoAfip(String resultadoAfip) {
        this.resultadoAfip = resultadoAfip;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<FacturaVentaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<FacturaVentaDetalle> detalles) {
        this.detalles = detalles;
    }

    @Override
    public String toString() {
        return "FacturaVenta{" +
                "numero=" + numero +
                ", fechaEmision=" + fechaEmision +
                ", puntoVenta=" + puntoVenta +
                ", importeCobrado=" + importeCobrado +
                ", importeSaldo=" + importeSaldo +
                ", importeTotal=" + importeTotal +
                ", cae='" + cae + '\'' +
                ", caeFechaVencimiento=" + caeFechaVencimiento +
                ", resultadoAfip='" + resultadoAfip + '\'' +
                ", motivoRechazo='" + motivoRechazo + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaAnulacion=" + fechaAnulacion +
                ", observaciones='" + observaciones + '\'' +
                ", detalles=" + detalles +
                "} " + super.toString();
    }
}
