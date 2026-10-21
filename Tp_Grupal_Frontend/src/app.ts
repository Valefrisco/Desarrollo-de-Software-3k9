import {
    EstadoFactura,
    FacturaDetalleRequestDTO,
    FacturaFiltrosDTO,
    FacturaReporteDTO,
    FacturaRequestDTO,
} from './models/factura.dto.js';
import { FacturaService } from './services/FacturaService.js';

// =====================================================================
// PARTE 1: INICIO Y UTILIDADES
// =====================================================================

const service = new FacturaService();

/** Renglones de la factura que se esta cargando. Viven en memoria hasta "Emitir". */
let items: FacturaDetalleRequestDTO[] = [];

/** Busca un elemento del HTML por id. Si no existe, avisa con un error claro. */
function elemento<T extends HTMLElement>(id: string): T {
    const el = document.getElementById(id);
    if (!el) {
        throw new Error(`No existe el elemento #${id} en index.html`);
    }
    return el as T;
}

// --- Referencias a los elementos del HTML (una sola vez, al cargar) ---
const mensaje = elemento<HTMLDivElement>('mensaje');

// Seccion 1: filtros
const formFiltros = elemento<HTMLFormElement>('form-filtros');
const filtroFechaDesde = elemento<HTMLInputElement>('filtro-fecha-desde');
const filtroFechaHasta = elemento<HTMLInputElement>('filtro-fecha-hasta');
const filtroEstado = elemento<HTMLSelectElement>('filtro-estado');
const filtroMontoMinimo = elemento<HTMLInputElement>('filtro-monto-minimo');
const btnLimpiarFiltros = elemento<HTMLButtonElement>('btn-limpiar-filtros');

// Seccion 2: alta de factura
const formFactura = elemento<HTMLFormElement>('form-factura');
const inputPuntoVenta = elemento<HTMLInputElement>('punto-venta-id');
const inputCliente = elemento<HTMLInputElement>('cliente-id');
const inputCondicionIva = elemento<HTMLInputElement>('condicion-iva-id');
const inputTipoMoneda = elemento<HTMLInputElement>('tipo-moneda-id');
const inputListaPrecio = elemento<HTMLInputElement>('lista-precio-id');
const inputObservaciones = elemento<HTMLInputElement>('observaciones');
const inputItemArticulo = elemento<HTMLInputElement>('item-articulo-id');
const inputItemCantidad = elemento<HTMLInputElement>('item-cantidad');
const inputItemBonificacion = elemento<HTMLInputElement>('item-bonificacion');
const btnAgregarItem = elemento<HTMLButtonElement>('btn-agregar-item');
const itemsBody = elemento<HTMLTableSectionElement>('items-body');
const btnEmitir = elemento<HTMLButtonElement>('btn-emitir');

// Seccion 3: listado
const facturasBody = elemento<HTMLTableSectionElement>('facturas-body');

// --- Mensajes al usuario ---
let temporizadorMensaje: number | undefined;

function mostrarMensaje(texto: string, tipo: 'exito' | 'error'): void {
    mensaje.textContent = texto;
    mensaje.className = tipo;              // la clase CSS lo pinta verde o rojo
    window.clearTimeout(temporizadorMensaje);
    // Los exitos se ocultan solos; los errores quedan visibles hasta la proxima accion
    if (tipo === 'exito') {
        temporizadorMensaje = window.setTimeout(() => (mensaje.className = ''), 6000);
    }
}

/** Convierte cualquier error en un texto para mostrar. */
function textoDeError(error: unknown): string {
    // fetch lanza TypeError cuando ni siquiera pudo conectarse (backend apagado)
    if (error instanceof TypeError && error.message.toLowerCase().includes('fetch')) {
        return 'No se pudo conectar con el servidor. Revisá que el backend esté levantado en localhost:8080 '
            + 'y que permita CORS (si el problema es CORS, la consola del navegador -F12- lo dice).';
    }
    return error instanceof Error ? error.message : String(error);
}

// --- Formatos ---
const formatoMoneda = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' });

/** "2026-10-20T16:53:14.0147" -> "20/10/2026 16:53" */
function formatearFecha(iso: string): string {
    const [fecha, hora = ''] = iso.split('T');
    const [anio, mes, dia] = fecha.split('-');
    return `${dia}/${mes}/${anio} ${hora.slice(0, 5)}`;
}

/** Lee un <input type="number">: si esta vacio devuelve undefined (el campo no viaja en el JSON). */
function numeroOpcional(input: HTMLInputElement): number | undefined {
    return input.value.trim() === '' ? undefined : Number(input.value);
}

