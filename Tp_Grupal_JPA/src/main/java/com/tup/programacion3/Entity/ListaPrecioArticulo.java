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






}
