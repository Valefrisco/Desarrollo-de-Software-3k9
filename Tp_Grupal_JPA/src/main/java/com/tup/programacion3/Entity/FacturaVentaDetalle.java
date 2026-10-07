package com.tup.programacion3.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
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





}
