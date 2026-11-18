import {
    CatalogosDTO,
    ErrorDTO,
    EstadoFactura,
    FacturaCompletaDTO,
    FacturaFiltrosDTO,
    FacturaReporteDTO,
    FacturaRequestDTO,
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
    async obtenerFacturas(filtros: FacturaFiltrosDTO): Promise<FacturaReporteDTO[]> {
        // Solo se agregan a la URL los filtros que tienen valor
        const params = new URLSearchParams();
        if (filtros.fechaDesde) params.append('fechaDesde', filtros.fechaDesde);
        if (filtros.fechaHasta) params.append('fechaHasta', filtros.fechaHasta);
        if (filtros.estado) params.append('estado', filtros.estado);
        if (filtros.montoMinimo !== undefined) params.append('montoMinimo', String(filtros.montoMinimo));

        const query = params.toString();
        const url = query ? `${API_URL}?${query}` : API_URL;

        const respuesta = await fetch(url);
        return this.procesarRespuesta<FacturaReporteDTO[]>(respuesta);
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
