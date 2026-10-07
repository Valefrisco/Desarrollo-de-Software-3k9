package com.tup.programacion3.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PUNTO_VENTA")
public class PuntoVenta extends AuditoriaApp{
    @Column(nullable = false)
    private int numero;
    @Column(nullable = false)
    private String descripcion;
    @Column(nullable = false)
    private String tipoEmision;
    @Column(nullable = false)
    private String domicilioComercial;






}
