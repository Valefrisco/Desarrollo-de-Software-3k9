package com.tup.programacion3.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacturaRequestDTO {

    // ----- Relaciones (solo IDs) -----
    private Long puntoVentaId;
    private Long clienteId;        // opcional: null = Consumidor Final
    private Long condicionIvaId;
    private Long tipoMonedaId;
    private Long listaPrecioId;    // lista de la que se toman los precios de los articulos

    // ----- Datos de cabecera -----
    private String estado;         // Solo PUT (ej. "ANULADA"). En POST se ignora: toda factura nace "EMITIDA"
    private String observaciones;

    // ----- Items -----
    private List<FacturaDetalleRequestDTO> detalles;
}
