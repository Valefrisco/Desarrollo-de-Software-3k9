// =====================================================================
// Interfaces que espejan los DTOs de Spring Boot (paquete DTOs).
// Los nombres de los campos tienen que ser EXACTAMENTE los mismos que en
// Java, porque son las claves del JSON que viaja entre front y back.
// =====================================================================

/** Estados posibles de una factura (en Java es un String con estos valores). */
// FACTURADA = ARCA le dio CAE: queda de solo lectura (no se edita, anula ni borra).
export type EstadoFactura = 'BORRADOR' | 'EMITIDA' | 'FACTURADA' | 'ANULADA';

// ---------------------------------------------------------------------
// Salida: lo que DEVUELVE la API
// ---------------------------------------------------------------------

/** Espejo de FacturaReporteDTO.java (respuesta de GET, POST y PUT). */
export interface FacturaReporteDTO {
    id: number;
    numeroFactura: number | null;  // null mientras es BORRADOR (se numera al emitir)
    fechaEmision: string;          // LocalDateTime -> texto ISO, ej. "2026-10-20T16:53:14"
    clienteDenominacion: string;
    condicionIva: string;
    puntoVentaDescripcion: string;
    importeTotal: number;
    estado: EstadoFactura;
    cantidadItems: number;
    cae: string | null;            // solo FACTURADA
    numeroArca: number | null;     // solo FACTURADA: numero del comprobante en ARCA
    numeroNotaCredito: number | null;  // solo ANULADA en ARCA: numero de la Nota de Credito C
    caeNotaCredito: string | null;     // solo ANULADA en ARCA: CAE de la Nota de Credito C
    clienteCuit: string | null;        // CUIT/CUIL del cliente (null = Consumidor Final)
}

/** Espejo de FacturaCompletaDTO.java (respuesta de GET /api/facturas/{id}, para editar). */
export interface FacturaCompletaDTO {
    id: number;
    numeroFactura: number | null;
    estado: EstadoFactura;
    puntoVentaId: number;
    clienteId: number | null;      // null = Consumidor Final
    condicionIvaId: number;
    tipoMonedaId: number;
    listaPrecioId: number | null;
    observaciones: string | null;
    importeTotal: number;
    detalles: FacturaDetalleRequestDTO[];
    // Datos de ARCA (null mientras no se facturo)
    cae: string | null;
    caeVencimiento: string | null;     // LocalDate -> "AAAA-MM-DD"
    numeroArca: number | null;
    fechaFacturacion: string | null;
    motivoRechazo: string | null;      // ultimo rechazo de ARCA, si lo hubo
    // Nota de Credito C con la que se anulo en ARCA (null si no se anulo en ARCA)
    numeroNotaCredito: number | null;
    caeNotaCredito: string | null;
}

/** Espejo de AnulacionArcaDTO.java (respuesta de POST /api/facturas/{id}/anular-en-arca). */
export interface AnulacionArcaDTO {
    id: number;
    numeroFactura: number;
    numeroNotaCredito: number;
    caeNotaCredito: string;
    caeVencimiento: string | null;     // "AAAA-MM-DD"
}

/** Espejo de PaginaDTO.java (respuesta de GET /api/facturas/pagina). */
export interface PaginaDTO<T> {
    contenido: T[];          // los elementos de esta pagina
    pagina: number;          // empieza en 0
    tamano: number;
    totalElementos: number;  // de todas las paginas
    totalPaginas: number;    // 0 si no hay resultados
    importeTotal: number;    // suma de TODOS los resultados filtrados (sin anuladas, salvo que se filtre por ANULADA)
}

