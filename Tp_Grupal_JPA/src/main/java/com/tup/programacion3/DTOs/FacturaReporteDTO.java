package com.tup.programacion3.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacturaReporteDTO {
    // OJO: el orden de los campos define el orden de los parametros del
    // constructor (@AllArgsConstructor) que usa el SELECT new del repositorio.
    private Long id;               // necesario para PUT/DELETE desde el frontend
    private Long numeroFactura;    // null mientras la factura es BORRADOR
    private LocalDateTime fechaEmision;
    private String clienteDenominacion;
    private String condicionIva;
    private String puntoVentaDescripcion;
    private double importeTotal;
    private String estado;         // BORRADOR / EMITIDA / FACTURADA / ANULADA
    private long cantidadItems;
    private String cae;            // solo FACTURADA: codigo de autorizacion de ARCA
    private Long numeroArca;       // solo FACTURADA: numero del comprobante en ARCA
    private Long numeroNotaCredito;  // solo ANULADA en ARCA: numero de la Nota de Credito C
    private String caeNotaCredito;   // solo ANULADA en ARCA: CAE de la Nota de Credito C
    private String clienteCuit;      // CUIT/CUIL del cliente (null = Consumidor Final)
}