// --- Construccion de la tabla con la API del DOM ---
// Se usa textContent / append (y no innerHTML) para que ningun texto que venga
// de la base se interprete como HTML (evita inyeccion de codigo, XSS).

function celda(contenido: string | HTMLElement, clase?: string): HTMLTableCellElement {
    const td = document.createElement('td');
    if (clase) td.className = clase;
    td.append(contenido);
    return td;
}

function filaVacia(columnas: number, texto: string): HTMLTableRowElement {
    const tr = document.createElement('tr');
    const td = celda(texto, 'vacio');
    td.colSpan = columnas;
    tr.append(td);
    return tr;
}

function boton(texto: string, clase: string, alHacerClic: () => void): HTMLButtonElement {
    const b = document.createElement('button');
    b.type = 'button';
    b.textContent = texto;
    b.className = clase;
    b.addEventListener('click', alHacerClic);
    return b;
}

// =====================================================================
// PARTE 2: LISTADO, FILTROS Y ACCIONES POR FILA (GET, PUT, DELETE)
// =====================================================================

/** Arma el FacturaFiltrosDTO con lo cargado en el formulario de busqueda. */
function leerFiltros(): FacturaFiltrosDTO {
    return {
        fechaDesde: filtroFechaDesde.value || undefined,
        fechaHasta: filtroFechaHasta.value || undefined,
        estado: (filtroEstado.value || undefined) as EstadoFactura | undefined,
        montoMinimo: numeroOpcional(filtroMontoMinimo),
    };
}

/** Pide las facturas al backend (GET) y redibuja la tabla. */
async function cargarFacturas(): Promise<void> {
    try {
        const facturas = await service.obtenerFacturas(leerFiltros());
        dibujarFacturas(facturas);
    } catch (error) {
        dibujarFacturas([]);
        mostrarMensaje(textoDeError(error), 'error');
    }
}

function dibujarFacturas(facturas: FacturaReporteDTO[]): void {
    facturasBody.replaceChildren();   // vacia la tabla

    if (facturas.length === 0) {
        facturasBody.append(filaVacia(9, 'No hay comprobantes para mostrar.'));
        return;
    }

    for (const factura of facturas) {
        const estado = document.createElement('span');
        estado.className = `estado ${factura.estado}`;
        estado.textContent = factura.estado;

        const btnAnular = boton('Anular', 'aviso', () => void anular(factura));
        btnAnular.disabled = factura.estado === 'ANULADA';   // una anulada no se vuelve a anular
        const btnEliminar = boton('Eliminar', 'peligro', () => void eliminar(factura));

        const acciones = document.createElement('div');
        acciones.className = 'acciones';
        acciones.append(btnAnular, btnEliminar);

        const fila = document.createElement('tr');
        fila.append(
            celda(String(factura.numeroFactura)),
            celda(formatearFecha(factura.fechaEmision)),
            celda(factura.clienteDenominacion),
            celda(factura.condicionIva),
            celda(factura.puntoVentaDescripcion),
            celda(String(factura.cantidadItems), 'numero'),
            celda(formatoMoneda.format(factura.importeTotal), 'numero'),
            celda(estado),
            celda(acciones),
        );
        facturasBody.append(fila);
    }
}

/** Anulacion logica: PUT con estado ANULADA y el motivo como observaciones. */
async function anular(factura: FacturaReporteDTO): Promise<void> {
    const motivo = window.prompt(`Motivo de anulación de la factura ${factura.numeroFactura}:`);
    if (motivo === null) return;   // el usuario apreto "Cancelar"

    try {
        await service.actualizarEstado(factura.id, 'ANULADA', motivo.trim() || undefined);
        mostrarMensaje(`Factura ${factura.numeroFactura} anulada.`, 'exito');
        await cargarFacturas();
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
    }
}

/** Borrado fisico: DELETE, previa confirmacion. */
async function eliminar(factura: FacturaReporteDTO): Promise<void> {
    const confirmado = window.confirm(
        `¿Eliminar definitivamente la factura ${factura.numeroFactura}? Esta acción no se puede deshacer.`);
    if (!confirmado) return;

    try {
        await service.eliminarFactura(factura.id);
        mostrarMensaje(`Factura ${factura.numeroFactura} eliminada.`, 'exito');
        await cargarFacturas();
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
    }
}

