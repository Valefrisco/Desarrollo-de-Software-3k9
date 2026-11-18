import {
    CatalogosDTO,
    EstadoFactura,
    FacturaDetalleRequestDTO,
    FacturaFiltrosDTO,
    FacturaReporteDTO,
    FacturaRequestDTO,
} from './models/factura.dto.js';
import { FacturaService } from './services/FacturaService.js';

// =====================================================================
// PARTE 1: ESTADO DE LA PANTALLA Y UTILIDADES
// =====================================================================

const service = new FacturaService();

/** Modulos del listado: uno por estado, mas el historico (todos). */
type Vista = EstadoFactura | 'HISTORICO';

/** Modulo de listado que se esta mostrando. Al abrir la pagina: el historico. */
let vistaActual: Vista = 'HISTORICO';

/** Renglones de la factura que se esta cargando. Viven en memoria hasta guardar o emitir. */
let items: FacturaDetalleRequestDTO[] = [];

/** Factura que se esta editando (borrador o emitida); null = el formulario es de alta. */
let facturaEditando: { id: number; estado: EstadoFactura; numeroFactura: number | null } | null = null;

/** Opciones de los desplegables (GET /api/catalogos). Se cargan una vez al abrir la pagina. */
let catalogos: CatalogosDTO | null = null;

/** Textos de cada modulo del listado. */
const VISTAS: Record<Vista, { titulo: string; descripcion: string; vacio: string }> = {
    BORRADOR: {
        titulo: 'Borradores',
        descripcion: 'Comprobantes guardados sin emitir. Se pueden editar o emitir.',
        vacio: 'No hay borradores. Desde «Nueva factura» podés guardar uno sin emitirlo.',
    },
    EMITIDA: {
        titulo: 'Emitidas',
        descripcion: 'Facturas emitidas. Se pueden editar o anular, o facturar en ARCA para obtener su CAE.',
        vacio: 'No hay facturas emitidas con estos filtros.',
    },
    FACTURADA: {
        titulo: 'Facturadas',
        descripcion: 'Facturas autorizadas por ARCA (homologación), con CAE. Son de solo lectura.',
        vacio: 'No hay facturas facturadas. Desde «Emitidas» usá «Facturar emitidas en ARCA».',
    },
    ANULADA: {
        titulo: 'Anuladas',
        descripcion: 'Facturas anuladas. Quedan como registro y no se pueden modificar.',
        vacio: 'No hay facturas anuladas.',
    },
    HISTORICO: {
        titulo: 'Histórico',
        descripcion: 'Todos los comprobantes, en cualquier estado.',
        vacio: 'No hay comprobantes con estos filtros.',
    },
};

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
const botonesMenu = Array.from(document.querySelectorAll<HTMLButtonElement>('.menu button[data-vista]'));
const btnNuevaFactura = elemento<HTMLButtonElement>('btn-nueva-factura');
const btnFacturarEmitidas = elemento<HTMLButtonElement>('btn-facturar-emitidas');

// Modulo de listado
const vistaListado = elemento<HTMLElement>('vista-listado');
const tituloListado = elemento<HTMLHeadingElement>('titulo-listado');
const descripcionListado = elemento<HTMLParagraphElement>('descripcion-listado');
const formFiltros = elemento<HTMLFormElement>('form-filtros');
const filtroFechaDesde = elemento<HTMLInputElement>('filtro-fecha-desde');
const filtroFechaHasta = elemento<HTMLInputElement>('filtro-fecha-hasta');
const campoFiltroEstado = elemento<HTMLLabelElement>('campo-filtro-estado');
const filtroEstado = elemento<HTMLSelectElement>('filtro-estado');
const filtroMontoMinimo = elemento<HTMLInputElement>('filtro-monto-minimo');
const btnLimpiarFiltros = elemento<HTMLButtonElement>('btn-limpiar-filtros');
const facturasBody = elemento<HTMLTableSectionElement>('facturas-body');
const facturasPie = elemento<HTMLTableSectionElement>('facturas-pie');