/** Espejo de ComprobanteImpresionDTO.java (GET /api/facturas/{id}/impresion). */
export interface ComprobanteImpresionDTO {
    emisorRazonSocial: string;
    emisorCuit: number;
    emisorDomicilio: string | null;
    emisorCondicionIva: string;
    tipoComprobante: string;           // "FACTURA"
    letra: string;                     // "C"
    codigoComprobante: string;         // "011"
    estado: 'EMITIDA' | 'FACTURADA';   // EMITIDA = todavia sin CAE de ARCA
    puntoVentaArca: number | null;     // null en una EMITIDA
    numeroArca: number | null;         // null en una EMITIDA
    fechaFacturacion: string | null;   // "AAAA-MM-DDTHH:mm:ss"; null en una EMITIDA
    numeroInterno: number | null;
    puntoVentaNumero: number;          // numero del punto de venta del sistema
    puntoVentaDescripcion: string;
    fechaEmision: string;              // "AAAA-MM-DDTHH:mm:ss" (fecha de emision en el sistema)
    clienteDenominacion: string;
    clienteCuit: string | null;
    clienteDomicilio: string | null;
    condicionIva: string;
    moneda: string;
    renglones: {
        codigo: string;
        descripcion: string;
        cantidad: number;
        precioUnitario: number;
        porcentajeBonificacion: number;
        subtotal: number;
    }[];
    importeTotal: number;
    observaciones: string | null;
    cae: string | null;                // null en una EMITIDA
    caeVencimiento: string | null;     // "AAAA-MM-DD"
}

/** Espejo de ResultadoFacturacionDTO.java (respuesta de POST /api/facturas/facturar-emitidas). */
export interface ResultadoFacturacionDTO {
    facturadas: number;
    rechazadas: number;
    detalle: {
        id: number;
        numeroFactura: number | null;
        resultado: 'A' | 'R';          // A = aprobada (FACTURADA), R = rechazada (sigue EMITIDA)
        numeroArca: number | null;
        cae: string | null;
        mensaje: string | null;        // motivo del rechazo
    }[];
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
    listaPrecioId?: number;        // lista de precios de este renglon (si falta, la de la factura)
}

/** Espejo de FacturaRequestDTO.java (cabecera + renglones). */
export interface FacturaRequestDTO {
    puntoVentaId?: number;
    clienteId?: number | null;     // null = Consumidor Final (en PUT, sin la clave = no cambia)
    condicionIvaId?: number;
    tipoMonedaId?: number;
    listaPrecioId?: number;
    estado?: EstadoFactura;        // POST: BORRADOR o EMITIDA. PUT: EMITIDA (emitir) o ANULADA
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
    numero?: number;               // numero de factura exacto
    cliente?: string;              // parte de la razon social o del CUIT (sin distinguir mayusculas)
}

// ---------------------------------------------------------------------
// Catalogos para los desplegables: GET /api/catalogos
// ---------------------------------------------------------------------

/** Espejo de OpcionDTO.java: el ID que viaja en el JSON y el nombre que se muestra. */
export interface OpcionDTO {
    id: number;
    nombre: string;
}

/** Espejo de ArticuloPrecioDTO.java: un articulo con su precio en una lista. */
export interface ArticuloPrecioDTO {
    listaPrecioId: number;
    articuloId: number;
    nombre: string;
    precio: number;
}

/** Espejo de CatalogosDTO.java. */
export interface CatalogosDTO {
    puntosVenta: OpcionDTO[];
    clientes: OpcionDTO[];
    condicionesIva: OpcionDTO[];
    monedas: OpcionDTO[];
    listasPrecio: OpcionDTO[];
    articulos: ArticuloPrecioDTO[];
    rubros: OpcionDTO[];           // filtros del explorador de articulos
    marcas: OpcionDTO[];
}

/** Espejo de ClienteExplorerDTO.java (GET /api/catalogos/clientes). */
export interface ClienteExplorerDTO {
    id: number;
    denominacion: string;
    cuitCuil: string;
    email: string | null;
    telefono: string | null;
    domicilio: string | null;
}

/** Espejo de ArticuloExplorerDTO.java (GET /api/catalogos/articulos). */
export interface ArticuloExplorerDTO {
    articuloId: number;
    codigo: string;
    denominacion: string;
    rubro: string | null;
    marca: string | null;
    precio: number;                // en la lista de precios pedida
}

/** Filtros del explorador de articulos. */
export interface FiltrosArticuloExplorer {
    listaPrecioId: number;
    texto?: string;
    rubroId?: number;
    marcaId?: number;
}