// Eventos de la seccion de filtros
formFiltros.addEventListener('submit', (evento) => {
    evento.preventDefault();   // sin esto el navegador recargaria la pagina al enviar el form
    void cargarFacturas();
});

btnLimpiarFiltros.addEventListener('click', () => {
    formFiltros.reset();
    void cargarFacturas();
});

// =====================================================================
// PARTE 3: ALTA DE FACTURA (renglones en memoria + POST)
// =====================================================================

/** Valida los inputs del renglon y lo agrega al array en memoria. */
function agregarItem(): void {
    const articuloId = numeroOpcional(inputItemArticulo);
    const cantidad = numeroOpcional(inputItemCantidad);
    const bonificacion = numeroOpcional(inputItemBonificacion);

    // Validaciones para ayudar al usuario. El backend vuelve a validar todo,
    // porque al servidor se le puede pegar sin pasar por esta pantalla (ej. Postman).
    if (articuloId === undefined || articuloId <= 0) {
        mostrarMensaje('Indicá el ID del artículo.', 'error');
        return;
    }
    if (cantidad === undefined || cantidad <= 0) {
        mostrarMensaje('La cantidad debe ser mayor a 0.', 'error');
        return;
    }
    if (bonificacion !== undefined && (bonificacion < 0 || bonificacion > 100)) {
        mostrarMensaje('La bonificación debe estar entre 0 y 100.', 'error');
        return;
    }

    items.push({ articuloId, cantidad, porcentajeBonificacion: bonificacion });

    // Limpia los inputs para cargar el siguiente renglon
    inputItemArticulo.value = '';
    inputItemCantidad.value = '';
    inputItemBonificacion.value = '';
    inputItemArticulo.focus();

    dibujarItems();
}

function quitarItem(indice: number): void {
    items.splice(indice, 1);
    dibujarItems();
}

/** Redibuja la tabla de renglones a partir del array en memoria. */
function dibujarItems(): void {
    itemsBody.replaceChildren();

    if (items.length === 0) {
        itemsBody.append(filaVacia(5, 'Todavía no hay renglones. Agregá al menos uno para emitir.'));
    }

    items.forEach((item, indice) => {
        const fila = document.createElement('tr');
        fila.append(
            celda(String(indice + 1)),
            celda(String(item.articuloId)),
            celda(String(item.cantidad), 'numero'),
            celda(String(item.porcentajeBonificacion ?? 0), 'numero'),
            celda(boton('Quitar', 'peligro', () => quitarItem(indice))),
        );
        itemsBody.append(fila);
    });

    btnEmitir.disabled = items.length === 0;   // sin renglones no se puede emitir
}

/** Junta cabecera + renglones en un FacturaRequestDTO y lo manda por POST. */
async function emitirFactura(): Promise<void> {
    if (items.length === 0) {
        mostrarMensaje('Agregá al menos un renglón antes de emitir.', 'error');
        return;
    }

    const factura: FacturaRequestDTO = {
        puntoVentaId: numeroOpcional(inputPuntoVenta),
        clienteId: numeroOpcional(inputCliente),        // vacio = Consumidor Final
        condicionIvaId: numeroOpcional(inputCondicionIva),
        tipoMonedaId: numeroOpcional(inputTipoMoneda),
        listaPrecioId: numeroOpcional(inputListaPrecio),
        observaciones: inputObservaciones.value.trim() || undefined,
        detalles: items,
    };

    btnEmitir.disabled = true;   // evita emitir dos veces con doble clic
    try {
        const creada = await service.crearFactura(factura);
        mostrarMensaje(
            `Factura ${creada.numeroFactura} emitida por ${formatoMoneda.format(creada.importeTotal)}.`, 'exito');
        items = [];
        inputObservaciones.value = '';
        dibujarItems();
        await cargarFacturas();
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
    } finally {
        btnEmitir.disabled = items.length === 0;
    }
}

// Eventos de la seccion de alta
btnAgregarItem.addEventListener('click', agregarItem);

// Enter en los inputs del renglon agrega el renglon (en vez de enviar el formulario)
for (const input of [inputItemArticulo, inputItemCantidad, inputItemBonificacion]) {
    input.addEventListener('keydown', (evento) => {
        if (evento.key === 'Enter') {
            evento.preventDefault();
            agregarItem();
        }
    });
}

formFactura.addEventListener('submit', (evento) => {
    evento.preventDefault();
    void emitirFactura();
});

// =====================================================================
// ARRANQUE: al abrir la pagina
// =====================================================================
dibujarItems();
void cargarFacturas();
