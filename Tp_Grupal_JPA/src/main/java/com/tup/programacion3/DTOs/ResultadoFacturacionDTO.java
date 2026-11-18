package com.tup.programacion3.DTOs;

import java.util.List;

/**
 * Respuesta de POST /api/facturas/facturar-emitidas: cuantas facturas autorizo ARCA,
 * cuantas rechazo y el detalle de cada una.
 */
public record ResultadoFacturacionDTO(
        int facturadas,
        int rechazadas,
        List<Detalle> detalle) {

    /**
     * Resultado de una factura.
     * resultado: "A" = aprobada (paso a FACTURADA), "R" = rechazada o error (sigue EMITIDA).
     */
    public record Detalle(
            Long id,
            Long numeroFactura,
            String resultado,
            Long numeroArca,         // null si fue rechazada
            String cae,              // null si fue rechazada
            String mensaje) {        // motivo del rechazo, o null si salio bien
    }
}
