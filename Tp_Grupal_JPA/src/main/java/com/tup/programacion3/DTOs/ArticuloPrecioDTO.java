package com.tup.programacion3.DTOs;

/** Un articulo con su precio dentro de una lista (sale de ListaPrecioArticulo). */
public record ArticuloPrecioDTO(Long listaPrecioId, Long articuloId, String nombre, double precio) {
}
