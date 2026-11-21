# Registro de cambios

Cada entrada indica qué se cambió, en qué archivos buscarlo y cómo probarlo.
Rutas del back relativas a `Tp_Grupal_JPA/src/main/java/com/tup/programacion3/`.
Rutas del front relativas a `Tp_Grupal_Frontend/`.

---

## 1. Filtros por número de factura y por cliente

**Qué hace:** en el listado de facturas (todas las vistas) hay dos filtros nuevos:
- **N° factura**: busca el número exacto (los borradores no tienen número, así que nunca coinciden).
- **Cliente**: busca el texto dentro de la razón social o del CUIT/CUIL, sin distinguir mayúsculas
  (ej. "trebol" o "30-7098"). Las facturas sin cliente se encuentran buscando "consumidor final".

Funcionan combinados con los filtros que ya existían y con la paginación.

**Back**
| Archivo | Cambio |
|---|---|
| `Repository/FacturaReporteRepository.java` | `construirWhere()` recibe `numero` y `cliente`. El cliente se busca con `LOWER(...) LIKE` sobre `c.denominacion` y `c.cuitCuil`. La consulta de totales de la paginación ahora hace `LEFT JOIN f.cliente c` para poder filtrar por cliente. |
| `Service/FacturaService.java` | `buscarFacturasFiltradas()` y `buscarPaginaFiltrada()` reciben y pasan `numero` y `cliente`. |
| `Controller/FacturaRestController.java` | `GET /api/facturas` y `GET /api/facturas/pagina` aceptan `?numero=` y `?cliente=`. |
| `DataLoader.java` | Las dos llamadas a `buscarFacturasFiltradas` pasan los parámetros nuevos (en `null`). |

**Front**
| Archivo | Cambio |
|---|---|
| `index.html` | Campos `#filtro-numero` y `#filtro-cliente` al principio del formulario de filtros. |
| `src/models/factura.dto.ts` | `FacturaFiltrosDTO` suma `numero` y `cliente`. |
| `src/services/FacturaService.ts` | `parametrosDeFiltro()` agrega `numero` y `cliente` a la URL. |
| `src/app.ts` | `leerFiltros()` lee los dos campos nuevos. |

**Probar:** `GET http://localhost:8080/api/facturas/pagina?cliente=trebol` o, en el front,
escribir "trebol" en Cliente y tocar Buscar.

### 1.1 CUIT del cliente en el listado

**Qué hace:** en la columna Cliente del listado, debajo de la razón social, se ve
"CUIT 30-70987654-2 · condición IVA" (las de Consumidor Final siguen mostrando solo la condición IVA).
Sirve para probar el filtro de cliente por CUIT.

| Archivo | Cambio |
|---|---|
| `DTOs/FacturaReporteDTO.java` (back) | Campo nuevo `clienteCuit` (al final). |
| `Repository/FacturaReporteRepository.java` (back) | El `SELECT new` y el `GROUP BY` suman `c.cuitCuil`. |
| `Service/FacturaService.java` (back) | `aReporteDTO()` pasa el CUIT del cliente. |
| `src/models/factura.dto.ts` (front) | `FacturaReporteDTO.clienteCuit`. |
| `src/app.ts` (front) | `clienteConIva()` muestra el CUIT. |

---

## 2. Explorador de clientes y de artículos (ventanas emergentes)

**Qué hace:** en el comprobante, Cliente y Artículo ya no son desplegables. Cada uno tiene un
visor de solo lectura y un botón **Buscar…** (o clic en el visor) que abre una ventana emergente
con un listado y filtros propios. Al hacer clic en una fila (o Enter sobre ella) queda elegido.
- **Clientes:** filtro por texto (razón social o CUIT), columnas razón social, CUIT, email y
  domicilio. Botón "Usar Consumidor Final" en la ventana y ✕ al lado del visor para quitar el cliente.
- **Artículos:** muestra los artículos con precio en la lista de precios elegida en el comprobante.
  Filtros por texto (código o descripción), rubro y marca; columnas código, artículo, rubro, marca y precio.
  Al elegir uno, el foco pasa a Cantidad.

Los filtros buscan mientras se escribe (con 250 ms de demora para no hacer un GET por tecla) y en el
backend. Escape o "Cerrar" cierran la ventana sin elegir.