// Modulo nueva / editar factura (arranca oculto)
const seccionFactura = elemento<HTMLElement>('seccion-factura');
const tituloFormulario = elemento<HTMLHeadingElement>('titulo-formulario');
const descripcionFormulario = elemento<HTMLParagraphElement>('descripcion-formulario');
const formFactura = elemento<HTMLFormElement>('form-factura');
const letraComprobante = elemento<HTMLElement>('letra-comprobante');
const codigoComprobante = elemento<HTMLElement>('codigo-comprobante');
const numeroComprobante = elemento<HTMLSpanElement>('numero-comprobante');
const selectPuntoVenta = elemento<HTMLSelectElement>('punto-venta-id');
const selectCliente = elemento<HTMLSelectElement>('cliente-id');
const selectCondicionIva = elemento<HTMLSelectElement>('condicion-iva-id');
const selectTipoMoneda = elemento<HTMLSelectElement>('tipo-moneda-id');
const selectListaPrecio = elemento<HTMLSelectElement>('lista-precio-id');
const inputObservaciones = elemento<HTMLInputElement>('observaciones');
const selectItemArticulo = elemento<HTMLSelectElement>('item-articulo-id');
const inputItemCantidad = elemento<HTMLInputElement>('item-cantidad');
const celdaItemPrecio = elemento<HTMLTableCellElement>('item-precio');
const inputItemBonificacion = elemento<HTMLInputElement>('item-bonificacion');
const btnAgregarItem = elemento<HTMLButtonElement>('btn-agregar-item');
const itemsBody = elemento<HTMLTableSectionElement>('items-body');
const totalNeto = elemento<HTMLElement>('total-neto');
const totalIva = elemento<HTMLElement>('total-iva');
const totalComprobante = elemento<HTMLElement>('total-comprobante');
const btnCancelar = elemento<HTMLButtonElement>('btn-cancelar');
const btnGuardarBorrador = elemento<HTMLButtonElement>('btn-guardar-borrador');
const btnEmitir = elemento<HTMLButtonElement>('btn-emitir');
// Fila de carga de renglones (se oculta cuando la factura es de solo lectura)
const filaAgregarItem = btnAgregarItem.closest('tr') as HTMLTableRowElement;

/** true cuando se esta viendo una factura FACTURADA: nada se puede cambiar. */
let soloLectura = false;

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
const TASA_IVA = 0.21;   // la misma que FacturaService.java (precios con IVA incluido)

/** "2026-10-20T16:53:14.0147" -> "20/10/2026 16:53" */
function formatearFecha(iso: string): string {
    const [fecha, hora = ''] = iso.split('T');
    const [anio, mes, dia] = fecha.split('-');
    return `${dia}/${mes}/${anio} ${hora.slice(0, 5)}`;
}

/** Fecha arriba y hora abajo, en una celda del listado. */
function fechaConHora(iso: string): HTMLElement {
    const [fecha, hora] = formatearFecha(iso).split(' ');
    const contenedor = document.createElement('span');
    const chico = document.createElement('small');
    chico.textContent = hora;
    contenedor.append(fecha, chico);
    return contenedor;
}

/** Nombre del cliente y, debajo, su condicion IVA. */
function clienteConIva(factura: FacturaReporteDTO): HTMLElement {
    const contenedor = document.createElement('span');
    const nombre = document.createElement('strong');
    nombre.textContent = factura.clienteDenominacion;
    const iva = document.createElement('small');
    iva.textContent = factura.condicionIva;
    contenedor.append(nombre, iva);
    return contenedor;
}

/** Total arriba y cantidad de items abajo. */
function totalConItems(factura: FacturaReporteDTO): HTMLElement {
    const contenedor = document.createElement('div');
    const importe = document.createElement('span');
    importe.textContent = formatoMoneda.format(factura.importeTotal);
    const items = document.createElement('small');
    items.textContent = `${factura.cantidadItems} ${factura.cantidadItems === 1 ? 'ítem' : 'ítems'}`;
    contenedor.append(importe, items);
    return contenedor;
}

/** Lee un input numerico o un desplegable: si esta vacio devuelve undefined (el campo no viaja en el JSON). */
function numeroOpcional(input: HTMLInputElement | HTMLSelectElement): number | undefined {
    return input.value.trim() === '' ? undefined : Number(input.value);
}

function redondear(valor: number): number {
    return Math.round(valor * 100) / 100;
}

/** "factura 12" o "el borrador" (los borradores todavia no tienen numero). */
function nombreFactura(factura: { numeroFactura: number | null }): string {
    return factura.numeroFactura !== null ? `factura ${factura.numeroFactura}` : 'el borrador';
}

// --- Construccion de tablas con la API del DOM ---
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
    b.className = `boton chico ${clase}`.trim();
    b.addEventListener('click', alHacerClic);
    return b;
}

// =====================================================================
// PARTE 2: NAVEGACION ENTRE MODULOS
// =====================================================================

