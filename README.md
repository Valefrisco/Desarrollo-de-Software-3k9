# Desarrollo de Software 3K9 — Facturación de Ventas

Sistema de facturación de ventas hecho a lo largo de los trabajos prácticos de la materia:
mapeo ORM con JPA/Hibernate, consultas JPQL, API REST con Spring Boot y un CRUD completo
de facturas con frontend en TypeScript.

| Carpeta | Qué es |
| --- | --- |
| `Tp_Grupal_JPA/` | Backend: Spring Boot 3.5 + JPA/Hibernate + PostgreSQL (Java 21, Gradle) |
| `Tp_Grupal_Frontend/` | Frontend: HTML + TypeScript que consume la API REST |

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

> Como `ddl-auto=create-drop`, **la base se recrea en cada arranque**: siempre empieza con una sola factura (la 1001).

Prueba rápida: abrir <http://localhost:8080/api/facturas> en el navegador. Tiene que devolver un JSON con la factura 1001.

---

## 3. Frontend

Con el backend corriendo:

```bash
cd Tp_Grupal_Frontend
npm install          # solo la primera vez
npm run build        # compila src/*.ts -> dist/*.js
npm start            # sirve la página en http://localhost:5500
```

Abrir **<http://localhost:5500>**.

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
| POST | `/api/facturas` | `FacturaRequestDTO` | 201 + `FacturaReporteDTO` creada |
| PUT | `/api/facturas/{id}` | `FacturaRequestDTO` (solo lo que cambia) | 200 + `FacturaReporteDTO` actualizada |
| DELETE | `/api/facturas/{id}` | — | 204 sin cuerpo |

Errores: **404** si un ID no existe, **400** si los datos son inválidos, **409** si se intenta modificar
una factura ANULADA. El cuerpo del error es `{ "status", "mensaje", "timestamp" }`.

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

Anular: `PUT /api/facturas/{id}` con `{ "estado": "ANULADA", "observaciones": "motivo" }`.

### Datos de prueba que carga el DataLoader

| Entidad | ID | Dato |
| --- | --- | --- |
| Punto de venta | 1 | Punto de Venta Local Central |
| Cliente | 1 | Cliente consumidor final |
| Condición IVA | 1 | Consumidor final |
| Tipo de moneda | 1 | Peso argentino |
| Lista de precios | 1 | Lista Precios Mayorista |
| Artículo | 1 | Gaseosa Cola 2.25L — $2500 en la lista 1 |
| Artículo | 2 | Gaseosa Lima Limón 2L — $2000 en la lista 1 |

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