**Cómo está hecho:** los `<select id="cliente-id">` y `<select id="item-articulo-id">` siguen
existiendo (ocultos) y guardan el ID elegido, así el resto del código (guardar, editar, precio del
renglón) no cambió. El explorador solo les asigna el valor y dispara su evento `change`.

**Back**
| Archivo | Cambio |
|---|---|
| `Controller/CatalogoRestController.java` | `GET /api/catalogos/clientes?texto=` y `GET /api/catalogos/articulos?listaPrecioId=&texto=&rubroId=&marcaId=`. |
| `Service/CatalogoService.java` | `buscarClientes()` y `buscarArticulos()`; `obtenerCatalogos()` suma rubros y marcas (para los filtros). |
| `Repository/ClienteRepository.java` | `buscarPorTexto()` (JPQL con `LIKE` en razón social y CUIT). |
| `Repository/ListaPrecioArticuloRepository.java` | `buscarParaExplorador()` (lista + texto + rubro + marca; `0` = cualquiera, para no pasar `null` a PostgreSQL). |
| `Repository/RubroRepository.java`, `Repository/MarcaRepository.java` | Nuevos. |
| `DTOs/ClienteExplorerDTO.java`, `DTOs/ArticuloExplorerDTO.java` | Nuevos: filas de cada explorador. |
| `DTOs/CatalogosDTO.java` | Suma `rubros` y `marcas`. |

**Front**
| Archivo | Cambio |
|---|---|
| `index.html` | Visores + botones en Cliente y en la fila de carga de artículos; dos `<dialog>` al final del `<body>` (`#explorador-clientes`, `#explorador-articulos`); estilos `.selector-explorer` y `dialog.explorador`. |
| `src/app.ts` | Sección "Exploradores de clientes y articulos": `abrirExploradorClientes/Articulos()`, `buscarEnExplorador...()`, `filaExplorador()`, `conDemora()`, `actualizarVisorCliente()`; el visor del artículo se actualiza en `actualizarPrecioItem()`. |
| `src/services/FacturaService.ts` | `buscarClientes()` y `buscarArticulos()`. |
| `src/models/factura.dto.ts` | `ClienteExplorerDTO`, `ArticuloExplorerDTO`, `FiltrosArticuloExplorer`; `CatalogosDTO` suma `rubros` y `marcas`. |

**Ajuste de diseño:** Cliente y Condición IVA ocupan todo el ancho (clase `ancho`, igual que
Observaciones). En la fila de carga, la columna Artículo toma el 40 % de la tabla y el visor
alinea el texto a la izquierda (`index.html`, estilos `.renglones th:first-child` y `#item-articulo-visor`).

**Ajuste de uso:** los exploradores se abren **solo** con el botón "Buscar…" (antes también al hacer
clic en el visor). Los visores de Cliente y Artículo quedan siempre en gris, sin cursor ni foco
(`pointer-events: none`, `tabindex="-1"`): solo muestran lo elegido. Los botones "Buscar…" y "✕"
pasaron a tamaño normal (sin la clase `chico`). Archivos: `index.html` (estilo `.selector-explorer input`
y los botones) y `src/app.ts` (se quitaron los `click` de los visores).

**Probar:** Nueva factura → Cliente → Buscar… → escribir "30-7" (filtra por CUIT). En la fila de
artículos → Buscar… → elegir Rubro "Limpieza." → clic en un artículo → cargar cantidad → Agregar.

---

## 3. Errores más visibles en el comprobante

**Qué hace:** mientras se crea o edita una factura, cualquier error aparece en una **ventana
emergente** ("Revisá el comprobante") en lugar del cartel de arriba de la página, que no se veía
si el usuario estaba más abajo.
- **Validación previa al guardar** (`validarComprobante()`): revisa punto de venta, moneda, lista de
  precios, condición IVA y que haya renglones, y muestra **todos** los problemas juntos en una lista.
  También avisa si quedó un artículo o una cantidad cargados en la fila de carga **sin tocar "Agregar"**.
- **Validación del renglón** (`agregarItem()`): artículo sin elegir, cantidad o bonificación con
  caracteres que no son números (`validity.badInput`), cantidad no entera o menor a 1, bonificación
  fuera de 0–100. También junta todos los problemas.
