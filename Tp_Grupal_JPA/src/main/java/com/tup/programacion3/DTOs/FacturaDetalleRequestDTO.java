package com.tup.programacion3.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacturaDetalleRequestDTO {

    private Long articuloId;
    private Double cantidad;                // obligatoria y mayor a 0
    private Double porcentajeBonificacion;  // opcional: null = 0 % . PELIGRO, el cliente pude poner cualquier ptje.
    // Lista de precios de ESTE renglon (opcional). Si viene null se usa la listaPrecioId de la
    // factura, que funciona como lista "por defecto". Asi una factura puede mezclar articulos
    // de distintas listas de precios.
    private Long listaPrecioId;

    /** Renglon con la lista de precios por defecto de la factura (la usa el DataLoader). */
    public FacturaDetalleRequestDTO(Long articuloId, Double cantidad, Double porcentajeBonificacion) {
        this(articuloId, cantidad, porcentajeBonificacion, null);
    }
}
