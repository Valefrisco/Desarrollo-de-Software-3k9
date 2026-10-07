package com.tup.programacion3.Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true, exclude = "detalles")
@EqualsAndHashCode(exclude = "detalles")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "FACTURAVENTA")
public class FacturaVenta extends AuditoriaApp {
    private Long numero;
    @Column(nullable = false)
    private LocalDateTime fechaEmision;
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


    public void addDetalle(FacturaVentaDetalle detalle) { //NO PIDE HACER ESTE METODO PERO BUENO LO AGREGUE PORQUE PARECE LOGICO
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        this.detalles.add(detalle);
        detalle.setFactura(this);
    }


}