- **Campos en rojo:** cada campo con problema queda con borde y fondo rojos (clase `.invalido`) hasta
  que el usuario lo modifica. Al cerrar la ventana (botón "Entendido" o Escape), la página baja hasta
  el primer campo con problema y le pone el foco.
- **Errores del backend** al guardar: se muestran en la misma ventana y, si el mensaje menciona un
  campo (lista de precios, punto de venta, cliente, cantidad, etc.), ese campo queda en rojo
  (`problemaDelBackend()`).
- Los botones "Emitir factura" y "Guardar borrador" ya no se deshabilitan cuando no hay renglones:
  un botón gris no explicaba nada; ahora al tocarlo la ventana dice qué falta.
- El formulario tiene `novalidate`: se reemplazan los globitos nativos del navegador por esta ventana.
- **Fuera del comprobante** (listado), los mensajes siguen en el cartel de arriba, pero ahora la
  página sube hasta él (`scrollTo` en `mostrarMensaje()`).

**Front** (no hay cambios en el back)
| Archivo | Cambio |
|---|---|
| `index.html` | `<dialog id="dialogo-error" class="alerta">` al final del `<body>`; estilos `.invalido` y `dialog.alerta`; `novalidate` en `#form-factura`. |
| `src/app.ts` | Sección "Errores del comprobante": interfaz `Problema`, `mostrarErrores()`, `resaltar()`, `limpiarResaltados()`, `alCerrarDialogoError()`, `problemaDelBackend()`, `validarComprobante()`. Cambios en `mostrarMensaje()`, `agregarItem()`, `guardarFactura()`, `dibujarItems()` y `limpiarFormulario()`. |

**Probar:** Nueva factura → tocar "Emitir factura" sin renglones (aparece la ventana con la lista y
los campos en rojo). En la fila de carga, elegir un artículo, poner cantidad 0 y tocar "Agregar".

### 3.1 Aviso al buscar sin filtros

**Qué hace:** si en el listado se toca "Buscar" sin ningún filtro cargado, no se consulta al backend
y aparece un texto chico en rojo debajo de los filtros: *"No hay filtros cargados: completá al menos
uno para buscar."* (no es una ventana emergente). El aviso desaparece apenas se escribe o elige algo
en cualquier filtro, al tocar "Limpiar" o al cambiar de vista. El filtro Estado solo cuenta en el
Histórico (en las otras vistas el estado lo pone la vista).

| Archivo | Cambio |
|---|---|
| `index.html` (front) | `<p id="aviso-filtros" class="aviso-filtros">` debajo de `#form-filtros`; estilo `.aviso-filtros`. |
| `src/app.ts` (front) | El `submit` de `formFiltros` revisa si hay algún filtro; listeners `input`/`change` que ocultan el aviso; `mostrarVista()` y "Limpiar" también lo ocultan. |

### 3.2 Filtros del listado en grilla

**Qué hace:** los filtros se ordenan en una grilla de 3 columnas × 2 filas, con los botones en una
4ta columna: fila 1 = N° factura, Cliente, Monto mínimo y **Buscar**; fila 2 = Fecha desde,
Fecha hasta, Estado y **Limpiar**. Todos los campos tienen la misma altura (38 px), así quedan
alineados. Fuera del Histórico el filtro Estado no se muestra y su celda queda vacía.
En pantallas de menos de 900 px pasa a 2 columnas con los botones abajo.

| Archivo | Cambio |
|---|---|
| `index.html` (front) | Estilo `.filtros` con `display: grid`; `.acciones-filtro` con `display: contents` para ubicar cada botón en la columna 4; "Monto mínimo" se movió a la primera fila. No cambió `app.ts`. |

---

## 4. Artículos de distintas listas de precios en la misma factura

**Qué hace:**
- La lista de precios del encabezado pasa a ser la **lista por defecto** ("Lista de precios por defecto"):
  se aplica a cada artículo que se **agrega**. Si se cambia, los renglones ya cargados **no cambian**.
- Cada renglón guarda **su propia lista**. En la tabla hay una columna nueva **Lista** con un
  **mini selector** por renglón, que solo ofrece las listas donde ese artículo tiene precio. Al
  cambiarlo se recalculan el precio, el subtotal y los totales de ese renglón.
