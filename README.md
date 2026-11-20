# Desarrollo de Software 3K9 — Facturación de Ventas

Sistema de facturación de ventas hecho a lo largo de los trabajos prácticos de la materia:
mapeo ORM con JPA/Hibernate, consultas JPQL, API REST con Spring Boot y un CRUD completo
de facturas con frontend en TypeScript.

| Carpeta | Qué es |
| --- | --- |
| `Tp_Grupal_JPA/` | Backend: Spring Boot 3.5 + JPA/Hibernate + PostgreSQL (Java 21, Gradle) |
| `Tp_Grupal_Frontend/` | Frontend: HTML + TypeScript que consume la API REST |

---

## Puesta en marcha rápida (sin ARCA)

Después de clonar o hacer `git pull`, para probar todo el sistema **sin** facturar en ARCA:

1. Levantar PostgreSQL (`cd Tp_Grupal_JPA` y `docker compose up -d`, o ver el paso 1).
2. Abrir `Tp_Grupal_JPA` en IntelliJ, recargar Gradle (ícono del elefante) y ejecutar `Main`.
   **No hace falta configurar ninguna variable de entorno**: sin las variables `ARCA_*` el backend arranca igual.
3. En otra terminal: `cd Tp_Grupal_Frontend`, `npm install` (la primera vez), `npm run build` y `npm start`.
4. Abrir <http://localhost:5500>.

Funciona todo menos lo que habla con ARCA: crear, editar, emitir y anular facturas, filtros, paginación,
exploradores de clientes y artículos, listas de precios por renglón, etc. Los botones "Facturar emitidas en ARCA"
y "Anular en ARCA" responden "ARCA no está configurado en esta PC". Como no se puede facturar, la vista
**Facturadas** queda vacía y no hay facturas para imprimir en PDF.

> Después de **cada** `git pull`, volver a correr `npm run build` (la carpeta `dist/` no se sube al repo)
> y reiniciar el backend.

