// =====================================================================
// Interfaces que espejan los DTOs de Spring Boot (paquete DTOs).
// Los nombres de los campos tienen que ser EXACTAMENTE los mismos que en
// Java, porque son las claves del JSON que viaja entre front y back.
// =====================================================================

/** Estados posibles de una factura (en Java es un String con estos valores). */
export type EstadoFactura = 'EMITIDA' | 'ANULADA';

// ---------------------------------------------------------------------
// Salida: lo que DEVUELVE la API
// ---------------------------------------------------------------------

/** Espejo de FacturaReporteDTO.java (respuesta de GET, POST y PUT). */
export interface FacturaReporteDTO {
    id: number;
    numeroFactura: number;
    fechaEmision: string;          // LocalDateTime -> texto ISO, ej. "2026-10-20T16:53:14"
    clienteDenominacion: string;
    condicionIva: string;
    puntoVentaDescripcion: string;
    importeTotal: number;
    estado: EstadoFactura;
    cantidadItems: number;
}

/** Espejo de ErrorDTO.java (cuerpo de las respuestas 400, 404 y 409). */
export interface ErrorDTO {
    status: number;
    mensaje: string;
    timestamp: string;
}

// ---------------------------------------------------------------------
// Entrada: lo que se ENVIA a la API (POST y PUT)
// En Java todos los campos son objetos (Long, Double, String) y pueden
// venir en null, por eso aca son opcionales (?).
// ---------------------------------------------------------------------

/** Espejo de FacturaDetalleRequestDTO.java (un renglon de la factura). */
export interface FacturaDetalleRequestDTO {
    articuloId?: number;
    cantidad?: number;
    porcentajeBonificacion?: number;
}

/** Espejo de FacturaRequestDTO.java (cabecera + renglones). */
export interface FacturaRequestDTO {
    puntoVentaId?: number;
    clienteId?: number;            // sin cliente = Consumidor Final
    condicionIvaId?: number;
    tipoMonedaId?: number;
    listaPrecioId?: number;
    estado?: EstadoFactura;        // solo en PUT (ej. 'ANULADA')
    observaciones?: string;
    detalles?: FacturaDetalleRequestDTO[];
}

// ---------------------------------------------------------------------
// Filtros del GET (los @RequestParam opcionales de FacturaRestController)
// ---------------------------------------------------------------------

/** Filtros de busqueda: GET /api/facturas?fechaDesde=...&estado=... */
export interface FacturaFiltrosDTO {
    fechaDesde?: string;           // formato "AAAA-MM-DD"
    fechaHasta?: string;           // formato "AAAA-MM-DD"
    estado?: EstadoFactura;
    montoMinimo?: number;
}