- En la fila de carga, la columna Lista muestra con qué lista se va a agregar el próximo renglón.
- En una factura de solo lectura (facturada/anulada) la columna muestra el nombre de la lista, sin selector.
- Al editar una factura guardada, cada renglón vuelve con su lista.

**Back**
| Archivo | Cambio |
|---|---|
| `DTOs/FacturaDetalleRequestDTO.java` | Campo nuevo `listaPrecioId` (opcional). Constructor de 3 parámetros que lo deja en `null` (lo usa el `DataLoader`). |
| `Service/FacturaService.java` | `cargarDetalles()`: cada renglón usa su `listaPrecioId`; si no viene, la de la factura (`listaPrecioPorDefecto`). `obtener()`: cada renglón devuelve su `listaPrecioId`. |

El modelo no cambió: `FacturaVentaDetalle` ya apuntaba a un `ListaPrecioArticulo` (artículo + lista),
así que cada renglón siempre pudo tener su lista. La API sigue aceptando requests sin `listaPrecioId`
en los renglones (usan la de la factura).

**Front**
| Archivo | Cambio |
|---|---|
| `src/models/factura.dto.ts` | `FacturaDetalleRequestDTO.listaPrecioId`. |
| `src/app.ts` | `agregarItem()` guarda la lista elegida en el renglón; `dibujarItems()` calcula cada renglón con su lista (`precioEnLista()`) y dibuja `selectorListaRenglon()`; `actualizarListaItem()` muestra la lista en la fila de carga. |
| `index.html` | Columna "Lista" (encabezado, celda `#item-lista` en la fila de carga); el label del encabezado dice "Lista de precios por defecto"; estilos `.lista-renglon`; anchos de la tabla ajustados para que entre la columna nueva (el visor del artículo usa `flex: 1 1 0; width: 0`, Cantidad/Bonif. 84 px). |

**Probar:** Nueva factura → agregar un artículo con la Lista Mayorista → cambiar la lista por defecto
a Minorista (el primer renglón no cambia) → agregar otro artículo → en el primer renglón, cambiar su
lista con el mini selector y ver cómo cambia el precio.

### 4.1 Exploradores sin botón "Buscar…"

**Qué hace:**
- Se quitaron los botones "Buscar…". El campo de **Cliente** y el de **Artículo** (fila de carga) ahora
  **son el botón**: se ven como un campo con una lupa, muestran lo elegido (o "Consumidor Final" /
  "Elegí un artículo" en gris) y **al hacer clic abren el explorador**. Se recorren con Tab y se
  abren con Enter. En solo lectura quedan deshabilitados (gris).
- **Cliente** (con su ✕) y **Condición IVA** vuelven a estar en la misma fila (2/3 y 1/3 del ancho);
  Observaciones sigue abajo a todo el ancho.
- En la fila de carga del detalle ya no hay celda vacía bajo "Subtotal": el botón **Agregar** ocupa
  esa columna y la de acciones (`colspan="2"`). Cantidad y Bonif. vuelven a 96 px.

Esto reemplaza lo de la sección 2 sobre "visor de solo lectura + botón Buscar…".

| Archivo | Cambio |
|---|---|
| `index.html` (front) | `#cliente-visor` e `#item-articulo-visor` pasan a ser `<button class="campo-explorer">` con `<span class="texto-visor">` y una lupa SVG; se borraron `#btn-buscar-cliente` y `#btn-buscar-articulo`; clase `grilla-cliente` (2fr / 1fr); estilos `.campo-explorer`; celda Agregar con `colspan="2"`. |
| `src/app.ts` (front) | `btnClienteVisor` / `btnArticuloVisor` abren los exploradores; función nueva `ponerTextoVisor()`; los errores del comprobante marcan y enfocan esos campos-botón. |

### 4.2 Cerrar los exploradores con clic afuera

**Qué hace:** un clic sobre el fondo oscuro, fuera de la ventana del explorador de clientes o de
artículos, la cierra sin elegir nada (igual que "Cerrar" o Escape). Un clic dentro de la ventana no la cierra.

| Archivo | Cambio |
|---|---|
| `src/app.ts` (front) | Listener `click` en cada `<dialog>` de explorador: si el punto cae fuera de su `getBoundingClientRect()`, `dialogo.close()`. |