/** Muestra un modulo del listado (borradores, emitidas, anuladas o historico). */
function mostrarVista(vista: Vista): void {
    vistaActual = vista;
    seccionFactura.hidden = true;
    vistaListado.hidden = false;
    limpiarFormulario();

    tituloListado.textContent = VISTAS[vista].titulo;
    descripcionListado.textContent = VISTAS[vista].descripcion;
    // El filtro de estado solo tiene sentido en el historico
    campoFiltroEstado.hidden = vista !== 'HISTORICO';
    if (vista !== 'HISTORICO') filtroEstado.value = '';
    // Facturar en ARCA tiene sentido donde se ven las emitidas
    btnFacturarEmitidas.hidden = vista !== 'EMITIDA' && vista !== 'HISTORICO';

    marcarMenu(vista);
    void cargarFacturas();
}

/** Resalta en la barra lateral el modulo activo. */
function marcarMenu(activo: Vista | 'NUEVA'): void {
    for (const b of botonesMenu) {
        if (b.dataset.vista === activo) b.setAttribute('aria-current', 'page');
        else b.removeAttribute('aria-current');
    }
    if (activo === 'NUEVA') btnNuevaFactura.setAttribute('aria-current', 'page');
    else btnNuevaFactura.removeAttribute('aria-current');
}

// =====================================================================
// PARTE 3: LISTADO, FILTROS Y ACCIONES POR FILA (GET, PUT, DELETE)
// =====================================================================

/** Arma el FacturaFiltrosDTO con los filtros cargados y el estado del modulo. */
function leerFiltros(): FacturaFiltrosDTO {
    const estado = vistaActual === 'HISTORICO'
        ? (filtroEstado.value || undefined) as EstadoFactura | undefined
        : vistaActual;
    return {
        fechaDesde: filtroFechaDesde.value || undefined,
        fechaHasta: filtroFechaHasta.value || undefined,
        estado,
        montoMinimo: numeroOpcional(filtroMontoMinimo),
    };
}

/** Pide las facturas al backend (GET), actualiza los contadores y redibuja la tabla. */
async function cargarFacturas(): Promise<void> {
    try {
        // Sin filtros: para los contadores de la barra lateral
        const todas = await service.obtenerFacturas({});
        actualizarContadores(todas);

        // Con los filtros del modulo actual: para la tabla
        const facturas = await service.obtenerFacturas(leerFiltros());
        facturas.sort((a, b) => b.fechaEmision.localeCompare(a.fechaEmision));   // mas nuevas primero
        dibujarFacturas(facturas);
    } catch (error) {
        dibujarFacturas([]);
        mostrarMensaje(textoDeError(error), 'error');
    }
}

function actualizarContadores(facturas: FacturaReporteDTO[]): void {
    const estados: EstadoFactura[] = ['BORRADOR', 'EMITIDA', 'FACTURADA', 'ANULADA'];
    for (const estado of estados) {
        elemento(`contador-${estado}`).textContent = String(facturas.filter((f) => f.estado === estado).length);
    }
    // Sin emitidas no hay nada para mandar a ARCA
    btnFacturarEmitidas.disabled = !facturas.some((f) => f.estado === 'EMITIDA');
    elemento('contador-HISTORICO').textContent = String(facturas.length);
}

