package com.tup.programacion3.DTOs;

import java.util.List;

/**
 * Respuesta de GET /api/facturas/{id}: la factura con sus IDs y renglones, tal como
 * los necesita el formulario para editarla. Los renglones usan los mismos
 * campos que el request (articuloId, cantidad, porcentajeBonificacion), asi el
 * frontend puede devolverlos sin transformarlos.
 */
public record FacturaCompletaDTO(
        Long id,
        Long numeroFactura,          // null mientras es BORRADOR
        String estado,
        Long puntoVentaId,
        Long clienteId,              // null = Consumidor Final
        Long condicionIvaId,
        Long tipoMonedaId,
        Long listaPrecioId,
        String observaciones,
        double importeTotal,
        List<FacturaDetalleRequestDTO> detalles) {
}