---

## 5. Imprimir / guardar en PDF la factura facturada en ARCA

**Qué hace:** al abrir con "Ver" una factura **FACTURADA**, junto a "Volver" aparece el botón
**Imprimir / Guardar PDF**. Arma una hoja A4 con el formato de un comprobante de ARCA y abre el
diálogo de impresión del navegador, donde se elige la impresora o **"Guardar como PDF"**. El nombre
sugerido del archivo es `Factura-C-0001-00000012` (letra, punto de venta y número de ARCA).

La hoja tiene:
- **Cabecera:** emisor (razón social, domicilio comercial del punto de venta, "Responsable Monotributo"),
  recuadro con la letra **C** y "COD. 011", y a la derecha FACTURA, punto de venta y número de ARCA
  (`0001-00000012`), fecha de emisión (la de facturación en ARCA), CUIT del emisor y N° interno.
- **Receptor:** cliente, CUIT/CUIL, condición IVA, domicilio, moneda y observaciones.
- **Detalle:** código, descripción, cantidad, precio unitario, % bonificación y subtotal, con los
  importes **guardados en la factura** (no los precios actuales de las listas).
- **Totales:** subtotal e importe total (Factura C: sin IVA discriminado).
- **Autorización:** **CAE N°** y **fecha de vencimiento del CAE**, y la aclaración de que es del
  entorno de homologación, sin validez fiscal.

Se imprime como **Factura C** porque así se informa a ARCA (ver `WsfeService`), aunque en el sistema
la factura figure como A o B. No incluye el código QR que exige ARCA en los comprobantes reales.

**Back**
| Archivo | Cambio |
|---|---|
| `DTOs/ComprobanteImpresionDTO.java` | Nuevo: datos del comprobante impreso (record con `Renglon` adentro). |
| `Service/ComprobanteImpresionService.java` | Nuevo: arma el DTO; 409 si la factura no está FACTURADA o no tiene CAE. Emisor: razón social y condición IVA fijas en constantes, CUIT de `ArcaProperties`. |
| `Controller/FacturaRestController.java` | `GET /api/facturas/{id}/impresion`. |

**Front**
| Archivo | Cambio |
|---|---|
| `index.html` | Botón `#btn-imprimir` (oculto salvo en "Ver" de una facturada); `<section id="impresion">` al final del `<body>`; estilos de la hoja y `@media print` (al imprimir se oculta todo menos `#impresion`). |
| `src/app.ts` | `imprimirFactura()` (trae los datos, arma la hoja con `crear()`/`dato()`, cambia el título de la página para el nombre del PDF y llama a `window.print()`); `numeroArcaFormateado()`; el botón se muestra en `abrirFormularioEdicion()` para FACTURADA y se oculta en `limpiarFormulario()`; `activarSoloLectura()` no lo deshabilita. |
| `src/services/FacturaService.ts` | `obtenerImpresion()`. |
| `src/models/factura.dto.ts` | `ComprobanteImpresionDTO`. |

**Probar:** facturar las emitidas en ARCA → vista Facturadas → "Ver" en una factura →
"Imprimir / Guardar PDF" → en el diálogo, Destino "Guardar como PDF".

### 5.1 Botón de imprimir en el listado

**Qué hace:** en el listado, cada factura **FACTURADA** tiene un botón cuadrado con un ícono de
impresora (entre "Ver" y "Anular en ARCA"). Hace lo mismo que "Imprimir / Guardar PDF" de la
pantalla "Ver", pero sin abrir la factura. El texto "Imprimir / Guardar PDF" aparece como tooltip.

| Archivo | Cambio |
|---|---|
| `src/app.ts` (front) | `botonIcono()` y `ICONO_IMPRIMIR` (SVG); botón en las acciones de las filas FACTURADA; `imprimirFactura(id, botonUsado)` ahora recibe el id y el botón que la llamó (lo usan el listado y la pantalla "Ver"). |
| `index.html` (front) | Estilo `.boton.icono` (30 × 30 px). |

---

## 6. Botón de cierre del comprobante

