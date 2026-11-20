package com.tup.programacion3.DTOs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta de GET /api/facturas/{id}/impresion: todo lo que lleva el comprobante impreso
 * de una factura autorizada por ARCA (estado FACTURADA). Los importes son los guardados en
 * la factura (no los precios actuales de las listas).
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
        int puntoVentaArca,
        long numeroArca,
        LocalDateTime fechaFacturacion,
        Long numeroInterno,            // numero de la factura en el sistema
        String puntoVentaDescripcion,
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
        // Autorizacion de ARCA
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
