package com.tup.programacion3;

import com.tup.programacion3.Entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("FacturacionPU");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            LocalDateTime ahora = LocalDateTime.now();

            // ==================================================================
            // 1. Instanciar y persistir Usuario (Auditoría)
            // ==================================================================
            Usuario usuarioAdmin = new Usuario();
            usuarioAdmin.setUsuario("admin");
            usuarioAdmin.setClave("pass123");
            usuarioAdmin.setNombre("Valentino");
            usuarioAdmin.setApellido("Desarrollo");
            em.persist(usuarioAdmin);

            // ==================================================================
            // 2. Instanciar y persistir Punto de Venta
            // ==================================================================
            PuntoVenta puntoVenta = new PuntoVenta();
            puntoVenta.setNumero(1);
            puntoVenta.setDescripcion("Punto de Venta Local Central");
            puntoVenta.setTipoEmision("Factura Electronica");
            puntoVenta.setDomicilioComercial("Av. San Martin 1000");
            puntoVenta.setFechaAlta(ahora);
            puntoVenta.setFechaaModificar(ahora);
            puntoVenta.setUsuarioCarga(usuarioAdmin);
            puntoVenta.setUsuarioModificacion(usuarioAdmin);
            em.persist(puntoVenta);

            // ==================================================================
            // 3. Instanciar y persistir Rubro y Marca
            // ==================================================================
            Rubro rubroBebidas = new Rubro();
            rubroBebidas.setCodigo(10);
            rubroBebidas.setDenominacion("Bebidas.");
            rubroBebidas.setFechaAlta(ahora);
            rubroBebidas.setFechaaModificar(ahora);
            rubroBebidas.setUsuarioCarga(usuarioAdmin);
            rubroBebidas.setUsuarioModificacion(usuarioAdmin);
            em.persist(rubroBebidas);

            Marca marcaGaseosa = new Marca();
            marcaGaseosa.setCodigo(200);
            marcaGaseosa.setDenominacion("Coca Cola.");
            marcaGaseosa.setFechaAlta(ahora);
            marcaGaseosa.setFechaaModificar(ahora);
            marcaGaseosa.setUsuarioCarga(usuarioAdmin);
            marcaGaseosa.setUsuarioModificacion(usuarioAdmin);
            em.persist(marcaGaseosa);

            // ==================================================================
            // 4. Instanciar y persistir Artículos
            // ==================================================================
            Articulo articulo1 = new Articulo();
            articulo1.setCodigo("ART-001");
            articulo1.setDenominacion("Gaseosa Cola 2.25L");
            articulo1.setRubro(rubroBebidas);
            articulo1.setMarca(marcaGaseosa);
            articulo1.setFechaAlta(ahora);
            articulo1.setFechaaModificar(ahora);
            articulo1.setUsuarioCarga(usuarioAdmin);
            articulo1.setUsuarioModificacion(usuarioAdmin);
            em.persist(articulo1);

            Articulo articulo2 = new Articulo();
            articulo2.setCodigo("ART-002");
            articulo2.setDenominacion("Gaseosa Lima Limon 2L");
            articulo2.setRubro(rubroBebidas);
            articulo2.setMarca(marcaGaseosa);
            articulo2.setFechaAlta(ahora);
            articulo2.setFechaaModificar(ahora);
            articulo2.setUsuarioCarga(usuarioAdmin);
            articulo2.setUsuarioModificacion(usuarioAdmin);
            em.persist(articulo2);

            // ==================================================================
            // 5. Instanciar y persistir Lista de Precios y sus Artículos
            // ==================================================================
            ListaPrecio listaPrecio = new ListaPrecio();
            listaPrecio.setCodigo("LP-MAY");
            listaPrecio.setDenominacion("Lista Precios Mayorista");
            listaPrecio.setFechaAlta(ahora);
            listaPrecio.setFechaaModificar(ahora);
            listaPrecio.setUsuarioCarga(usuarioAdmin);
            listaPrecio.setUsuarioModificacion(usuarioAdmin);
            em.persist(listaPrecio);

            ListaPrecioArticulo precioArt1 = new ListaPrecioArticulo();
            precioArt1.setArticulo(articulo1);
            precioArt1.setListaPrecio(listaPrecio);
            precioArt1.setPrecioVenta(2500.0);
            precioArt1.setFechaAlta(ahora);
            precioArt1.setFechaaModificar(ahora);
            precioArt1.setUsuarioCarga(usuarioAdmin);
            precioArt1.setUsuarioModificacion(usuarioAdmin);
            em.persist(precioArt1);

            ListaPrecioArticulo precioArt2 = new ListaPrecioArticulo();
            precioArt2.setArticulo(articulo2);
            precioArt2.setListaPrecio(listaPrecio);
            precioArt2.setPrecioVenta(2000.0);
            precioArt2.setFechaAlta(ahora);
            precioArt2.setFechaaModificar(ahora);
            precioArt2.setUsuarioCarga(usuarioAdmin);
            precioArt2.setUsuarioModificacion(usuarioAdmin);
            em.persist(precioArt2);

            // ==================================================================
            // 6. Instanciar los datos obligatorios de la factura
            // ==================================================================
            Contacto contactoCliente = new Contacto("cliente@ejemplo.com", "1112345678", "44445555");
            em.persist(contactoCliente);

            Domicilio domicilioCliente = new Domicilio("123", "Av. Siempre Viva");
            em.persist(domicilioCliente);

            Cliente cliente = new Cliente("20-12345678-9", domicilioCliente, contactoCliente, "Cliente consumidor final");
            cliente.setFechaAlta(ahora);
            cliente.setFechaaModificar(ahora);
            cliente.setUsuarioCarga(usuarioAdmin);
            cliente.setUsuarioModificacion(usuarioAdmin);
            em.persist(cliente);

            CondicionIva condicionIva = new CondicionIva(5, "Consumidor final");
            condicionIva.setFechaAlta(ahora);
            condicionIva.setFechaaModificar(ahora);
            condicionIva.setUsuarioCarga(usuarioAdmin);
            condicionIva.setUsuarioModificacion(usuarioAdmin);
            em.persist(condicionIva);

            TipoMoneda tipoMoneda = new TipoMoneda("PES", "$", "Peso argentino");
            tipoMoneda.setFechaAlta(ahora);
            tipoMoneda.setFechaaModificar(ahora);
            tipoMoneda.setUsuarioCarga(usuarioAdmin);
            tipoMoneda.setUsuarioModificacion(usuarioAdmin);
            em.persist(tipoMoneda);

            // ==================================================================
            // 7. Instanciar Cabecera de FacturaVenta
            // ==================================================================
            FacturaVenta factura = new FacturaVenta();
            factura.setNumero(1001L);
            factura.setFechaEmision(ahora);
            factura.setPuntoVenta(puntoVenta);
            factura.setEstado("EMITIDA");
            factura.setImporteCobrado(0.0);
            factura.setImporteSaldo(7000.0);
            factura.setImporteTotal(7000.0);
            factura.setFechaAlta(ahora);
            factura.setFechaaModificar(ahora);
            factura.setUsuarioCarga(usuarioAdmin);
            factura.setUsuarioModificacion(usuarioAdmin);

            // ==================================================================
            // 8. Instanciar Detalles y asociar usando el método helper
            // ==================================================================
            FacturaVentaDetalle detalle1 = new FacturaVentaDetalle();
            detalle1.setListaPrecioArticulo(precioArt1);
            detalle1.setDescripcion("Gaseosa Cola 2.25L - Pack x2");
            detalle1.setCantidad(2.0);
            detalle1.setPrecioUnitario(2500.0);
            detalle1.setPorcentajeBonificacion(0.0);
            detalle1.setImporteNeto(4132.23);
            detalle1.setImporteIva(867.77);
            detalle1.setImporteSubtotal(5000.0);
            factura.addDetalle(detalle1);

            FacturaVentaDetalle detalle2 = new FacturaVentaDetalle();
            detalle2.setListaPrecioArticulo(precioArt2);
            detalle2.setDescripcion("Gaseosa Lima Limon 2L - x1");
            detalle2.setCantidad(1.0);
            detalle2.setPrecioUnitario(2000.0);
            detalle2.setPorcentajeBonificacion(0.0);
            detalle2.setImporteNeto(1652.89);
            detalle2.setImporteIva(347.11);
            detalle2.setImporteSubtotal(2000.0);
            factura.addDetalle(detalle2);

            // ==================================================================
            // 9. Persistir cabecera FacturaVenta
            // ==================================================================
            em.persist(factura);

            // ==================================================================
            // AQUÍ COMIENZAN LAS CONSULTAS JPQL ORGANIZADAS CON IMPRESIÓN
            // ==================================================================

            imprimirSeccion(1, "Lista completa de todas las facturas");
            List<FacturaVenta> facturas1 = em.createQuery("""
                    SELECT f
                    FROM FacturaVenta f""", FacturaVenta.class).getResultList();
            facturas1.forEach(f -> System.out.println("-> Factura N°: " + f.getNumero() + " | Total: " + f.getImporteTotal()));

            imprimirSeccion(2, "Proyección: número, fecha emisión e importe total");
            List<Object[]> facturas2 = em.createQuery("""
                    SELECT f.numero, f.fechaEmision, f.importeTotal 
                    FROM FacturaVenta f""", Object[].class).getResultList();
            for (Object[] row : facturas2) {
                System.out.println("-> Factura N°: " + row[0] + " | Fecha: " + row[1] + " | Total: " + row[2]);
            }

            imprimirSeccion(3, "Artículos por rubro");
            List<Articulo> articulos1 = em.createQuery("""
                    SELECT a 
                    FROM Articulo a 
                    WHERE LOWER(a.rubro.denominacion) = LOWER(:denominacion)""", Articulo.class)
                    .setParameter("denominacion", "Bebidas.")
                    .getResultList();
            articulos1.forEach(a -> System.out.println("-> Artículo: " + a.getCodigo() + " - " + a.getDenominacion()));

            imprimirSeccion(4, "Facturas emitidas dentro de un rango de fechas");
            LocalDateTime fechaDesde = ahora.minusDays(1);
            LocalDateTime fechaHasta = ahora.plusDays(1);
            List<FacturaVenta> facturas3 = em.createQuery("""
                    SELECT f 
                    FROM FacturaVenta f 
                    WHERE f.fechaEmision BETWEEN :desde AND :hasta""", FacturaVenta.class)
                    .setParameter("desde", fechaDesde)
                    .setParameter("hasta", fechaHasta)
                    .getResultList();
            facturas3.forEach(f -> System.out.println("-> Factura ID: " + f.getId() + " | Fecha: " + f.getFechaEmision()));

            imprimirSeccion(5, "Facturas EMITIDA > $10.000 sin anular");
            List<FacturaVenta> facturas4 = em.createQuery("""
                    SELECT f 
                    FROM FacturaVenta f 
                    WHERE f.estado = :estado 
                      AND f.importeTotal > :importe 
                      AND f.fechaAnulacion IS NULL""", FacturaVenta.class)
                    .setParameter("estado", "EMITIDA")
                    .setParameter("importe", 10000.0)
                    .getResultList();
            System.out.println("-> Facturas encontradas: " + facturas4.size());

            imprimirSeccion(6, "Clientes por denominación parcial o CUIT '20-'");
            List<Cliente> cliente1 = em.createQuery("""
                    SELECT c 
                    FROM Cliente c 
                    WHERE LOWER(c.denominacion) LIKE LOWER(:denominacion) 
                       OR c.cuitCuil LIKE :cuit""", Cliente.class)
                    .setParameter("denominacion", "%Ar%")
                    .setParameter("cuit", "20-%")
                    .getResultList();
            cliente1.forEach(c -> System.out.println("-> Cliente: " + c.getDenominacion() + " | CUIT: " + c.getCuitCuil()));

            imprimirSeccion(7, "Estados distintos de facturas ordenados");
            List<String> estados1 = em.createQuery("""
                    SELECT DISTINCT f.estado 
                    FROM FacturaVenta f 
                    ORDER BY f.estado ASC""", String.class).getResultList();
            estados1.forEach(est -> System.out.println("-> Estado: " + est));

            imprimirSeccion(8, "Agregaciones: COUNT, SUM, AVG de facturas emitidas");
            Object[] objects1 = em.createQuery("""
                    SELECT COUNT(f), SUM(f.importeTotal), AVG(f.importeTotal) 
                    FROM FacturaVenta f 
                    WHERE f.estado = :estado""", Object[].class)
                    .setParameter("estado", "EMITIDA")
                    .getSingleResult();
            System.out.println("-> Cantidad: " + objects1[0] + " | Suma: $" + objects1[1] + " | Promedio: $" + objects1[2]);

            imprimirSeccion(9, "Puntos de venta por lista de números IN");
            List<PuntoVenta> puntoVentas1 = em.createQuery("""
                    SELECT pv 
                    FROM PuntoVenta pv 
                    WHERE pv.numero IN :numeros""", PuntoVenta.class)
                    .setParameter("numeros", List.of(1, 2, 5))
                    .getResultList();
            puntoVentas1.forEach(pv -> System.out.println("-> PV N° " + pv.getNumero() + ": " + pv.getDescripcion()));

            imprimirSeccion(10, "Facturas por usuario de carga");
            List<FacturaVenta> facturas5 = em.createQuery("""
                    SELECT f 
                    FROM FacturaVenta f 
                    WHERE f.usuarioCarga.usuario = :usuario""", FacturaVenta.class)
                    .setParameter("usuario", "admin")
                    .getResultList();
            facturas5.forEach(f -> System.out.println("-> Factura N° " + f.getNumero() + " registrada por admin"));

            imprimirSeccion(11, "Detalles de facturas por número de punto de venta");
            List<FacturaVentaDetalle> facturasDetalles1 = em.createQuery("""
                    SELECT fv 
                    FROM FacturaVentaDetalle fv 
                    JOIN fv.factura f 
                    WHERE f.puntoVenta.numero = :numero""", FacturaVentaDetalle.class)
                    .setParameter("numero", 1)
                    .getResultList();
            facturasDetalles1.forEach(d -> System.out.println("-> Detalle: " + d.getDescripcion() + " | Subtotal: " + d.getImporteSubtotal()));

            imprimirSeccion(12, "Denominación de artículos con LEFT JOIN Marca");
            List<Object[]> denominaciones = em.createQuery("""
                    SELECT a.denominacion, m.denominacion 
                    FROM Articulo a 
                    LEFT JOIN a.marca m""", Object[].class).getResultList();
            for (Object[] row : denominaciones) {
                System.out.println("-> Artículo: " + row[0] + " | Marca: " + (row[1] != null ? row[1] : "Sin Marca"));
            }

            imprimirSeccion(13, "Facturas que contienen artículos de marca específica");
            List<FacturaVenta> facturas6 = em.createQuery("""
                    SELECT DISTINCT f 
                    FROM FacturaVenta f 
                    JOIN f.detalles d 
                    JOIN d.listaPrecioArticulo lpa 
                    JOIN lpa.articulo a 
                    JOIN a.marca m 
                    WHERE m.denominacion = :marca""", FacturaVenta.class)
                    .setParameter("marca", "Coca Cola.")
                    .getResultList();
            facturas6.forEach(f -> System.out.println("-> Factura N°: " + f.getNumero()));

            imprimirSeccion(14, "Facturas con total superior al promedio general");
            List<FacturaVenta> facturas7 = em.createQuery("""
                    SELECT f 
                    FROM FacturaVenta f 
                    WHERE f.importeTotal > (SELECT AVG(f2.importeTotal) FROM FacturaVenta f2)""", FacturaVenta.class)
                    .getResultList();
            facturas7.forEach(f -> System.out.println("-> Factura sobre promedio N°: " + f.getNumero()));

            imprimirSeccion(15, "Resumen por punto de venta (GROUP BY)");
            List<Object[]> objects2 = em.createQuery("""
                    SELECT f.puntoVenta.descripcion, COUNT(f), SUM(f.importeTotal) 
                    FROM FacturaVenta f 
                    WHERE f.estado = :estado 
                    GROUP BY f.puntoVenta.descripcion""", Object[].class)
                    .setParameter("estado", "EMITIDA")
                    .getResultList();
            for (Object[] row : objects2) {
                System.out.println("-> PV: " + row[0] + " | Cantidad: " + row[1] + " | Total Facturado: $" + row[2]);
            }

            imprimirSeccion(16, "Usuarios con más de 5 facturas (HAVING)");
            List<String> objects3 = em.createQuery("""
                    SELECT u.usuario 
                    FROM FacturaVenta f 
                    JOIN f.usuarioCarga u 
                    GROUP BY u.usuario 
                    HAVING COUNT(f) > :numero""", String.class)
                    .setParameter("numero", 5L)
                    .getResultList();
            System.out.println("-> Usuarios encontrados: " + objects3);

            imprimirSeccion(17, "Ventas agrupadas por marca (SUM cantidad y subtotal)");
            List<Object[]> objects4 = em.createQuery("""
                    SELECT m.denominacion, SUM(d.cantidad), SUM(d.importeSubtotal) 
                    FROM FacturaVentaDetalle d 
                    JOIN d.listaPrecioArticulo lpa 
                    JOIN lpa.articulo a 
                    JOIN a.marca m 
                    GROUP BY m.denominacion""", Object[].class).getResultList();
            for (Object[] row : objects4) {
                System.out.println("-> Marca: " + row[0] + " | Unidades: " + row[1] + " | Subtotal acumulado: $" + row[2]);
            }

            imprimirSeccion(18, "Marcas con al menos un artículo facturado (EXISTS)");
            List<Marca> marcas = em.createQuery("""
                    SELECT m 
                    FROM Marca m 
                    WHERE EXISTS (
                        SELECT d 
                        FROM FacturaVentaDetalle d 
                        JOIN d.listaPrecioArticulo lpa 
                        JOIN lpa.articulo a 
                        WHERE a.marca = m
                    )""", Marca.class).getResultList();
            marcas.forEach(m -> System.out.println("-> Marca con ventas: " + m.getDenominacion()));

            imprimirSeccion(19, "Artículos nunca facturados (NOT EXISTS)");
            List<Articulo> articulos2 = em.createQuery("""
                    SELECT a 
                    FROM Articulo a 
                    WHERE NOT EXISTS (
                        SELECT d 
                        FROM FacturaVentaDetalle d 
                        JOIN d.listaPrecioArticulo lpa 
                        JOIN lpa.articulo a1 
                        WHERE a1 = a
                    )""", Articulo.class).getResultList();
            articulos2.forEach(a -> System.out.println("-> Artículo sin ventas: " + a.getDenominacion()));

            imprimirSeccion(20, "Categorización de facturas con CASE");
            List<Object[]> object5 = em.createQuery("""
                    SELECT f.numero, f.importeTotal, 
                           CASE 
                               WHEN f.importeTotal > :importe1 THEN 'ALTO VALOR' 
                               WHEN f.importeTotal >= :importe2 THEN 'MEDIO VALOR' 
                               ELSE 'BAJO VALOR' 
                           END AS Categoria 
                    FROM FacturaVenta f 
                    ORDER BY f.importeTotal DESC""", Object[].class)
                    .setParameter("importe1", 50000.0)
                    .setParameter("importe2", 10000.0)
                    .getResultList();
            for (Object[] row : object5) {
                System.out.println("-> Factura N°: " + row[0] + " | Total: $" + row[1] + " | Categoría: " + row[2]);
            }

            // 10. Confirmar transacción
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                System.out.println("UPPPSSS");
            }
            e.printStackTrace();
        } finally {
            // 11. Cerrar recursos
            em.close();
            emf.close();
        }
    }

    // ==================================================================
    // FUNCIÓN HELPER (va dentro de la clase Main, pero fuera del main)
    // ==================================================================
    private static void imprimirSeccion(int numero, String titulo) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Consulta " + numero + ": " + titulo);
        System.out.println("=".repeat(80));
    }
}