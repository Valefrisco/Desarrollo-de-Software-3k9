package com.tup.programacion3.DTOs;

/** Una fila del explorador de articulos (GET /api/catalogos/articulos): articulo con su precio en una lista. */
public record ArticuloExplorerDTO(
        Long articuloId,
        String codigo,
        String denominacion,
        String rubro,          // null si no tiene
        String marca,          // null si no tiene
        double precio) {       // precio en la lista de precios pedida
}
