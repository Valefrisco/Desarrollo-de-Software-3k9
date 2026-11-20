package com.tup.programacion3.DTOs;

import java.util.List;

/**
 * Respuesta de GET /api/catalogos: todo lo que el formulario de factura necesita
 * para armar sus desplegables, en un solo viaje al servidor.
 */
public record CatalogosDTO(
        List<OpcionDTO> puntosVenta,
        List<OpcionDTO> clientes,
        List<OpcionDTO> condicionesIva,
        List<OpcionDTO> monedas,
        List<OpcionDTO> listasPrecio,
        List<ArticuloPrecioDTO> articulos,
        List<OpcionDTO> rubros,      // para los filtros del explorador de articulos
        List<OpcionDTO> marcas) {
}
