import {
    ArticuloExplorerDTO,
    CatalogosDTO,
    ClienteExplorerDTO,
    EstadoFactura,
    FacturaDetalleRequestDTO,
    FacturaFiltrosDTO,
    FacturaReporteDTO,
    FacturaRequestDTO,
    PaginaDTO,
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
        descripcion: 'Facturas autorizadas por ARCA (homologación), con CAE. No se editan ni se borran; se anulan en ARCA con una Nota de Crédito.',
        vacio: 'No hay facturas facturadas. Desde «Emitidas» usá «Facturar emitidas en ARCA».',
    },
    ANULADA: {
        titulo: 'Anuladas',
        descripcion: 'Facturas anuladas. Quedan como registro y no se pueden modificar. Las que estaban facturadas muestran su Nota de Crédito de ARCA.',
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
const dialogoError = elemento<HTMLDialogElement>('dialogo-error');
const textoDialogoError = elemento<HTMLDivElement>('dialogo-error-texto');
const btnDialogoError = elemento<HTMLButtonElement>('btn-dialogo-error');
const botonesMenu = Array.from(document.querySelectorAll<HTMLButtonElement>('.menu button[data-vista]'));
const btnNuevaFactura = elemento<HTMLButtonElement>('btn-nueva-factura');
const btnFacturarEmitidas = elemento<HTMLButtonElement>('btn-facturar-emitidas');
const menuFacturar = elemento<HTMLDivElement>('menu-facturar');
const opcionesFacturar = elemento<HTMLDivElement>('opciones-facturar');
const btnFacturarTodas = elemento<HTMLButtonElement>('btn-facturar-todas');
const btnFacturarPagina = elemento<HTMLButtonElement>('btn-facturar-pagina');

// Modulo de listado
const vistaListado = elemento<HTMLElement>('vista-listado');
const tituloListado = elemento<HTMLHeadingElement>('titulo-listado');
const descripcionListado = elemento<HTMLParagraphElement>('descripcion-listado');
const formFiltros = elemento<HTMLFormElement>('form-filtros');
const avisoFiltros = elemento<HTMLParagraphElement>('aviso-filtros');
const filtroFechaDesde = elemento<HTMLInputElement>('filtro-fecha-desde');
const filtroFechaHasta = elemento<HTMLInputElement>('filtro-fecha-hasta');
const campoFiltroEstado = elemento<HTMLLabelElement>('campo-filtro-estado');
const filtroEstado = elemento<HTMLSelectElement>('filtro-estado');
const filtroMontoMinimo = elemento<HTMLInputElement>('filtro-monto-minimo');
const filtroNumero = elemento<HTMLInputElement>('filtro-numero');
const filtroCliente = elemento<HTMLInputElement>('filtro-cliente');
const btnLimpiarFiltros = elemento<HTMLButtonElement>('btn-limpiar-filtros');
const facturasBody = elemento<HTMLTableSectionElement>('facturas-body');
const facturasPie = elemento<HTMLTableSectionElement>('facturas-pie');
const paginacion = elemento<HTMLDivElement>('paginacion');
const btnPaginaAnterior = elemento<HTMLButtonElement>('btn-pagina-anterior');
const btnPaginaSiguiente = elemento<HTMLButtonElement>('btn-pagina-siguiente');
const textoPagina = elemento<HTMLSpanElement>('texto-pagina');

/** Facturas por pagina en el listado (la paginacion la hace el backend). */
const TAMANO_PAGINA = 10;
/** Pagina del listado que se esta mostrando (empieza en 0). Vuelve a 0 al cambiar de vista o de filtros. */
let paginaActual = 0;
/** IDs de las facturas EMITIDA de la pagina que se esta viendo (con los filtros aplicados). */
let emitidasDeLaPagina: number[] = [];

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
// Visores y botones de los exploradores (los select de arriba quedan ocultos y guardan el ID)
// Los visores son botones con forma de campo: muestran lo elegido y al hacer clic abren el explorador
const btnClienteVisor = elemento<HTMLButtonElement>('cliente-visor');
const btnQuitarCliente = elemento<HTMLButtonElement>('btn-quitar-cliente');
const btnArticuloVisor = elemento<HTMLButtonElement>('item-articulo-visor');
// Explorador de clientes
const dialogClientes = elemento<HTMLDialogElement>('explorador-clientes');
const formFiltrosClientes = elemento<HTMLFormElement>('filtros-exp-clientes');
const inputExpClienteTexto = elemento<HTMLInputElement>('exp-cliente-texto');
const bodyExpClientes = elemento<HTMLTableSectionElement>('exp-clientes-body');
const cantidadExpClientes = elemento<HTMLSpanElement>('exp-clientes-cantidad');
const btnExpConsumidorFinal = elemento<HTMLButtonElement>('btn-exp-consumidor-final');
// Explorador de articulos
const dialogArticulos = elemento<HTMLDialogElement>('explorador-articulos');
const formFiltrosArticulos = elemento<HTMLFormElement>('filtros-exp-articulos');
const inputExpArticuloTexto = elemento<HTMLInputElement>('exp-articulo-texto');
const selectExpArticuloRubro = elemento<HTMLSelectElement>('exp-articulo-rubro');
const selectExpArticuloMarca = elemento<HTMLSelectElement>('exp-articulo-marca');
const bodyExpArticulos = elemento<HTMLTableSectionElement>('exp-articulos-body');
const cantidadExpArticulos = elemento<HTMLSpanElement>('exp-articulos-cantidad');
const textoExpArticulosLista = elemento<HTMLParagraphElement>('exp-articulos-lista');
const inputItemCantidad = elemento<HTMLInputElement>('item-cantidad');
const celdaItemPrecio = elemento<HTMLTableCellElement>('item-precio');
const celdaItemLista = elemento<HTMLTableCellElement>('item-lista');
const inputItemBonificacion = elemento<HTMLInputElement>('item-bonificacion');
const btnAgregarItem = elemento<HTMLButtonElement>('btn-agregar-item');
const itemsBody = elemento<HTMLTableSectionElement>('items-body');
const totalNeto = elemento<HTMLElement>('total-neto');
const totalIva = elemento<HTMLElement>('total-iva');
const totalComprobante = elemento<HTMLElement>('total-comprobante');
const btnCancelar = elemento<HTMLButtonElement>('btn-cancelar');
const btnGuardarBorrador = elemento<HTMLButtonElement>('btn-guardar-borrador');
const btnEmitir = elemento<HTMLButtonElement>('btn-emitir');
const btnImprimir = elemento<HTMLButtonElement>('btn-imprimir');
const btnCerrarFormulario = elemento<HTMLButtonElement>('btn-cerrar-formulario');
const seccionImpresion = elemento<HTMLElement>('impresion');
// Fila de carga de renglones (se oculta cuando la factura es de solo lectura)
const filaAgregarItem = btnAgregarItem.closest('tr') as HTMLTableRowElement;

/** true cuando se esta viendo una factura FACTURADA: nada se puede cambiar. */
let soloLectura = false;

// --- Mensajes al usuario ---
let temporizadorMensaje: number | undefined;

function mostrarMensaje(texto: string, tipo: 'exito' | 'error'): void {
    // Con el comprobante abierto, los errores van a una ventana emergente (el cartel queda arriba
    // de todo y no se ve si el usuario esta mas abajo en el formulario)
    if (tipo === 'error' && !seccionFactura.hidden) {
        mostrarErrores([{ mensaje: texto }]);
        return;
    }
    mensaje.textContent = texto;
    mensaje.className = tipo;              // la clase CSS lo pinta verde o rojo
    window.clearTimeout(temporizadorMensaje);
    // Los exitos se ocultan solos; los errores quedan visibles hasta la proxima accion
    if (tipo === 'exito') {
        temporizadorMensaje = window.setTimeout(() => (mensaje.className = ''), 6000);
    }
    // Sube hasta el cartel para que se vea aunque la pagina este scrolleada
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// --- Errores del comprobante ---

/** Un problema del comprobante y, si corresponde, el campo donde esta. */
interface Problema {
    mensaje: string;
    campo?: HTMLElement;      // se pinta en rojo
    enfocar?: HTMLElement;    // recibe el foco al cerrar la ventana (por defecto, el mismo campo)
    cambio?: HTMLElement;     // cuyo "input"/"change" saca el rojo (por defecto, el mismo campo)
}

/** Campo al que se lleva el foco cuando el usuario cierra la ventana de error. */
let campoAEnfocar: HTMLElement | null = null;

/** Saca el rojo de todos los campos marcados. */
function limpiarResaltados(): void {
    for (const campo of Array.from(document.querySelectorAll('.invalido'))) {
        campo.classList.remove('invalido');
    }
}

/** Pinta un campo en rojo hasta que el usuario lo modifica. */
function resaltar(problema: Problema): void {
    if (!problema.campo) return;
    const campo = problema.campo;
    campo.classList.add('invalido');
    const fuente = problema.cambio ?? campo;
    const quitar = () => campo.classList.remove('invalido');
    fuente.addEventListener('input', quitar, { once: true });
    fuente.addEventListener('change', quitar, { once: true });
}

/**
 * Muestra una ventana emergente con uno o varios errores del comprobante, pinta en rojo los
 * campos involucrados y, al cerrarla, baja hasta el primero y le pone el foco.
 */
function mostrarErrores(problemas: Problema[]): void {
    limpiarResaltados();
    problemas.forEach(resaltar);

    if (problemas.length === 1) {
        const parrafo = document.createElement('p');
        parrafo.textContent = problemas[0].mensaje;
        textoDialogoError.replaceChildren(parrafo);
    } else {
        const lista = document.createElement('ul');
        for (const problema of problemas) {
            const li = document.createElement('li');
            li.textContent = problema.mensaje;
            lista.append(li);
        }
        textoDialogoError.replaceChildren(lista);
    }

    const primero = problemas.find((p) => p.campo);
    campoAEnfocar = primero ? (primero.enfocar ?? primero.campo ?? null) : null;
    if (!dialogoError.open) dialogoError.showModal();
}

/** Al cerrar la ventana de error (boton, Escape): lleva al usuario al primer campo con problema. */
function alCerrarDialogoError(): void {
    if (!campoAEnfocar) return;
    campoAEnfocar.scrollIntoView({ behavior: 'smooth', block: 'center' });
    campoAEnfocar.focus({ preventScroll: true });
    campoAEnfocar = null;
}

/**
 * Relaciona un error del backend con el campo del comprobante (por las palabras del mensaje),
 * para pintarlo en rojo. Si no reconoce el campo, el error se muestra igual, sin resaltar nada.
 */
function problemaDelBackend(texto: string): Problema {
    const t = texto.toLowerCase();
    const reglas: [string[], HTMLElement, HTMLElement?][] = [
        [['punto de venta', 'puntoventa'], selectPuntoVenta],
        [['lista de precio', 'listaprecio'], selectListaPrecio],
        [['condicion iva', 'condición iva', 'condicioniva'], selectCondicionIva],
        [['moneda'], selectTipoMoneda],
        [['cliente'], btnClienteVisor],
        [['cantidad'], inputItemCantidad],
        [['bonificacion', 'bonificación'], inputItemBonificacion],
        [['articulo', 'artículo', 'item', 'renglon', 'renglón'], btnArticuloVisor],
    ];
    for (const [palabras, campo, enfocar] of reglas) {
        if (palabras.some((p) => t.includes(p))) {
            return { mensaje: texto, campo, enfocar, cambio: campo === btnClienteVisor ? selectCliente
                : campo === btnArticuloVisor ? selectItemArticulo : undefined };
        }
    }
    return { mensaje: texto };
}

/**
 * Revisa el comprobante antes de mandarlo al backend y junta TODOS los problemas
 * (asi el usuario los corrige de una vez). El backend vuelve a validar igual.
 */
function validarComprobante(): Problema[] {
    const problemas: Problema[] = [];
    if (!selectPuntoVenta.value) {
        problemas.push({ mensaje: 'Elegí el punto de venta.', campo: selectPuntoVenta });
    }
    if (!selectTipoMoneda.value) {
        problemas.push({ mensaje: 'Elegí la moneda.', campo: selectTipoMoneda });
    }
    if (!selectListaPrecio.value) {
        problemas.push({ mensaje: 'Elegí la lista de precios.', campo: selectListaPrecio });
    }
    if (!selectCondicionIva.value) {
        problemas.push({ mensaje: 'Elegí la condición IVA del cliente.', campo: selectCondicionIva });
    }
    // Un articulo elegido (o una cantidad escrita) que no se agrego con "Agregar" se perderia
    const renglonSinAgregar = selectItemArticulo.value !== '' || inputItemCantidad.value !== '' || inputItemCantidad.validity.badInput;
    if (renglonSinAgregar) {
        problemas.push({
            mensaje: 'Hay un renglón cargado sin agregar: tocá «Agregar» (o vacialo) antes de guardar.',
            campo: btnArticuloVisor, enfocar: btnAgregarItem, cambio: selectItemArticulo,
        });
    } else if (items.length === 0) {
        problemas.push({
            mensaje: 'Agregá al menos un renglón: hacé clic en «Elegí un artículo», cargá la cantidad y tocá «Agregar».',
            campo: btnArticuloVisor, cambio: selectItemArticulo,
        });
    }
    return problemas;
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

/** Nombre del cliente y, debajo, su CUIT (si tiene) y su condicion IVA. */
function clienteConIva(factura: FacturaReporteDTO): HTMLElement {
    const contenedor = document.createElement('span');
    const nombre = document.createElement('strong');
    nombre.textContent = factura.clienteDenominacion;
    const iva = document.createElement('small');
    iva.textContent = factura.clienteCuit
        ? `CUIT ${factura.clienteCuit} · ${factura.condicionIva}`
        : factura.condicionIva;
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

/** Boton cuadrado solo con icono (SVG); el texto va en el tooltip y en aria-label. */
function botonIcono(svg: string, titulo: string, alHacerClic: () => void): HTMLButtonElement {
    const b = document.createElement('button');
    b.type = 'button';
    b.className = 'boton chico icono';
    b.title = titulo;
    b.setAttribute('aria-label', titulo);
    b.innerHTML = svg;   // SVG fijo del codigo (no viene de datos del usuario)
    b.addEventListener('click', alHacerClic);
    return b;
}

/** Icono de impresora (trazo, hereda el color del boton). */
const ICONO_IMPRIMIR = '<svg aria-hidden="true" viewBox="0 0 24 24" width="16" height="16" fill="none" '
    + 'stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
    + '<path d="M6 9V3h12v6"/><rect x="3" y="9" width="18" height="8" rx="2"/><path d="M6 14h12v7H6z"/></svg>';

// =====================================================================
// PARTE 2: NAVEGACION ENTRE MODULOS
// =====================================================================

/** Muestra un modulo del listado (borradores, emitidas, anuladas o historico). */
function mostrarVista(vista: Vista, conservarPagina = false): void {
    vistaActual = vista;
    avisoFiltros.hidden = true;   // el aviso de "sin filtros" es de la busqueda anterior
    seccionFactura.hidden = true;
    vistaListado.hidden = false;
    limpiarFormulario();

    tituloListado.textContent = VISTAS[vista].titulo;
    descripcionListado.textContent = VISTAS[vista].descripcion;
    // El filtro de estado solo tiene sentido en el historico
    campoFiltroEstado.hidden = vista !== 'HISTORICO';
    if (vista !== 'HISTORICO') filtroEstado.value = '';
    // Facturar en ARCA tiene sentido donde se ven las emitidas
    menuFacturar.hidden = vista !== 'EMITIDA' && vista !== 'HISTORICO';
    cerrarMenuFacturar();

    marcarMenu(vista);
    // Cada vista arranca en su primera pagina, salvo al volver del comprobante (se queda donde estaba)
    if (!conservarPagina) paginaActual = 0;
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
        numero: numeroOpcional(filtroNumero),
        cliente: filtroCliente.value.trim() || undefined,
    };
}

/** Pide las facturas al backend (GET), actualiza los contadores y redibuja la tabla. */
async function cargarFacturas(): Promise<void> {
    try {
        // Sin filtros: para los contadores de la barra lateral
        const todas = await service.obtenerFacturas({});
        actualizarContadores(todas);

        // Con los filtros del modulo actual: solo la pagina actual (ya viene ordenada del backend)
        const pagina = await service.obtenerPagina(leerFiltros(), paginaActual, TAMANO_PAGINA);
        paginaActual = pagina.pagina;   // el backend la ajusta si la pagina pedida ya no existe
        dibujarFacturas(pagina);
        dibujarPaginacion(pagina);
    } catch (error) {
        const vacia: PaginaDTO<FacturaReporteDTO> =
            { contenido: [], pagina: 0, tamano: TAMANO_PAGINA, totalElementos: 0, totalPaginas: 0, importeTotal: 0 };
        dibujarFacturas(vacia);
        dibujarPaginacion(vacia);
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

/** Texto "Pagina X de Y" y botones Anterior/Siguiente. Se oculta si todo entra en una sola pagina. */
function dibujarPaginacion(pagina: PaginaDTO<FacturaReporteDTO>): void {
    const totalPaginas = Math.max(1, pagina.totalPaginas);
    paginacion.hidden = totalPaginas <= 1;
    textoPagina.textContent = `Página ${pagina.pagina + 1} de ${totalPaginas}`;
    btnPaginaAnterior.disabled = pagina.pagina === 0;
    btnPaginaSiguiente.disabled = pagina.pagina >= totalPaginas - 1;
}

function dibujarFacturas(pagina: PaginaDTO<FacturaReporteDTO>): void {
    const facturas = pagina.contenido;
    emitidasDeLaPagina = facturas.filter((f) => f.estado === 'EMITIDA').map((f) => f.id);
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
        } else if (factura.estado === 'ANULADA' && factura.caeNotaCredito !== null) {
            // Anulada en ARCA: numero y CAE de la Nota de Credito C que la anulo
            const datoArca = document.createElement('small');
            datoArca.className = 'dato-arca';
            datoArca.textContent = `NC C ${factura.numeroNotaCredito ?? ''} · CAE ${factura.caeNotaCredito}`;
            celdaEstado.append(datoArca);
        }

        // Las acciones dependen del estado: un borrador se edita o se emite;
        // una emitida se edita o se anula; una facturada (con CAE) se ve o se anula
        // en ARCA con una nota de credito; una anulada ya no admite cambios.
        const acciones = document.createElement('div');
        acciones.className = 'acciones';
        if (factura.estado === 'BORRADOR') {
            acciones.append(
                boton('Editar', '', () => void abrirFormularioEdicion(factura)),
                boton('Emitir', 'primario', () => void emitirBorrador(factura)),
            );
        } else if (factura.estado === 'EMITIDA') {
            // Una emitida tambien se imprime (sin CAE: todavia no la autorizo ARCA)
            const btnImprimirFila: HTMLButtonElement = botonIcono(ICONO_IMPRIMIR, 'Imprimir / Guardar PDF (sin CAE)',
                () => void imprimirFactura(factura.id, btnImprimirFila));
            // Facturar solo esta factura en ARCA (las demas emitidas no se tocan)
            const btnFacturarFila: HTMLButtonElement = boton('Facturar', 'primario',
                () => void facturarUna(factura, btnFacturarFila));
            btnFacturarFila.title = 'Pedir el CAE a ARCA solo para esta factura';
            // Emitida: los 4 botones con texto en 2x2 y la impresora aparte, a la derecha (ver CSS)
            acciones.classList.add('acciones-emitida');
            acciones.append(
                btnFacturarFila,
                boton('Editar', '', () => void abrirFormularioEdicion(factura)),
                btnImprimirFila,
                boton('Anular', 'aviso', () => void anular(factura)),
            );
        } else if (factura.estado === 'FACTURADA') {
            const btnImprimirFila: HTMLButtonElement = botonIcono(ICONO_IMPRIMIR, 'Imprimir / Guardar PDF',
                () => void imprimirFactura(factura.id, btnImprimirFila));
            acciones.append(
                boton('Ver', '', () => void abrirFormularioEdicion(factura)),
                btnImprimirFila,   // imprime directo desde el listado, sin abrir "Ver"
                boton('Anular en ARCA', 'aviso', () => void anularEnArca(factura)),
            );
        } else if (factura.estado === 'ANULADA' && factura.cae !== null) {
            acciones.append(boton('Ver', '', () => void abrirFormularioEdicion(factura)));
        }
        // Lo que paso por ARCA (facturada, o anulada con nota de credito) queda registrado: no se borra
        if (factura.cae === null) {
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

    // Pie: cantidad y total de TODOS los resultados filtrados (no solo de esta pagina).
    // Los calcula el backend; en el historico las anuladas no suman (no son ventas).
    const total = pagina.importeTotal;
    const desde = pagina.pagina * pagina.tamano + 1;
    const hasta = desde + facturas.length - 1;
    const pie = document.createElement('tr');
    const cantidad = celda(pagina.totalPaginas > 1
        ? `${desde}–${hasta} de ${pagina.totalElementos} comprobantes`
        : `${pagina.totalElementos} ${pagina.totalElementos === 1 ? 'comprobante' : 'comprobantes'}`);
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

/**
 * Anula en ARCA una factura FACTURADA: POST /anular-en-arca. ARCA no permite borrar un
 * comprobante con CAE, asi que se emite una Nota de Credito C asociada por el mismo total.
 */
async function anularEnArca(factura: FacturaReporteDTO): Promise<void> {
    const confirmado = window.confirm(
        `¿Anular en ARCA la factura ${factura.numeroFactura} (Factura C N° ${factura.numeroArca}) `
        + `por ${formatoMoneda.format(factura.importeTotal)}?\n\n`
        + 'Se va a emitir una Nota de Crédito C asociada por el mismo importe. No se puede deshacer.');
    if (!confirmado) return;

    const motivo = window.prompt(`Motivo de anulación de la factura ${factura.numeroFactura}:`);
    if (motivo === null) return;   // el usuario apreto "Cancelar"
    if (motivo.trim() === '') {
        mostrarMensaje('Para anular en ARCA hay que indicar un motivo.', 'error');
        return;
    }

    try {
        const nc = await service.anularEnArca(factura.id, motivo.trim());
        mostrarMensaje(`Factura ${factura.numeroFactura} anulada en ARCA con la Nota de Crédito C `
            + `N° ${nc.numeroNotaCredito} (CAE ${nc.caeNotaCredito}).`, 'exito');
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

/** Abre el desplegable de "Facturar emitidas en ARCA" con la cantidad de cada opcion. */
function abrirMenuFacturar(): void {
    const total = Number(elemento('contador-EMITIDA').textContent);
    const enPagina = emitidasDeLaPagina.length;
    btnFacturarTodas.innerHTML = 'Facturar <strong>todas</strong> las facturas emitidas';
    btnFacturarTodas.append(crear('small', `${total} en total, sin importar filtros ni página`));
    btnFacturarTodas.disabled = total === 0;
    btnFacturarPagina.textContent = 'Facturar todas las de esta página';
    btnFacturarPagina.append(crear('small', enPagina === 0
        ? 'No hay emitidas en esta página'
        : `${enPagina} ${enPagina === 1 ? 'emitida' : 'emitidas'} en la página que estás viendo (con los filtros aplicados)`));
    btnFacturarPagina.disabled = enPagina === 0;
    opcionesFacturar.hidden = false;
    btnFacturarEmitidas.setAttribute('aria-expanded', 'true');
}

function cerrarMenuFacturar(): void {
    opcionesFacturar.hidden = true;
    btnFacturarEmitidas.setAttribute('aria-expanded', 'false');
}

/**
 * Manda a ARCA (homologacion) facturas EMITIDA: POST /facturar-emitidas.
 * soloPagina = false: TODAS las emitidas. true: solo las emitidas de la pagina actual.
 * Las aprobadas pasan a FACTURADA (con CAE); las rechazadas siguen EMITIDA con el motivo.
 */
async function facturarEmitidas(soloPagina: boolean): Promise<void> {
    cerrarMenuFacturar();
    const ids = soloPagina ? [...emitidasDeLaPagina] : undefined;
    const cantidad = ids ? ids.length : Number(elemento('contador-EMITIDA').textContent);
    if (cantidad === 0) return;
    const queFacturas = cantidad === 1
        ? (soloPagina ? 'la factura emitida de esta página' : 'la factura emitida')
        : (soloPagina ? `las ${cantidad} facturas emitidas de esta página` : `TODAS las facturas emitidas (${cantidad})`);
    const confirmado = window.confirm(
        `¿Facturar en ARCA ${queFacturas}?\n\n`
        + 'Las que ARCA apruebe reciben su CAE y quedan de solo lectura: ya no se pueden editar, anular ni eliminar.');
    if (!confirmado) return;

    // ARCA tarda unos segundos por factura: se avisa y se evita el doble clic
    btnFacturarEmitidas.disabled = true;
    const textoBoton = btnFacturarEmitidas.textContent;
    btnFacturarEmitidas.textContent = 'Facturando en ARCA…';
    try {
        const resultado = await service.facturarEmitidas(ids);
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

/**
 * Factura en ARCA una sola factura EMITIDA (boton "Facturar" de su fila).
 * Usa el mismo POST /facturar-emitidas, pero mandando solo su ID.
 */
async function facturarUna(factura: FacturaReporteDTO, botonUsado: HTMLButtonElement): Promise<void> {
    const confirmado = window.confirm(
        `¿Facturar en ARCA la factura ${factura.numeroFactura ?? factura.id} (${factura.clienteDenominacion})?\n\n`
        + 'Si ARCA la aprueba recibe su CAE y queda de solo lectura: ya no se puede editar, anular ni eliminar.');
    if (!confirmado) return;

    // ARCA tarda unos segundos: se avisa en el boton y se evita el doble clic
    botonUsado.disabled = true;
    botonUsado.textContent = 'Facturando…';
    try {
        const resultado = await service.facturarEmitidas([factura.id]);
        const d = resultado.detalle[0];
        if (d && d.resultado === 'A') {
            mostrarMensaje(`Factura ${d.numeroFactura ?? d.id} facturada en ARCA. CAE ${d.cae ?? '—'} (comprobante N° ${d.numeroArca ?? '—'}).`, 'exito');
        } else if (d) {
            mostrarMensaje(`ARCA no facturó la factura ${d.numeroFactura ?? d.id}; sigue como emitida: ${d.mensaje ?? 'rechazada'}`, 'error');
        } else {
            mostrarMensaje(`La factura ${factura.numeroFactura ?? factura.id} ya no estaba emitida: no se facturó.`, 'error');
        }
        await cargarFacturas();   // redibuja la tabla (la fila pasa a FACTURADA o queda como estaba)
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
        botonUsado.disabled = false;
        botonUsado.textContent = 'Facturar';
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
        actualizarVisorCliente();
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
            btnImprimir.hidden = false;   // la factura con CAE se puede imprimir / guardar en PDF
        } else if (completa.estado === 'ANULADA') {
            // Anulada (en ARCA con nota de credito, o una emitida anulada): solo se muestra
            tituloFormulario.textContent = `Factura ${completa.numeroFactura} (anulada)`;
            const motivo = completa.observaciones ? ` Motivo: ${completa.observaciones}.` : '';
            descripcionFormulario.textContent = completa.caeNotaCredito
                ? `Facturada como Factura C N° ${completa.numeroArca ?? '—'} (CAE ${completa.cae ?? '—'}) y anulada en ARCA `
                  + `con la Nota de Crédito C N° ${completa.numeroNotaCredito ?? '—'} (CAE ${completa.caeNotaCredito}).${motivo}`
                : `Factura anulada.${motivo}`;
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
            btnImprimir.hidden = false;   // imprime lo guardado (sin CAE); los cambios sin guardar no salen
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
        if (campo !== btnCancelar && campo !== btnImprimir) (campo as HTMLInputElement).disabled = true;
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
/** Cierra el comprobante sin guardar y vuelve al ultimo listado (misma vista, filtros y pagina). */
function cerrarFormulario(): void {
    mostrarVista(vistaActual, true);
}

function limpiarFormulario(): void {
    formFactura.reset();   // vuelve cada campo a su valor inicial (en los desplegables, la primera opcion)
    facturaEditando = null;
    limpiarResaltados();   // un comprobante nuevo arranca sin campos en rojo
    items = [];
    // Deshace el modo solo lectura (factura facturada)
    soloLectura = false;
    for (const campo of Array.from(formFactura.elements)) {
        (campo as HTMLInputElement).disabled = false;
    }
    filaAgregarItem.hidden = false;
    btnEmitir.hidden = false;
    btnImprimir.hidden = true;
    btnCancelar.textContent = 'Cancelar';
    // Botones y campos como en el alta (la edicion de una emitida los cambia)
    selectPuntoVenta.disabled = false;
    btnGuardarBorrador.hidden = false;
    btnGuardarBorrador.textContent = 'Guardar borrador';
    btnEmitir.textContent = 'Emitir factura';
    numeroComprobante.textContent = 'El número se asigna al emitir';
    numeroComprobante.classList.add('pendiente');
    llenarArticulos();
    actualizarLetra();    actualizarVisorCliente();
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
        llenarSelect(selectExpArticuloRubro, catalogos.rubros, 'Todos');
        llenarSelect(selectExpArticuloMarca, catalogos.marcas, 'Todas');
        llenarArticulos();
        actualizarLetra();
        actualizarVisorCliente();
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
    actualizarListaItem();
    // Los renglones ya agregados conservan su propia lista: cambiar la lista por defecto no los modifica
    dibujarItems();
}

/** Muestra el precio unitario del articulo elegido en el renglon de carga (sale de la lista de precios). */
function actualizarPrecioItem(): void {
    const articulo = articuloDeLista(numeroOpcional(selectItemArticulo));
    celdaItemPrecio.textContent = articulo ? formatoMoneda.format(articulo.precio) : '—';
    ponerTextoVisor(btnArticuloVisor, articulo?.nombre, 'Elegí un artículo');   // lo que se ve del articulo elegido
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

/** Articulo de la lista de precios por defecto (la del encabezado) o undefined si no esta. */
function articuloDeLista(articuloId: number | undefined) {
    return precioEnLista(articuloId, Number(selectListaPrecio.value));
}

/** Articulo (nombre y precio) en una lista de precios dada, o undefined si no tiene precio ahi. */
function precioEnLista(articuloId: number | undefined, listaId: number) {
    return catalogos?.articulos.find((a) => a.articuloId === articuloId && a.listaPrecioId === listaId);
}

/**
 * Mini selector de la lista de precios de un renglon ya agregado. Solo ofrece las listas donde
 * ese articulo tiene precio. Al cambiarla, se recalculan el precio y el subtotal de ESE renglon.
 * En solo lectura muestra el nombre de la lista como texto.
 */
function selectorListaRenglon(item: FacturaDetalleRequestDTO, listaId: number): HTMLElement | string {
    const nombreLista = (id: number) => catalogos?.listasPrecio.find((l) => l.id === id)?.nombre ?? `Lista ${id}`;
    if (soloLectura) return nombreLista(listaId);

    const listasConPrecio = (catalogos?.articulos ?? [])
        .filter((a) => a.articuloId === item.articuloId)
        .map((a) => a.listaPrecioId);
    const select = document.createElement('select');
    select.className = 'lista-renglon';
    select.setAttribute('aria-label', 'Lista de precios del renglón');
    for (const id of listasConPrecio) {
        select.append(new Option(nombreLista(id), String(id)));
    }
    if (!listasConPrecio.includes(listaId)) {
        select.append(new Option(`${nombreLista(listaId)} (sin precio)`, String(listaId)));
    }
    select.value = String(listaId);
    select.addEventListener('change', () => {
        item.listaPrecioId = Number(select.value);
        dibujarItems();   // precio, subtotal y totales con la nueva lista
    });
    return select;
}

/** En la fila de carga: con que lista se va a agregar el proximo renglon. */
function actualizarListaItem(): void {
    celdaItemLista.textContent = selectListaPrecio.selectedOptions[0]?.textContent ?? '—';
}

// --- Comprobante impreso (Imprimir / Guardar PDF) ---

/** "0001-00000012": punto de venta (4 digitos) y numero (8 digitos), como en los comprobantes de ARCA. */
function numeroArcaFormateado(puntoVenta: number, numero: number): string {
    return `${String(puntoVenta).padStart(4, '0')}-${String(numero).padStart(8, '0')}`;
}

/** Crea un elemento con texto (atajo para armar la hoja de impresion sin innerHTML). */
function crear<K extends keyof HTMLElementTagNameMap>(tag: K, texto?: string, clase?: string): HTMLElementTagNameMap[K] {
    const el = document.createElement(tag);
    if (texto !== undefined) el.textContent = texto;
    if (clase) el.className = clase;
    return el;
}

/** Parrafo "Etiqueta: valor" con la etiqueta en negrita (sinCortar: el valor no se parte en dos lineas). */
function dato(etiqueta: string, valor: string, sinCortar = false): HTMLParagraphElement {
    const p = crear('p');
    p.append(crear('strong', `${etiqueta}: `), sinCortar ? crear('span', valor, 'sin-cortar') : valor);
    return p;
}

/**
 * Trae los datos del comprobante (GET /{id}/impresion), arma la hoja A4 en #impresion y abre el
 * dialogo de impresion del navegador, donde se elige la impresora o "Guardar como PDF".
 * El nombre sugerido del PDF sale del titulo de la pagina (Factura-C-0001-00000012).
 */
async function imprimirFactura(id: number, botonUsado: HTMLButtonElement): Promise<void> {
    botonUsado.disabled = true;   // evita pedidos repetidos mientras llega la respuesta
    try {
        const c = await service.obtenerImpresion(id);
        // Facturada: punto de venta y numero de ARCA. Emitida (sin CAE): punto de venta y numero del sistema.
        const conCae = c.estado === 'FACTURADA' && c.cae !== null;
        const numero = conCae && c.puntoVentaArca !== null && c.numeroArca !== null
            ? numeroArcaFormateado(c.puntoVentaArca, c.numeroArca)
            : numeroArcaFormateado(c.puntoVentaNumero, c.numeroInterno ?? 0);
        const fecha = (iso: string) => new Date(iso.length === 10 ? `${iso}T00:00:00` : iso).toLocaleDateString('es-AR');

        // Cabecera: emisor | letra | comprobante
        const emisor = crear('div');
        emisor.append(crear('h1', c.emisorRazonSocial), dato('Domicilio comercial', c.emisorDomicilio ?? '—'),
            dato('Condición frente al IVA', c.emisorCondicionIva), dato('Punto de venta', c.puntoVentaDescripcion));
        const letra = crear('div', undefined, 'imp-letra');
        letra.append(crear('b', c.letra), crear('small', `COD. ${c.codigoComprobante}`));
        const comprobante = crear('div');
        comprobante.append(crear('h2', c.tipoComprobante), dato('Punto de venta y número', numero, true),
            dato('Fecha de emisión', fecha(c.fechaFacturacion ?? c.fechaEmision)),
            dato('CUIT', c.emisorCuit ? String(c.emisorCuit) : '—'),   // 0 = ARCA no configurado en esta PC
            dato('N° interno', c.numeroInterno !== null ? String(c.numeroInterno) : '—'));
        const cabecera = crear('div', undefined, 'imp-cabecera');
        cabecera.append(emisor, letra, comprobante);

        // Receptor
        const receptor = crear('div', undefined, 'imp-caja');
        receptor.append(dato('Cliente', c.clienteDenominacion), dato('CUIT/CUIL', c.clienteCuit ?? '—'),
            dato('Condición frente al IVA', c.condicionIva), dato('Domicilio', c.clienteDomicilio ?? '—'),
            dato('Moneda', c.moneda), dato('Observaciones', c.observaciones || '—'));

        // Renglones (con los importes guardados en la factura)
        const tabla = crear('table');
        const encabezado = crear('tr');
        for (const [titulo, num] of [['Código', false], ['Descripción', false], ['Cantidad', true],
            ['Precio unit.', true], ['% Bonif.', true], ['Subtotal', true]] as [string, boolean][]) {
            encabezado.append(crear('th', titulo, num ? 'num' : undefined));
        }
        const thead = crear('thead');
        thead.append(encabezado);
        const tbody = crear('tbody');
        for (const r of c.renglones) {
            const fila = crear('tr');
            fila.append(crear('td', r.codigo), crear('td', r.descripcion), crear('td', String(r.cantidad), 'num'),
                crear('td', formatoMoneda.format(r.precioUnitario), 'num'), crear('td', String(r.porcentajeBonificacion), 'num'),
                crear('td', formatoMoneda.format(r.subtotal), 'num'));
            tbody.append(fila);
        }
        tabla.append(thead, tbody);

        // Totales (Factura C: sin IVA discriminado)
        const totales = crear('div', undefined, 'imp-totales');
        const subtotal = crear('p');
        subtotal.append(crear('span', 'Subtotal'), crear('span', formatoMoneda.format(c.importeTotal)));
        const total = crear('p', undefined, 'total');
        total.append(crear('span', 'Importe total'), crear('span', formatoMoneda.format(c.importeTotal)));
        totales.append(subtotal, total);

        // Autorizacion de ARCA: CAE y su vencimiento (una emitida todavia no lo tiene)
        const cae = crear('div', undefined, conCae ? 'imp-cae' : 'imp-cae sin-cae');
        const datosCae = crear('div');
        if (conCae) {
            datosCae.append(dato('CAE N°', c.cae ?? '—'),
                dato('Fecha de vencimiento del CAE', c.caeVencimiento ? fecha(c.caeVencimiento) : '—'));
            cae.append(datosCae, crear('p',
                'Comprobante autorizado por ARCA en el entorno de homologación (pruebas): sin validez fiscal.', 'aviso'));
        } else {
            datosCae.append(dato('CAE N°', 'Sin CAE'), dato('Estado', 'Emitida, pendiente de facturar en ARCA'));
            cae.append(datosCae, crear('p',
                'Comprobante NO VÁLIDO COMO FACTURA: todavía no fue autorizado por ARCA.', 'aviso'));
        }

        seccionImpresion.replaceChildren(cabecera, receptor, tabla, totales, cae);

        // El titulo de la pagina es el nombre que el navegador sugiere para el PDF
        const tituloAnterior = document.title;
        document.title = conCae ? `Factura-${c.letra}-${numero}` : `Factura-${c.letra}-${numero}-SIN-CAE`;
        window.addEventListener('afterprint', () => (document.title = tituloAnterior), { once: true });
        window.print();
    } catch (error) {
        mostrarMensaje(textoDeError(error), 'error');
    } finally {
        botonUsado.disabled = false;
    }
}

// --- Exploradores de clientes y articulos (ventanas emergentes) ---

/** Muestra en el visor el cliente elegido en el select oculto (vacio = Consumidor Final). */
function actualizarVisorCliente(): void {
    ponerTextoVisor(btnClienteVisor,
        selectCliente.value ? (selectCliente.selectedOptions[0]?.textContent ?? '') : undefined, 'Consumidor Final');
}

/** Texto del campo-boton de un explorador; sin valor muestra el texto por defecto en gris (clase "vacio"). */
function ponerTextoVisor(visor: HTMLButtonElement, texto: string | undefined, porDefecto: string): void {
    const span = visor.querySelector('.texto-visor');
    if (span) span.textContent = texto || porDefecto;
    visor.classList.toggle('vacio', !texto);
}

/** Elige un valor en un select oculto y avisa a quien escucha su "change" (precio, visor, etc.). */
function elegirEnSelect(select: HTMLSelectElement, valor: string): void {
    select.value = valor;
    select.dispatchEvent(new Event('change'));
}

/** Espera a que el usuario deje de escribir antes de buscar (evita un GET por tecla). */
function conDemora(accion: () => void, ms = 250): () => void {
    let temporizador: number | undefined;
    return () => {
        window.clearTimeout(temporizador);
        temporizador = window.setTimeout(accion, ms);
    };
}

/** Fila de un explorador: al hacer clic (o Enter con el foco en ella) elige el elemento. */
function filaExplorador(celdas: (string | null)[], alElegir: () => void, numericas: number[] = []): HTMLTableRowElement {
    const fila = document.createElement('tr');
    fila.tabIndex = 0;   // se puede recorrer con Tab y elegir con Enter
    celdas.forEach((texto, i) => fila.append(celda(texto ?? '—', numericas.includes(i) ? 'numero' : '')));
    fila.addEventListener('click', alElegir);
    fila.addEventListener('keydown', (evento: KeyboardEvent) => {
        if (evento.key === 'Enter') {
            evento.preventDefault();
            alElegir();
        }
    });
    return fila;
}

function abrirExploradorClientes(): void {
    inputExpClienteTexto.value = '';
    dialogClientes.showModal();
    inputExpClienteTexto.focus();
    void buscarEnExploradorClientes();
}

/** GET /api/catalogos/clientes con el texto del filtro y dibuja el listado. */
async function buscarEnExploradorClientes(): Promise<void> {
    try {
        const clientes: ClienteExplorerDTO[] = await service.buscarClientes(inputExpClienteTexto.value);
        bodyExpClientes.replaceChildren();
        if (clientes.length === 0) {
            bodyExpClientes.append(filaVacia(4, 'No hay clientes que coincidan con la búsqueda.'));
        }
        for (const c of clientes) {
            bodyExpClientes.append(filaExplorador([c.denominacion, c.cuitCuil, c.email, c.domicilio], () => {
                elegirEnSelect(selectCliente, String(c.id));
                dialogClientes.close();
            }));
        }
        cantidadExpClientes.textContent = `${clientes.length} ${clientes.length === 1 ? 'cliente' : 'clientes'}`;
    } catch (error) {
        bodyExpClientes.replaceChildren(filaVacia(4, textoDeError(error)));
        cantidadExpClientes.textContent = '';
    }
}

function abrirExploradorArticulos(): void {
    if (!selectListaPrecio.value) {
        mostrarMensaje('Elegí una lista de precios antes de buscar artículos.', 'error');
        return;
    }
    inputExpArticuloTexto.value = '';
    selectExpArticuloRubro.value = '';
    selectExpArticuloMarca.value = '';
    textoExpArticulosLista.textContent =
        `Precios de la ${selectListaPrecio.selectedOptions[0]?.textContent ?? 'lista elegida'}. Hacé clic en un artículo para elegirlo.`;
    dialogArticulos.showModal();
    inputExpArticuloTexto.focus();
    void buscarEnExploradorArticulos();
}

/** GET /api/catalogos/articulos con la lista de precios del comprobante y los filtros del explorador. */
async function buscarEnExploradorArticulos(): Promise<void> {
    try {
        const articulos: ArticuloExplorerDTO[] = await service.buscarArticulos({
            listaPrecioId: Number(selectListaPrecio.value),
            texto: inputExpArticuloTexto.value,
            rubroId: numeroOpcional(selectExpArticuloRubro),
            marcaId: numeroOpcional(selectExpArticuloMarca),
        });
        bodyExpArticulos.replaceChildren();
        if (articulos.length === 0) {
            bodyExpArticulos.append(filaVacia(5, 'No hay artículos que coincidan con los filtros.'));
        }
        for (const a of articulos) {
            bodyExpArticulos.append(filaExplorador(
                [a.codigo, a.denominacion, a.rubro, a.marca, formatoMoneda.format(a.precio)],
                () => {
                    elegirEnSelect(selectItemArticulo, String(a.articuloId));
                    dialogArticulos.close();
                    inputItemCantidad.focus();   // lo siguiente es cargar la cantidad
                },
                [4],
            ));
        }
        cantidadExpArticulos.textContent = `${articulos.length} ${articulos.length === 1 ? 'artículo' : 'artículos'}`;
    } catch (error) {
        bodyExpArticulos.replaceChildren(filaVacia(5, textoDeError(error)));
        cantidadExpArticulos.textContent = '';
    }
}

// --- Renglones ---

/** Valida los campos del renglon y lo agrega al array en memoria. */
function agregarItem(): void {
    const articuloId = numeroOpcional(selectItemArticulo);
    const cantidad = numeroOpcional(inputItemCantidad);
    const bonificacion = numeroOpcional(inputItemBonificacion);

    // Validaciones para ayudar al usuario. El backend vuelve a validar todo,
    // porque al servidor se le puede pegar sin pasar por esta pantalla (ej. Postman).
    // Se juntan todos los problemas del renglon para mostrarlos juntos
    const problemas: Problema[] = [];
    if (articuloId === undefined || articuloId <= 0) {
        problemas.push({ mensaje: 'Elegí un artículo: hacé clic en el campo Artículo para buscarlo.',
            campo: btnArticuloVisor, cambio: selectItemArticulo });
    }
    // badInput: el navegador recibio algo que no es un numero (ej. "2,5,1" o "abc")
    if (inputItemCantidad.validity.badInput) {
        problemas.push({ mensaje: 'La cantidad tiene caracteres que no son un número.', campo: inputItemCantidad });
    } else if (cantidad === undefined || !Number.isInteger(cantidad) || cantidad < 1) {
        problemas.push({ mensaje: 'La cantidad debe ser un número entero mayor a 0.', campo: inputItemCantidad });
    }
    if (inputItemBonificacion.validity.badInput) {
        problemas.push({ mensaje: 'La bonificación tiene caracteres que no son un número.', campo: inputItemBonificacion });
    } else if (bonificacion !== undefined && (bonificacion < 0 || bonificacion > 100)) {
        problemas.push({ mensaje: 'La bonificación debe estar entre 0 y 100.', campo: inputItemBonificacion });
    }
    if (problemas.length > 0 || articuloId === undefined || cantidad === undefined) {
        mostrarErrores(problemas);
        return;
    }

    // El renglon se queda con la lista de precios elegida AHORA: si despues se cambia la lista
    // por defecto del encabezado, este renglon no cambia (se puede cambiar con su mini selector)
    items.push({ articuloId, cantidad, porcentajeBonificacion: bonificacion, listaPrecioId: Number(selectListaPrecio.value) });

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
        itemsBody.append(filaVacia(7, 'Todavía no hay renglones. Elegí un artículo abajo y tocá «Agregar».'));
    }

    items.forEach((item, indice) => {
        // Cada renglon usa SU lista de precios (los viejos sin lista usan la del encabezado)
        const listaId = item.listaPrecioId ?? Number(selectListaPrecio.value);
        const articulo = precioEnLista(item.articuloId, listaId);
        const bonificacion = item.porcentajeBonificacion ?? 0;
        // Mismo calculo que FacturaService.cargarDetalles (el backend lo recalcula al guardar)
        const subtotal = articulo ? redondear((item.cantidad ?? 0) * articulo.precio * (1 - bonificacion / 100)) : 0;
        total += subtotal;

        const fila = document.createElement('tr');
        fila.append(
            celda(articulo ? articulo.nombre : `Artículo ${item.articuloId} (sin precio en esta lista)`),
            celda(selectorListaRenglon(item, listaId)),
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

    // Los botones quedan activos aunque falten datos: al tocarlos, validarComprobante()
    // explica que falta (un boton deshabilitado no le dice nada al usuario). En solo lectura, nunca.
    btnEmitir.disabled = soloLectura;
    btnGuardarBorrador.disabled = soloLectura;
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
    const problemas = validarComprobante();
    if (problemas.length > 0) {
        mostrarErrores(problemas);
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
        // Error del backend: ventana emergente y, si se reconoce, el campo en rojo.
        // Los datos quedan en el comprobante para corregirlos.
        mostrarErrores([problemaDelBackend(textoDeError(error))]);
        btnEmitir.disabled = false;
        btnGuardarBorrador.disabled = false;
    }
}

// =====================================================================
// PARTE 5: EVENTOS Y ARRANQUE
// =====================================================================

// Barra lateral
btnNuevaFactura.addEventListener('click', abrirFormularioNuevo);

// Ventana de error del comprobante
btnDialogoError.addEventListener('click', () => dialogoError.close());
dialogoError.addEventListener('close', alCerrarDialogoError);

// Exploradores: se abren haciendo clic en el campo-boton (Cliente / Articulo)
btnClienteVisor.addEventListener('click', abrirExploradorClientes);
btnQuitarCliente.addEventListener('click', () => elegirEnSelect(selectCliente, ''));
selectCliente.addEventListener('change', actualizarVisorCliente);
btnExpConsumidorFinal.addEventListener('click', () => {
    elegirEnSelect(selectCliente, '');
    dialogClientes.close();
});
btnArticuloVisor.addEventListener('click', abrirExploradorArticulos);
// Filtros internos: buscan mientras se escribe (con una pequena demora) o al cambiar un desplegable
inputExpClienteTexto.addEventListener('input', conDemora(() => void buscarEnExploradorClientes()));
inputExpArticuloTexto.addEventListener('input', conDemora(() => void buscarEnExploradorArticulos()));
selectExpArticuloRubro.addEventListener('change', () => void buscarEnExploradorArticulos());
selectExpArticuloMarca.addEventListener('change', () => void buscarEnExploradorArticulos());
// Enter en un filtro no debe enviar nada: busca
for (const form of [formFiltrosClientes, formFiltrosArticulos]) {
    form.addEventListener('submit', (evento) => evento.preventDefault());
}
// Boton "Cerrar" de cada explorador (Escape tambien los cierra, lo hace el navegador)
for (const dialogo of [dialogClientes, dialogArticulos]) {
    dialogo.querySelector('[data-cerrar]')?.addEventListener('click', () => dialogo.close());
    // Clic afuera de la ventana (sobre el fondo oscuro) tambien la cierra. Con showModal(), un clic
    // en el fondo llega al propio <dialog>: se cierra si el punto cae fuera de su rectangulo.
    dialogo.addEventListener('click', (evento: MouseEvent) => {
        const caja = dialogo.getBoundingClientRect();
        const afuera = evento.clientX < caja.left || evento.clientX > caja.right
            || evento.clientY < caja.top || evento.clientY > caja.bottom;
        if (afuera) dialogo.close();
    });
}
// "Facturar emitidas en ARCA" abre/cierra el desplegable con las dos opciones
btnFacturarEmitidas.addEventListener('click', () => {
    if (opcionesFacturar.hidden) abrirMenuFacturar();
    else cerrarMenuFacturar();
});
btnFacturarTodas.addEventListener('click', () => void facturarEmitidas(false));
btnFacturarPagina.addEventListener('click', () => void facturarEmitidas(true));
// Clic fuera del desplegable o Escape: se cierra
document.addEventListener('click', (evento: MouseEvent) => {
    if (!opcionesFacturar.hidden && !menuFacturar.contains(evento.target as Node)) cerrarMenuFacturar();
});
document.addEventListener('keydown', (evento: KeyboardEvent) => {
    if (evento.key === 'Escape' && !opcionesFacturar.hidden) {
        cerrarMenuFacturar();
        btnFacturarEmitidas.focus();
    }
});
for (const b of botonesMenu) {
    b.addEventListener('click', () => mostrarVista(b.dataset.vista as Vista));
}

// Filtros
formFiltros.addEventListener('submit', (evento) => {
    evento.preventDefault();   // sin esto el navegador recargaria la pagina al enviar el form
    // Buscar sin ningun filtro no cambia nada: se avisa con un texto chico debajo de los filtros.
    // (El estado solo cuenta en el historico: en las otras vistas lo pone la vista misma.)
    const f = leerFiltros();
    const hayFiltros = f.fechaDesde !== undefined || f.fechaHasta !== undefined
        || f.montoMinimo !== undefined || f.numero !== undefined || f.cliente !== undefined
        || (vistaActual === 'HISTORICO' && f.estado !== undefined)
        || filtroNumero.validity.badInput || filtroMontoMinimo.validity.badInput;
    avisoFiltros.hidden = hayFiltros;
    if (!hayFiltros) return;
    paginaActual = 0;          // con otros filtros, se vuelve a la primera pagina
    void cargarFacturas();
});
// El aviso se va apenas el usuario empieza a completar un filtro
formFiltros.addEventListener('input', () => (avisoFiltros.hidden = true));
formFiltros.addEventListener('change', () => (avisoFiltros.hidden = true));
btnLimpiarFiltros.addEventListener('click', () => {
    avisoFiltros.hidden = true;
    formFiltros.reset();
    paginaActual = 0;
    void cargarFacturas();
});

// Paginacion
btnPaginaAnterior.addEventListener('click', () => {
    paginaActual--;
    void cargarFacturas();
});
btnPaginaSiguiente.addEventListener('click', () => {
    paginaActual++;
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
const camposRenglon: HTMLElement[] = [inputItemCantidad, inputItemBonificacion];
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
btnCerrarFormulario.addEventListener('click', cerrarFormulario);
btnImprimir.addEventListener('click', () => {
    if (facturaEditando) void imprimirFactura(facturaEditando.id, btnImprimir);
});

// Al abrir la pagina: historico visible, comprobante oculto (atributo hidden en el HTML)
void cargarCatalogos();
mostrarVista('HISTORICO');
