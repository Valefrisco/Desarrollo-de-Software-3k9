package com.tup.programacion3.DTOs;

import java.time.LocalDate;

/**
 * Respuesta de POST /api/facturas/{id}/anular-en-arca: la Nota de Credito C que ARCA
 * autorizo para anular la factura.
 */
public record AnulacionArcaDTO(
        Long id,                     // id de la factura anulada
        Long numeroFactura,          // numero interno de la factura
        long numeroNotaCredito,      // numero de la nota de credito en ARCA
        String caeNotaCredito,
        LocalDate caeVencimiento) {
}
