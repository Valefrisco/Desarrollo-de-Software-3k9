package com.tup.programacion3.DTOs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta de GET /api/facturas/{id}/impresion: todo lo que lleva el comprobante impreso
 * de una factura EMITIDA (todavia sin CAE) o FACTURADA (autorizada por ARCA). Los importes
 * son los guardados en la factura (no los precios actuales de las listas).
 * En una EMITIDA los datos de ARCA (puntoVentaArca, numeroArca, fechaFacturacion, cae,
 * caeVencimiento) vienen en null y el impreso se numera con el punto de venta del sistema.
 */
public record ComprobanteImpresionDTO(
        // Emisor
        String emisorRazonSocial,
        long emisorCuit,
        String emisorDomicilio,
        String emisorCondicionIva,
        // Comprobante (como lo registro ARCA)
        String tipoComprobante,        // "FACTURA"
        String letra,                  // "C"
        String codigoComprobante,      // "011"
        String estado,                 // EMITIDA (sin CAE) o FACTURADA (con CAE)
        Integer puntoVentaArca,        // null en una EMITIDA
        Long numeroArca,               // null en una EMITIDA
        LocalDateTime fechaFacturacion,  // null en una EMITIDA
        Long numeroInterno,            // numero de la factura en el sistema
        int puntoVentaNumero,          // numero del punto de venta del sistema
        String puntoVentaDescripcion,
        LocalDateTime fechaEmision,    // fecha de emision en el sistema
        // Receptor
        String clienteDenominacion,
        String clienteCuit,            // null = Consumidor Final
        String clienteDomicilio,
        String condicionIva,
        // Detalle y totales
        String moneda,
        List<Renglon> renglones,
        double importeTotal,
        String observaciones,
        // Autorizacion de ARCA (null en una EMITIDA)
        String cae,
        LocalDate caeVencimiento) {

    public record Renglon(
            String codigo,
            String descripcion,
            double cantidad,
            double precioUnitario,
            double porcentajeBonificacion,
            double subtotal) {
    }
}