function dibujarFacturas(facturas: FacturaReporteDTO[]): void {
    facturasBody.replaceChildren();   // vacia la tabla
    facturasPie.replaceChildren();

    if (facturas.length === 0) {
        facturasBody.append(filaVacia(7, VISTAS[vistaActual].vacio));
        return;
    }

    for (const factura of facturas) {
        const estado = document.createElement('span');
        estado.className = `estado ${factura.estado}`;
        estado.textContent = factura.estado.charAt(0) + factura.estado.slice(1).toLowerCase();
        const celdaEstado = celda(estado);
        if (factura.estado === 'FACTURADA' && factura.cae !== null) {
            // Numero de ARCA (Factura C) y CAE debajo del estado
            const datoArca = document.createElement('small');
            datoArca.className = 'dato-arca';
            datoArca.textContent = `C ${factura.numeroArca ?? ''} · CAE ${factura.cae}`;
            celdaEstado.append(datoArca);
        }

        // Las acciones dependen del estado: un borrador se edita o se emite;
        // una emitida se edita o se anula; una facturada (con CAE) solo se ve;
        // una anulada ya no admite cambios.
        const acciones = document.createElement('div');
        acciones.className = 'acciones';
        if (factura.estado === 'BORRADOR') {
            acciones.append(
                boton('Editar', '', () => void abrirFormularioEdicion(factura)),
                boton('Emitir', 'primario', () => void emitirBorrador(factura)),
            );
        } else if (factura.estado === 'EMITIDA') {
            acciones.append(
                boton('Editar', '', () => void abrirFormularioEdicion(factura)),
                boton('Anular', 'aviso', () => void anular(factura)),
            );
        } else if (factura.estado === 'FACTURADA') {
            acciones.append(boton('Ver', '', () => void abrirFormularioEdicion(factura)));
        }
        // Una facturada ya esta registrada en ARCA: no se puede borrar
        if (factura.estado !== 'FACTURADA') {
            acciones.append(boton('Eliminar', 'peligro', () => void eliminar(factura)));
        }

        const fila = document.createElement('tr');
        if (factura.estado === 'ANULADA') fila.className = 'anulada';
        fila.append(
            celda(factura.numeroFactura !== null ? String(factura.numeroFactura) : '—', 'nro'),
            celda(fechaConHora(factura.fechaEmision), 'fecha'),
            celda(clienteConIva(factura), 'cliente'),
            celda(factura.puntoVentaDescripcion, 'pv'),
            celda(totalConItems(factura), 'numero total'),
            celdaEstado,
            celda(acciones),
        );
        facturasBody.append(fila);
    }

    // Pie: cantidad y total. En el historico las anuladas no suman (no son ventas).
    const sumables = vistaActual === 'ANULADA' ? facturas : facturas.filter((f) => f.estado !== 'ANULADA');
    const total = sumables.reduce((suma, f) => suma + f.importeTotal, 0);
    const pie = document.createElement('tr');
    const cantidad = celda(`${facturas.length} ${facturas.length === 1 ? 'comprobante' : 'comprobantes'}`);
    cantidad.colSpan = 3;
    const etiqueta = celda(vistaActual === 'HISTORICO' ? 'Total sin anuladas' : 'Total', 'numero');
    const importe = celda(formatoMoneda.format(total), 'numero');
    const relleno = celda('');
    relleno.colSpan = 2;
    pie.append(cantidad, etiqueta, importe, relleno);
    facturasPie.append(pie);
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

/** Emitir un borrador desde el listado: PUT con estado EMITIDA (ahi toma su numero). */
async function emitirBorrador(factura: FacturaReporteDTO): Promise<void> {
    const confirmado = window.confirm(
        `¿Emitir el borrador de ${factura.clienteDenominacion} por ${formatoMoneda.format(factura.importeTotal)}?`);
    if (!confirmado) return;

    try {
        const emitida = await service.actualizarEstado(factura.id, 'EMITIDA');
        mostrarMensaje(`Factura ${emitida.numeroFactura} emitida por ${formatoMoneda.format(emitida.importeTotal)}.`, 'exito');
        await cargarFacturas();
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
    }
}

/**
 * Manda a ARCA (homologacion) todas las facturas EMITIDA: POST /facturar-emitidas.
 * Las aprobadas pasan a FACTURADA (con CAE); las rechazadas siguen EMITIDA con el motivo.
 */
async function facturarEmitidas(): Promise<void> {
    const cantidad = Number(elemento('contador-EMITIDA').textContent);
    const confirmado = window.confirm(
        `¿Facturar en ARCA ${cantidad === 1 ? 'la factura emitida' : `las ${cantidad} facturas emitidas`}?\n\n`
        + 'Las que ARCA apruebe reciben su CAE y quedan de solo lectura: ya no se pueden editar, anular ni eliminar.');
    if (!confirmado) return;

    // ARCA tarda unos segundos por factura: se avisa y se evita el doble clic
    btnFacturarEmitidas.disabled = true;
    const textoBoton = btnFacturarEmitidas.textContent;
    btnFacturarEmitidas.textContent = 'Facturando en ARCA…';
    try {
        const resultado = await service.facturarEmitidas();
        const resumen = `${resultado.facturadas} ${resultado.facturadas === 1 ? 'factura facturada' : 'facturas facturadas'} en ARCA.`;
        if (resultado.rechazadas === 0) {
            mostrarMensaje(resumen, 'exito');
        } else {
            const motivos = resultado.detalle
                .filter((d) => d.resultado === 'R')
                .map((d) => `• Factura ${d.numeroFactura ?? d.id}: ${d.mensaje ?? 'rechazada'}`);
            mostrarMensaje(`${resumen} ${resultado.rechazadas} rechazada(s), siguen como emitidas:\n${motivos.join('\n')}`, 'error');
        }
        await cargarFacturas();
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
    } finally {
        btnFacturarEmitidas.textContent = textoBoton;
        // el estado "disabled" lo recalcula actualizarContadores al recargar
        btnFacturarEmitidas.disabled = !Number(elemento('contador-EMITIDA').textContent);
    }
}

/** Borrado fisico: DELETE, previa confirmacion. */
async function eliminar(factura: FacturaReporteDTO): Promise<void> {
    const confirmado = window.confirm(
        `¿Eliminar definitivamente ${nombreFactura(factura)}? Esta acción no se puede deshacer.`);
    if (!confirmado) return;

    try {
        await service.eliminarFactura(factura.id);
        mostrarMensaje(`Se eliminó ${nombreFactura(factura)}.`, 'exito');
        await cargarFacturas();
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
    }
}

// =====================================================================
// PARTE 4: EL COMPROBANTE (formulario de alta / edicion, POST o PUT)
// =====================================================================

/** Muestra el comprobante vacio, con la primera opcion de cada desplegable. */
function abrirFormularioNuevo(): void {
    limpiarFormulario();
    tituloFormulario.textContent = 'Nueva factura';
    descripcionFormulario.textContent = 'Completá el comprobante. Podés guardarlo como borrador o emitirlo.';
    mostrarFormulario();
}

/** Trae la factura completa (GET /{id}) y la carga en el comprobante para editarla. */
async function abrirFormularioEdicion(factura: FacturaReporteDTO): Promise<void> {
    try {
        const completa = await service.obtenerFactura(factura.id);
        limpiarFormulario();
        facturaEditando = { id: completa.id, estado: completa.estado, numeroFactura: completa.numeroFactura };

        selectPuntoVenta.value = String(completa.puntoVentaId);
        selectCliente.value = completa.clienteId !== null ? String(completa.clienteId) : '';
        selectCondicionIva.value = String(completa.condicionIvaId);
        selectTipoMoneda.value = String(completa.tipoMonedaId);
        selectListaPrecio.value = completa.listaPrecioId !== null ? String(completa.listaPrecioId) : '';
        inputObservaciones.value = completa.observaciones ?? '';
        items = completa.detalles.map((d) => ({ ...d }));   // copia: se edita en memoria hasta guardar
        llenarArticulos();   // los articulos dependen de la lista de precios elegida
        actualizarLetra();

        if (completa.estado === 'FACTURADA') {
            // Con CAE de ARCA: solo se muestra, no se puede cambiar nada
            tituloFormulario.textContent = `Factura ${completa.numeroFactura} (facturada)`;
            const vence = completa.caeVencimiento
                ? new Date(`${completa.caeVencimiento}T00:00:00`).toLocaleDateString('es-AR') : '—';
            descripcionFormulario.textContent =
                `Autorizada por ARCA (homologación) como Factura C N° ${completa.numeroArca ?? '—'}. `
                + `CAE ${completa.cae ?? '—'}, vence el ${vence}. Es de solo lectura.`;
            numeroComprobante.textContent = `N° ${completa.numeroFactura}`;
            numeroComprobante.classList.remove('pendiente');
            activarSoloLectura();
        } else if (completa.estado === 'EMITIDA') {
            // Una emitida conserva su numero: no puede cambiar de punto de venta ni volver a borrador
            tituloFormulario.textContent = `Editar factura ${completa.numeroFactura}`;
            descripcionFormulario.textContent = 'La factura conserva su número y su punto de venta.';
            numeroComprobante.textContent = `N° ${completa.numeroFactura}`;
            numeroComprobante.classList.remove('pendiente');
            selectPuntoVenta.disabled = true;
            btnGuardarBorrador.hidden = true;
            btnEmitir.textContent = 'Guardar cambios';
        } else {
            tituloFormulario.textContent = 'Editar borrador';
            descripcionFormulario.textContent = `Borrador de ${factura.clienteDenominacion}. Guardá los cambios o emitilo.`;
            btnGuardarBorrador.textContent = 'Guardar cambios';
        }
        mostrarFormulario();
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
    }
}

/** Bloquea el comprobante: campos deshabilitados, sin carga de renglones ni botones de guardar. */
function activarSoloLectura(): void {
    soloLectura = true;
    for (const campo of Array.from(formFactura.elements)) {
        if (campo !== btnCancelar) (campo as HTMLInputElement).disabled = true;
    }
    filaAgregarItem.hidden = true;
    btnGuardarBorrador.hidden = true;
    btnEmitir.hidden = true;
    btnCancelar.textContent = 'Volver';
    dibujarItems();   // sin botones «Quitar»
}

function mostrarFormulario(): void {
    vistaListado.hidden = true;
    seccionFactura.hidden = false;
    marcarMenu(facturaEditando === null ? 'NUEVA' : vistaActual);
    window.scrollTo({ top: 0 });
}

/** Cierra el comprobante, descarta lo cargado y vuelve al modulo de listado. */
function cerrarFormulario(): void {
    mostrarVista(vistaActual);
}

function limpiarFormulario(): void {
    formFactura.reset();   // vuelve cada campo a su valor inicial (en los desplegables, la primera opcion)
    facturaEditando = null;
    items = [];
    // Deshace el modo solo lectura (factura facturada)
    soloLectura = false;
    for (const campo of Array.from(formFactura.elements)) {
        (campo as HTMLInputElement).disabled = false;
    }
    filaAgregarItem.hidden = false;
    btnEmitir.hidden = false;
    btnCancelar.textContent = 'Cancelar';
    // Botones y campos como en el alta (la edicion de una emitida los cambia)
    selectPuntoVenta.disabled = false;
    btnGuardarBorrador.hidden = false;
    btnGuardarBorrador.textContent = 'Guardar borrador';
    btnEmitir.textContent = 'Emitir factura';
    numeroComprobante.textContent = 'El número se asigna al emitir';
    numeroComprobante.classList.add('pendiente');
    llenarArticulos();
    actualizarLetra();
}

// --- Desplegables ---

/** Reemplaza las opciones de un <select>: muestra el nombre y guarda el ID en el value. */
function llenarSelect(select: HTMLSelectElement, opciones: { id: number; nombre: string }[], opcionVacia?: string): void {
    select.replaceChildren();
    if (opcionVacia !== undefined) {
        select.append(new Option(opcionVacia, ''));
    }
    for (const opcion of opciones) {
        select.append(new Option(opcion.nombre, String(opcion.id)));
    }
}

/** Pide los catalogos (GET /api/catalogos) y arma los desplegables del comprobante. */
async function cargarCatalogos(): Promise<void> {
    try {
        catalogos = await service.obtenerCatalogos();
        llenarSelect(selectPuntoVenta, catalogos.puntosVenta);
        llenarSelect(selectCliente, catalogos.clientes, 'Consumidor Final');   // sin cliente
        llenarSelect(selectCondicionIva, catalogos.condicionesIva);
        llenarSelect(selectTipoMoneda, catalogos.monedas);
        llenarSelect(selectListaPrecio, catalogos.listasPrecio);
        llenarArticulos();
        actualizarLetra();
    } catch (error) {
        mostrarMensaje('No se pudieron cargar las opciones del formulario. ' + textoDeError(error), 'error');
    }
}

/** El desplegable de articulos muestra solo los que tienen precio en la lista elegida. */
function llenarArticulos(): void {
    const listaId = Number(selectListaPrecio.value);
    const articulos = (catalogos?.articulos ?? []).filter((a) => a.listaPrecioId === listaId);
    llenarSelect(
        selectItemArticulo,
        articulos.map((a) => ({ id: a.articuloId, nombre: `${a.nombre} — ${formatoMoneda.format(a.precio)}` })),
        articulos.length > 0 ? 'Elegí un artículo' : 'No hay artículos en esta lista',
    );
    actualizarPrecioItem();
    dibujarItems();   // nombres, precios y totales dependen de la lista
}

/** Muestra el precio unitario del articulo elegido en el renglon de carga (sale de la lista de precios). */
function actualizarPrecioItem(): void {
    const articulo = articuloDeLista(numeroOpcional(selectItemArticulo));
    celdaItemPrecio.textContent = articulo ? formatoMoneda.format(articulo.precio) : '—';
}

/**
 * Letra del comprobante, como en una factura impresa: A si el cliente es Responsable
 * Inscripto, B para el resto. Es solo visual (no se guarda en la base).
 */
function actualizarLetra(): void {
    const condicion = selectCondicionIva.selectedOptions[0]?.textContent ?? '';
    const esA = condicion.toLowerCase().includes('inscripto');
    letraComprobante.textContent = esA ? 'A' : 'B';
    codigoComprobante.textContent = esA ? 'Cód. 001' : 'Cód. 006';
}

/** Articulo de la lista de precios elegida (nombre y precio) o undefined si no esta. */
function articuloDeLista(articuloId: number | undefined) {
    const listaId = Number(selectListaPrecio.value);
    return catalogos?.articulos.find((a) => a.articuloId === articuloId && a.listaPrecioId === listaId);
}

// --- Renglones ---

/** Valida los campos del renglon y lo agrega al array en memoria. */
function agregarItem(): void {
    const articuloId = numeroOpcional(selectItemArticulo);
    const cantidad = numeroOpcional(inputItemCantidad);
    const bonificacion = numeroOpcional(inputItemBonificacion);

    // Validaciones para ayudar al usuario. El backend vuelve a validar todo,
    // porque al servidor se le puede pegar sin pasar por esta pantalla (ej. Postman).
    if (articuloId === undefined || articuloId <= 0) {
        mostrarMensaje('Elegí un artículo.', 'error');
        return;
    }
    if (cantidad === undefined || !Number.isInteger(cantidad) || cantidad < 1) {
        mostrarMensaje('La cantidad debe ser un número entero mayor a 0.', 'error');
        return;
    }
    if (bonificacion !== undefined && (bonificacion < 0 || bonificacion > 100)) {
        mostrarMensaje('La bonificación debe estar entre 0 y 100.', 'error');
        return;
    }

    items.push({ articuloId, cantidad, porcentajeBonificacion: bonificacion });

    // Limpia los campos para cargar el siguiente renglon
    selectItemArticulo.value = '';
    inputItemCantidad.value = '';
    inputItemBonificacion.value = '';
    actualizarPrecioItem();
    selectItemArticulo.focus();

    dibujarItems();
}

function quitarItem(indice: number): void {
    items.splice(indice, 1);
    dibujarItems();
}

/** Redibuja los renglones y recalcula los totales a partir del array en memoria. */
function dibujarItems(): void {
    itemsBody.replaceChildren();
    let total = 0;

    if (items.length === 0) {
        itemsBody.append(filaVacia(6, 'Todavía no hay renglones. Elegí un artículo abajo y tocá «Agregar».'));
    }

    items.forEach((item, indice) => {
        const articulo = articuloDeLista(item.articuloId);
        const bonificacion = item.porcentajeBonificacion ?? 0;
        // Mismo calculo que FacturaService.cargarDetalles (el backend lo recalcula al guardar)
        const subtotal = articulo ? redondear((item.cantidad ?? 0) * articulo.precio * (1 - bonificacion / 100)) : 0;
        total += subtotal;

        const fila = document.createElement('tr');
        fila.append(
            celda(articulo ? articulo.nombre : `Artículo ${item.articuloId} (sin precio en esta lista)`),
            celda(String(item.cantidad), 'numero'),
            celda(articulo ? formatoMoneda.format(articulo.precio) : '—', 'numero'),
            celda(String(bonificacion), 'numero'),
            celda(articulo ? formatoMoneda.format(subtotal) : '—', 'numero'),
            celda(soloLectura ? '' : boton('Quitar', 'peligro', () => quitarItem(indice))),
        );
        itemsBody.append(fila);
    });

    // Precios con IVA incluido: neto = total / 1,21
    const neto = redondear(total / (1 + TASA_IVA));
    totalNeto.textContent = formatoMoneda.format(neto);
    totalIva.textContent = formatoMoneda.format(redondear(total - neto));
    totalComprobante.textContent = formatoMoneda.format(redondear(total));

    // sin renglones no se puede guardar ni emitir (y en solo lectura, nunca)
    btnEmitir.disabled = soloLectura || items.length === 0;
    btnGuardarBorrador.disabled = soloLectura || items.length === 0;
}

// --- Guardar ---

/** Junta cabecera + renglones del comprobante en un FacturaRequestDTO. */
function leerFormulario(): FacturaRequestDTO {
    return {
        puntoVentaId: numeroOpcional(selectPuntoVenta),
        // Vacio = Consumidor Final. Viaja como null (y no se omite) para que, al editar,
        // el backend sepa que hay que quitarle el cliente.
        clienteId: numeroOpcional(selectCliente) ?? null,
        condicionIvaId: numeroOpcional(selectCondicionIva),
        tipoMonedaId: numeroOpcional(selectTipoMoneda),
        listaPrecioId: numeroOpcional(selectListaPrecio),
        observaciones: inputObservaciones.value.trim(),
        detalles: items,
    };
}

/**
 * Guarda lo que hay en el comprobante:
 *  - alta:    POST con estado BORRADOR o EMITIDA.
 *  - edicion: PUT sobre la misma factura. Si es un borrador y va con EMITIDA, ademas se emite;
 *             si ya estaba emitida, se guardan los cambios y conserva su numero.
 * Si sale bien vuelve al modulo correspondiente; si falla deja todo cargado para corregir.
 */
async function guardarFactura(estado: 'BORRADOR' | 'EMITIDA'): Promise<void> {
    if (items.length === 0) {
        mostrarMensaje('Agregá al menos un renglón antes de guardar.', 'error');
        return;
    }

    const factura: FacturaRequestDTO = { ...leerFormulario(), estado };
    const editando = facturaEditando;

    // evita guardar dos veces con doble clic
    btnEmitir.disabled = true;
    btnGuardarBorrador.disabled = true;
    try {
        const guardada = editando !== null
            ? await service.actualizarFactura(editando.id, factura)
            : await service.crearFactura(factura);

        const total = formatoMoneda.format(guardada.importeTotal);
        if (editando?.estado === 'EMITIDA') {
            mostrarMensaje(`Factura ${guardada.numeroFactura} actualizada (total ${total}).`, 'exito');
        } else if (estado === 'EMITIDA') {
            mostrarMensaje(`Factura ${guardada.numeroFactura} emitida por ${total}.`, 'exito');
        } else {
            mostrarMensaje(`Borrador ${editando !== null ? 'actualizado' : 'guardado'} (total ${total}).`, 'exito');
        }
        // Vuelve al modulo donde quedo el comprobante
        mostrarVista(guardada.estado);
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');   // los datos quedan en el comprobante
        window.scrollTo({ top: 0 });
        btnEmitir.disabled = items.length === 0;
        btnGuardarBorrador.disabled = items.length === 0;
    }
}

// =====================================================================
// PARTE 5: EVENTOS Y ARRANQUE
// =====================================================================

// Barra lateral
btnNuevaFactura.addEventListener('click', abrirFormularioNuevo);
btnFacturarEmitidas.addEventListener('click', () => void facturarEmitidas());
for (const b of botonesMenu) {
    b.addEventListener('click', () => mostrarVista(b.dataset.vista as Vista));
}

// Filtros
formFiltros.addEventListener('submit', (evento) => {
    evento.preventDefault();   // sin esto el navegador recargaria la pagina al enviar el form
    void cargarFacturas();
});
btnLimpiarFiltros.addEventListener('click', () => {
    formFiltros.reset();
    void cargarFacturas();
});

// Comprobante
selectListaPrecio.addEventListener('change', llenarArticulos);
selectCondicionIva.addEventListener('change', actualizarLetra);
btnAgregarItem.addEventListener('click', agregarItem);
selectItemArticulo.addEventListener('change', actualizarPrecioItem);

// Cantidad: solo enteros. Bloquea las teclas de decimales, signos y notacion cientifica.
inputItemCantidad.addEventListener('keydown', (evento: KeyboardEvent) => {
    if (['.', ',', 'e', 'E', '-', '+'].includes(evento.key)) {
        evento.preventDefault();
    }
});
// Si se pega un valor con decimales (ej. "2.5"), lo deja en la parte entera
inputItemCantidad.addEventListener('change', () => {
    const valor = Number(inputItemCantidad.value);
    if (inputItemCantidad.value !== '' && !Number.isInteger(valor)) {
        inputItemCantidad.value = String(Math.trunc(valor));
    }
});

// Enter en los campos del renglon agrega el renglon (en vez de enviar el formulario)
const camposRenglon: HTMLElement[] = [selectItemArticulo, inputItemCantidad, inputItemBonificacion];
for (const campo of camposRenglon) {
    campo.addEventListener('keydown', (evento: KeyboardEvent) => {
        if (evento.key === 'Enter') {
            evento.preventDefault();
            agregarItem();
        }
    });
}

// "Emitir factura" (o "Guardar cambios" en una emitida) es el submit del formulario
formFactura.addEventListener('submit', (evento) => {
    evento.preventDefault();
    void guardarFactura('EMITIDA');
});
btnGuardarBorrador.addEventListener('click', () => void guardarFactura('BORRADOR'));
btnCancelar.addEventListener('click', cerrarFormulario);

// Al abrir la pagina: historico visible, comprobante oculto (atributo hidden en el HTML)
void cargarCatalogos();
mostrarVista('HISTORICO');
