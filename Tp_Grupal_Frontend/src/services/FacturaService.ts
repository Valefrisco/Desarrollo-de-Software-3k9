import {
    CatalogosDTO,
    ErrorDTO,
    EstadoFactura,
    FacturaCompletaDTO,
    FacturaFiltrosDTO,
    FacturaReporteDTO,
    AnulacionArcaDTO,
    FacturaRequestDTO,
    PaginaDTO,
    ResultadoFacturacionDTO,
} from '../models/factura.dto.js';

/** URL base de la API REST de Spring Boot (FacturaRestController). */
const API_URL = 'http://localhost:8080/api/facturas';

/** Opciones de los desplegables del formulario (CatalogoRestController). */
const CATALOGOS_URL = 'http://localhost:8080/api/catalogos';

/**
 * Centraliza todas las llamadas HTTP al backend.
 * El resto del frontend nunca usa fetch directamente: siempre pasa por aca.
 */
export class FacturaService {

    // GET /api/facturas?fechaDesde=...&fechaHasta=...&estado=...&montoMinimo=...
    // Devuelve TODAS las que cumplen los filtros (se usa para los contadores del menu)
    async obtenerFacturas(filtros: FacturaFiltrosDTO): Promise<FacturaReporteDTO[]> {
        const query = this.parametrosDeFiltro(filtros).toString();
        const url = query ? `${API_URL}?${query}` : API_URL;

        const respuesta = await fetch(url);
        return this.procesarRespuesta<FacturaReporteDTO[]>(respuesta);
    }

    // GET /api/facturas/pagina?pagina=0&tamano=10 (+ filtros)
    // Devuelve solo una pagina, ya ordenada (mas nuevas primero) por el backend.
    async obtenerPagina(filtros: FacturaFiltrosDTO, pagina: number, tamano: number): Promise<PaginaDTO<FacturaReporteDTO>> {
        const params = this.parametrosDeFiltro(filtros);
        params.append('pagina', String(pagina));
        params.append('tamano', String(tamano));

        const respuesta = await fetch(`${API_URL}/pagina?${params.toString()}`);
        return this.procesarRespuesta<PaginaDTO<FacturaReporteDTO>>(respuesta);
    }

    // Solo se agregan a la URL los filtros que tienen valor
    private parametrosDeFiltro(filtros: FacturaFiltrosDTO): URLSearchParams {
        const params = new URLSearchParams();
        if (filtros.fechaDesde) params.append('fechaDesde', filtros.fechaDesde);
        if (filtros.fechaHasta) params.append('fechaHasta', filtros.fechaHasta);
        if (filtros.estado) params.append('estado', filtros.estado);
        if (filtros.montoMinimo !== undefined) params.append('montoMinimo', String(filtros.montoMinimo));
        return params;
    }

    // GET /api/catalogos  (puntos de venta, clientes, condiciones IVA, monedas, listas y articulos)
    async obtenerCatalogos(): Promise<CatalogosDTO> {
        const respuesta = await fetch(CATALOGOS_URL);
        return this.procesarRespuesta<CatalogosDTO>(respuesta);
    }

    // GET /api/facturas/{id}  (factura con IDs y renglones, para cargarla en el formulario)
    async obtenerFactura(id: number): Promise<FacturaCompletaDTO> {
        const respuesta = await fetch(`${API_URL}/${id}`);
        return this.procesarRespuesta<FacturaCompletaDTO>(respuesta);
    }

    // POST /api/facturas  (responde 201 con la factura creada)
    // Con estado 'BORRADOR' queda guardada sin emitir; sin estado o con 'EMITIDA' se emite.
    async crearFactura(factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {
        const respuesta = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(factura),
        });
        return this.procesarRespuesta<FacturaReporteDTO>(respuesta);
    }

    // PUT /api/facturas/{id}  (responde 200). Edita la factura; si es un borrador y va con estado 'EMITIDA', ademas lo emite.
    async actualizarFactura(id: number, factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {
        const respuesta = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(factura),
        });
        return this.procesarRespuesta<FacturaReporteDTO>(respuesta);
    }

    // PUT /api/facturas/{id}  (responde 200). Solo cambia el estado: anular o emitir un borrador.
    async actualizarEstado(id: number, estado: string, observaciones?: string): Promise<FacturaReporteDTO> {
        // Actualizacion parcial: solo viajan estado y observaciones.
        // El backend valida que el estado sea correcto (400 si no lo es).
        const cuerpo: FacturaRequestDTO = { estado: estado as EstadoFactura, observaciones };

        const respuesta = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cuerpo),
        });
        return this.procesarRespuesta<FacturaReporteDTO>(respuesta);
    }

    // POST /api/facturas/facturar-emitidas  (responde 200 con el resumen)
    // Pide el CAE a ARCA para todas las EMITIDA; las aprobadas pasan a FACTURADA.
    async facturarEmitidas(): Promise<ResultadoFacturacionDTO> {
        const respuesta = await fetch(`${API_URL}/facturar-emitidas`, { method: 'POST' });
        return this.procesarRespuesta<ResultadoFacturacionDTO>(respuesta);
    }

    // POST /api/facturas/{id}/anular-en-arca  body: { motivo }
    // Anula una FACTURADA emitiendo en ARCA una Nota de Credito C asociada.
    async anularEnArca(id: number, motivo: string): Promise<AnulacionArcaDTO> {
        const respuesta = await fetch(`${API_URL}/${id}/anular-en-arca`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ motivo }),
        });
        return this.procesarRespuesta<AnulacionArcaDTO>(respuesta);
    }

    // DELETE /api/facturas/{id}  (responde 204, sin cuerpo)
    async eliminarFactura(id: number): Promise<void> {
        const respuesta = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        await this.procesarRespuesta<void>(respuesta);
    }

    /**
     * Si la respuesta es un error (4xx/5xx), lanza un Error con el mensaje del
     * ErrorDTO del backend. Si salio bien, devuelve el cuerpo ya convertido.
     */
    private async procesarRespuesta<T>(respuesta: Response): Promise<T> {
        if (!respuesta.ok) {
            let mensaje = `Error ${respuesta.status}`;
            try {
                const error: ErrorDTO = await respuesta.json();
                if (error.mensaje) mensaje = error.mensaje;
            } catch {
                // La respuesta de error no traia JSON: queda el mensaje generico
            }
            throw new Error(mensaje);
        }
        if (respuesta.status === 204) {
            return undefined as T;  // 204 No Content: no hay cuerpo para leer
        }
        return (await respuesta.json()) as T;
    }
}