**Qué hace:** al crear, editar o ver una factura, arriba a la derecha (junto al título) hay un botón
cuadrado con una **✕** que cierra el comprobante sin guardar y vuelve al **último listado**: la misma
vista (Borradores, Emitidas, etc.), con los mismos filtros y en la **misma página** en la que estaba.
Ejemplo: Borradores → página 2 → Editar → ✕ → sigue en Borradores, página 2.
"Cancelar" / "Volver" (abajo) ahora también conserva la página.

| Archivo | Cambio |
|---|---|
| `index.html` (front) | Botón `#btn-cerrar-formulario` (ícono ✕ en SVG) en el encabezado del comprobante; estilo `.boton.icono.cerrar-formulario` (36 × 36 px). |
| `src/app.ts` (front) | `mostrarVista(vista, conservarPagina = false)`: con `true` no vuelve a la página 1. `cerrarFormulario()` la usa así; el botón nuevo y "Cancelar" llaman a `cerrarFormulario()`. |

---

## 7. El proyecto arranca sin ARCA

**Qué hace:** quien no configuró el certificado de ARCA puede levantar y probar todo el sistema.
- Antes, sin las variables `ARCA_*` el backend **no arrancaba** (`Could not resolve placeholder 'ARCA_CUIT'`).
  Ahora tienen valores por defecto vacíos y la app arranca igual.
- Al usar algo de ARCA sin configurarlo ("Facturar emitidas en ARCA", "Anular en ARCA") se responde
  **503** con un solo mensaje claro: "ARCA no esta configurado en esta PC: faltan las variables de
  entorno ARCA_CUIT, ARCA_KEY_PATH y ARCA_CERT_PATH...". No se marca ninguna factura como rechazada.
- `README.md`: sección nueva "Puesta en marcha rápida (sin ARCA)", sección "ARCA (opcional)",
  problemas frecuentes nuevos y datos desactualizados corregidos (20 facturas de prueba, estado FACTURADA,
  vista Facturadas, endpoints nuevos).

| Archivo | Cambio |
|---|---|
| `src/main/resources/application.properties` (back) | `arca.cuit=${ARCA_CUIT:0}`, `arca.key-path=${ARCA_KEY_PATH:}`, `arca.cert-path=${ARCA_CERT_PATH:}`. |
| `arca/WsaaService.java` | `verificarConfiguracion()` (público): lanza `ArcaException` si falta el CUIT o las rutas; se llama al pedir el ticket. |
| `Service/FacturacionArcaService.java` | `facturarEmitidas()` y `anularEnArca()` verifican la configuración antes de empezar. |
| `Controller/ManejadorErrores.java` | `ArcaException` → **503** con el mensaje en el cuerpo. |
| `README.md` (raíz) | Ver arriba. |

---

## 8. Imprimir también las facturas EMITIDA (sin CAE)

**Qué hace:** las facturas EMITIDA (todavía no facturadas en ARCA) también se pueden imprimir o guardar en PDF.
- En el listado, las filas EMITIDA tienen el mismo botón de impresora que las FACTURADA.
- En "Editar" de una EMITIDA aparece "Imprimir / Guardar PDF". Imprime lo **guardado**: los cambios sin guardar no salen.
- La hoja es la misma Factura C, pero numerada con el **punto de venta y número del sistema**
  (ej. `0001-00000012`) y con la fecha de emisión del sistema. Donde iba el CAE dice "Sin CAE" y un
  aviso en negrita: **"Comprobante NO VÁLIDO COMO FACTURA: todavía no fue autorizado por ARCA."** (recuadro punteado).
- El PDF se sugiere como `Factura-C-0001-00000012-SIN-CAE`.
- Funciona aunque ARCA no esté configurado en la PC (el CUIT del emisor sale como "—").
- BORRADOR y ANULADA siguen sin imprimirse (responde **409**).

| Archivo | Cambio |
|---|---|
| `DTOs/ComprobanteImpresionDTO.java` | Campos nuevos `estado`, `puntoVentaNumero`, `fechaEmision`. `puntoVentaArca`, `numeroArca`, `fechaFacturacion`, `cae` y `caeVencimiento` ahora pueden ser null (EMITIDA). |
| `Service/ComprobanteImpresionService.java` | Acepta EMITIDA además de FACTURADA; en una EMITIDA manda los datos de ARCA en null. |
| `Controller/FacturaRestController.java` | Comentario de `GET /{id}/impresion` actualizado. |
| `src/models/factura.dto.ts` (front) | `ComprobanteImpresionDTO` espeja los cambios del DTO. |
| `src/app.ts` (front) | Botón de imprimir en las filas EMITIDA y en el formulario de edición de una EMITIDA; `imprimirFactura()` arma la hoja con o sin CAE. |
| `index.html` (front) | Estilo `.imp-cae.sin-cae` (borde punteado y aviso en negrita). |

