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
}