Para usar ARCA ver la sección [ARCA (opcional)](#arca-opcional).

---

## Requisitos

| Herramienta | Versión | Para qué |
| --- | --- | --- |
| JDK | 21 | Compilar y correr el backend |
| PostgreSQL | 15 o superior **o** Docker | Base de datos |
| Node.js | 18 o superior (incluye npm) | Compilar el TypeScript y servir el frontend |
| IntelliJ IDEA | Opcional | Lo más cómodo para correr el backend |

---

## 1. Base de datos

El backend se conecta a `localhost:5432`, base **`underBase`**, usuario **`postgres`**, clave **`1234`**
(ver `Tp_Grupal_JPA/src/main/resources/application.properties`).
El esquema `modeloventas` y todas las tablas los crea Hibernate solo al arrancar.

### Opción A: con Docker

```bash
cd Tp_Grupal_JPA
docker compose up -d
```

Crea un contenedor PostgreSQL con la base `underBase` y el usuario/clave correctos.
Ojo: si ya tenés un PostgreSQL instalado escuchando en el puerto 5432, hay que frenarlo primero.

### Opción B: con PostgreSQL instalado

1. Durante la instalación, poné **`1234`** como clave del usuario `postgres`
   (o cambiá `spring.datasource.password` en `application.properties` por la tuya, sin commitearla).
2. Creá la base desde pgAdmin (Query Tool) o desde `psql`:

   ```sql
   CREATE DATABASE "underBase";
   ```

   Las comillas son necesarias: sin ellas PostgreSQL la crea como `underbase` (en minúscula) y la app no la encuentra.

---

## 2. Backend

**Desde IntelliJ:** abrir la carpeta `Tp_Grupal_JPA` (no la raíz del repo), esperar a que sincronice Gradle
y ejecutar `Main` con el botón ▶.

**Desde la terminal:**

```bash
cd Tp_Grupal_JPA
gradlew.bat bootRun      # Windows
./gradlew bootRun        # Linux / Mac
```

Está listo cuando la consola muestra `Tomcat started on port 8080` y `Started Main`.
Al arrancar, el `DataLoader` carga datos de prueba, imprime las 20 consultas JPQL y genera
`reporte-facturas.pdf` y `.txt`.

> Como `ddl-auto=create-drop`, **la base se recrea en cada arranque**: siempre empieza con las 20 facturas
> de prueba del `DataLoader` (emitidas, borradores y anuladas).

Prueba rápida: abrir <http://localhost:8080/api/facturas> en el navegador. Tiene que devolver un JSON con las facturas.

---

## 3. Frontend

Con el backend corriendo:

```bash
cd Tp_Grupal_Frontend
npm install          # solo la primera vez
npm run build        # compila src/*.ts -> dist/*.js
npm start            # sirve la página en http://localhost:5500
```

Abrir **<http://localhost:5500>**. La pantalla tiene una barra lateral con **Nueva factura** y los módulos
**Borradores**, **Emitidas**, **Facturadas**, **Anuladas** e **Histórico** (todos los comprobantes, con filtros).

Mientras se desarrolla, conviene usar `npm run watch` en otra terminal en lugar de `npm run build`:
recompila cada vez que se guarda un `.ts`.

> La página no funciona abriendo el `index.html` con doble clic (`file://`): los módulos de JavaScript
> necesitan servirse por HTTP. Por eso existe `npm start`.

---

## API REST

Base: `http://localhost:8080/api/facturas`

| Método | Ruta | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| GET | `/api/facturas?fechaDesde=&fechaHasta=&estado=&montoMinimo=` | — | 200 + lista de `FacturaReporteDTO` (filtros opcionales) |
| GET | `/api/facturas/{id}` | — | 200 + `FacturaCompletaDTO` (IDs y renglones, para editar) |
| GET | `/api/catalogos` | — | 200 + `CatalogosDTO` (opciones de los desplegables del formulario) |
| POST | `/api/facturas` | `FacturaRequestDTO` | 201 + `FacturaReporteDTO` creada |
| PUT | `/api/facturas/{id}` | `FacturaRequestDTO` (solo lo que cambia) | 200 + `FacturaReporteDTO` actualizada |
| DELETE | `/api/facturas/{id}` | — | 204 sin cuerpo |

Estados: **BORRADOR → EMITIDA → FACTURADA** (con CAE de ARCA), y **EMITIDA / FACTURADA → ANULADA**.
Otros endpoints: `GET /api/facturas/pagina` (listado paginado), `POST /api/facturas/facturar-emitidas`,
`POST /api/facturas/{id}/anular-en-arca`, `GET /api/facturas/{id}/impresion`, `GET /api/catalogos/clientes`
y `GET /api/catalogos/articulos`. El detalle de cada cambio está en [`CAMBIOS.md`](CAMBIOS.md).

- `POST` sin `estado` (o con `"EMITIDA"`) emite en el momento. Con `"estado": "BORRADOR"` la guarda sin emitir.
  El borrador no tiene número: lo toma al emitirse (correlativo por punto de venta).
- `PUT` sobre un BORRADOR edita cabecera y/o renglones; si además trae `"estado": "EMITIDA"`, lo emite.
  `"clienteId": null` lo pasa a Consumidor Final (si no viene `clienteId`, el cliente no cambia).
- Una factura EMITIDA se puede editar (cabecera y renglones) conservando su número, pero no puede cambiar
  de punto de venta ni volver a borrador. También se puede anular. Una ANULADA no admite cambios.

Errores: **404** si un ID no existe, **400** si los datos son inválidos (o si una anulación trae cambios de
cabecera o renglones), **409** si se intenta modificar una factura ANULADA, cambiarle el punto de venta a una emitida o pasarla a borrador. El cuerpo del error es `{ "status", "mensaje", "timestamp" }`.

Ejemplo de alta:

```json
{
  "puntoVentaId": 1, "clienteId": 1, "condicionIvaId": 1, "tipoMonedaId": 1, "listaPrecioId": 1,
  "observaciones": "Venta mostrador",
  "detalles": [
    { "articuloId": 1, "cantidad": 2, "porcentajeBonificacion": 10 },
    { "articuloId": 2, "cantidad": 1 }
  ]
}
```

Guardar como borrador: el mismo cuerpo con `"estado": "BORRADOR"`.
Emitir un borrador: `PUT /api/facturas/{id}` con `{ "estado": "EMITIDA" }`.
Anular una emitida: `PUT /api/facturas/{id}` con `{ "estado": "ANULADA", "observaciones": "motivo" }`.

### Datos de prueba que carga el DataLoader

| Entidad | Datos |
| --- | --- |
| Puntos de venta | 1 Punto de Venta Local Central, 2 Sucursal Norte, 3 Venta Online, 4 Deposito Mayorista |
| Clientes | Cliente consumidor final (ID 1), Distribuidora Del Sur S.A., Almacen Don Pedro, Maria Fernandez, Kiosco La Esquina, Supermercado El Trebol S.R.L. |
| Condiciones IVA | Consumidor final (ID 1), IVA Responsable Inscripto, Responsable Monotributo, IVA Sujeto Exento |
| Monedas | Peso argentino (ID 1), Dolar estadounidense, Euro |
| Listas de precios | Mayorista (ID 1), Minorista, Online: las tres con los 8 articulos |
| Articulos | Gaseosa Cola 2.25L (ID 1, $2500 en la mayorista), Gaseosa Lima Limon 2L (ID 2, $2000), agua, yerba, fideos, aceite, detergente y lavandina |
| Facturas | 20: la 1001 y 19 de ejemplo creadas con `FacturaService` (12 emitidas, 5 borradores y 3 anuladas), para probar la paginación |

---

## Problemas frecuentes

| Síntoma | Causa y solución |
| --- | --- |
| `password authentication failed for user "postgres"` | La clave de tu PostgreSQL no es `1234`. Cambiala en `application.properties`. |
| `database "underBase" does not exist` | Falta crear la base (paso 1), o se creó sin comillas como `underbase`. |
| `Port 8080 was already in use` | Ya hay otra instancia del backend corriendo. Frenala antes de volver a arrancar. |
| La página muestra "No se pudo conectar con el servidor" | El backend no está levantado en `localhost:8080`. |
| En la consola del navegador (F12): `blocked by CORS policy` | El backend que está corriendo no tiene `@CrossOrigin` en `FacturaRestController`. Reiniciarlo con el código actualizado. |
| `npm start` dice `Could not read package.json` | No estás parado en la carpeta `Tp_Grupal_Frontend`. |
| Después de un pull, un botón nuevo "no hace nada" o falta algo en pantalla | El navegador usa el `dist/` viejo: correr `npm run build` y recargar con **Ctrl+F5**. |
| "ARCA no está configurado en esta PC" | Normal si no hiciste la configuración de ARCA: todo lo demás funciona. Ver [ARCA (opcional)](#arca-opcional). |

---

## ARCA (opcional)

La facturación electrónica usa el entorno de **homologación** (pruebas) de ARCA y necesita un certificado
digital propio en cada PC. Los pasos están en las guías de ARCA del equipo (Guía 1: crear el certificado;
Guía 2: usar uno que te pasaron; Guía 3: usar el tuyo). En resumen: los archivos van en `C:\arca`
(fuera del repo) y en la configuración de ejecución de IntelliJ se definen las variables
`ARCA_CUIT`, `ARCA_KEY_PATH`, `ARCA_CERT_PATH` y, si corresponde, `ARCA_PTO_VTA`.
**Nunca subir al repo** los `.key`, `.crt` ni `ta-cache.properties`.