**Cómo probar:** Emitidas → botón de impresora en una fila → en el diálogo, "Guardar como PDF". Tiene que decir "Sin CAE" y el aviso de no válido.

---

## 9. "Facturar emitidas en ARCA": todas o solo las de la página

**Qué hace:** antes el botón facturaba **todas** las EMITIDA del sistema. Ahora abre un desplegable con dos opciones:
- **Facturar *todas* las facturas emitidas:** como antes; ignora filtros y página.
- **Facturar todas las de esta página:** solo las EMITIDA que se ven en la página actual del listado
  (con los filtros aplicados). Se deshabilita si en la página no hay ninguna emitida.

Cada opción muestra cuántas facturas va a mandar y pide confirmación. El desplegable se cierra con un clic afuera o con Escape.

| Archivo | Cambio |
|---|---|
| `DTOs/FacturarEmitidasRequestDTO.java` (nuevo) | `record` con `List<Long> ids` (cuerpo opcional del POST). |
| `Controller/FacturaRestController.java` | `POST /facturar-emitidas` acepta `@RequestBody(required = false)`: sin cuerpo = todas; `{ "ids": [..] }` = solo esas. |
| `Service/FacturacionArcaService.java` | `facturarEmitidas(List<Long> idsPedidos)`: `null` = todas; con IDs, solo las que sigan EMITIDA; lista vacía → **400**. |
| `Repository/FacturaVentaRepository.java` | `buscarIdsPorEstadoEntre(estado, ids)`: como `buscarIdsPorEstado` pero con `f.id IN :ids`. |
| `src/services/FacturaService.ts` (front) | `facturarEmitidas(ids?)`: con IDs manda el JSON `{ ids }`. |
| `src/app.ts` (front) | `emitidasDeLaPagina` (se llena en `dibujarFacturas`), `abrirMenuFacturar()` / `cerrarMenuFacturar()`, `facturarEmitidas(soloPagina)`; cierre con clic afuera y Escape. |
| `index.html` (front) | `#menu-facturar` con `#opciones-facturar` (dos opciones) y sus estilos. |

**Cómo probar:** Emitidas → filtrar o ir a otra página → "Facturar emitidas en ARCA ▾" → "Facturar todas las de esta página".
Solo pasan a FACTURADA las de esa página; las demás siguen emitidas. Verificable con `GET /api/arca/ultimo` (sube en la cantidad facturada).

---

## 10. Botón "Facturar" en cada factura emitida

**Qué hace:** cada fila EMITIDA del listado tiene un botón **Facturar** que manda a ARCA solo esa factura.
Pide confirmación, muestra "Facturando…" mientras espera y después avisa el resultado:
- Aprobada: mensaje verde con el CAE y el número de comprobante de ARCA; la fila pasa a FACTURADA.
- Rechazada: mensaje rojo con el motivo de ARCA; la factura sigue EMITIDA.

No hizo falta tocar el back: reusa `POST /api/facturas/facturar-emitidas` con el cuerpo `{ "ids": [id] }` (sección 9).

| Archivo | Cambio |
|---|---|
| `src/app.ts` (front) | Botón "Facturar" en las filas EMITIDA de `dibujarFacturas()` y función nueva `facturarUna(factura, boton)`. |
| `index.html` (front) | `.acciones` con `flex-wrap`: si los botones de una fila no entran, bajan de renglón en vez de cortarse. |
| `index.html` + `src/app.ts` (front) | Fila EMITIDA con clase `acciones-emitida`: grilla con Facturar/Editar arriba y Anular/Eliminar abajo (2x2), y el botón de imprimir aparte en una tercera columna, centrado entre los dos renglones. |

**Cómo probar:** Emitidas → "Facturar" en una fila → aceptar. Solo esa pasa a FACTURADA; el resto sigue igual.
