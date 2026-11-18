package com.tup.programacion3.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true, exclude = "detalles")
@EqualsAndHashCode(callSuper = true, exclude = "detalles")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "FACTURAVENTA")
public class FacturaVenta extends AuditoriaApp {
    private Long numero;
    @Column(nullable = false)
    private LocalDateTime fechaEmision;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private PuntoVenta puntoVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condicion_iva_id", nullable = false)
    private CondicionIva condicionIva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_moneda_id", nullable = false)
    private TipoMoneda tipoMoneda;

    private double importeCobrado;
    private double importeSaldo;
    @Column(nullable = false)
    private double importeTotal;
    private String cae;
    @Temporal(TemporalType.DATE)
    private Date caeFechaVencimiento;
    private String resultadoAfip;
    private String motivoRechazo;
    // Datos que devuelve ARCA al facturar (estado FACTURADA): numero del comprobante
    // en ARCA (Factura C, correlativo por punto de venta de ARCA) y fecha de la autorizacion.
    private Long numeroArca;
    private Integer puntoVentaArca;        // punto de venta de ARCA con el que se facturo
    private LocalDateTime fechaFacturacion;
    // Anulacion de una FACTURADA: en ARCA no se borra, se emite una Nota de Credito C
    // asociada a la factura. Estos son los datos que devuelve ARCA para esa nota de credito.
    private Long numeroNotaCredito;
    private String caeNotaCredito;
    @Column(nullable = false)
    private String estado;
    @Temporal(TemporalType.DATE)
    private Date fechaAnulacion;
    private String observaciones;

    @Builder.Default
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacturaVentaDetalle> detalles = new ArrayList<>();
    // <<< FIN AGREGADO

    public void addDetalle(FacturaVentaDetalle detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        this.detalles.add(detalle);
        detalle.setFactura(this);
    }
}