package com.tup.programacion3;

import com.tup.programacion3.Entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
            rubroBebidas.setDenominacion("Bebidas");
            rubroBebidas.setFechaAlta(ahora);
            rubroBebidas.setFechaaModificar(ahora);
            rubroBebidas.setUsuarioCarga(usuarioAdmin);
            rubroBebidas.setUsuarioModificacion(usuarioAdmin);
            em.persist(rubroBebidas);

            Marca marcaGaseosa = new Marca();
            marcaGaseosa.setCodigo(200);
            marcaGaseosa.setDenominacion("Coca Cola");
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
            factura.setCliente(cliente);
            factura.setCondicionIva(condicionIva);
            factura.setTipoMoneda(tipoMoneda);
            factura.setEstado("EMITIDA");
            factura.setImporteCobrado(0.0);
            factura.setImporteSaldo(7000.0);
            factura.setImporteTotal(7000.0); // 2 * 2500 + 1 * 2000 = 7000
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
            // Asociación bidireccional mediante helper
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
            // Asociación bidireccional mediante helper
            factura.addDetalle(detalle2);

            // ==================================================================
            // 9. REQUISITO CLAVE: Persistir ÚNICAMENTE el objeto cabecera FacturaVenta
            //    (No se debe llamar a em.persist para detalle1 ni detalle2)
            // ==================================================================
            em.persist(factura);

            //TP JPQL
            //Obtener la lista completa de todas las facturas de venta registradas en el sistema.
            String Consulta1 = "Select f from FacturaVenta f";
            List<FacturaVenta> c1 = em.createQuery(Consulta1, FacturaVenta.class).getResultList();

            //Seleccionar únicamente el número de factura, la fecha de emisión y el
            //importe total de todas las facturas de venta.
            String Consulta2 = "SELECT f.numero, f.fechaEmision, f.importeTotal FROM FacturaVenta f";
            List<Object[]> c2 = em.createQuery(Consulta2, Object[].class).getResultList();
            System.out.println("Consulta 2: Número de factura, fecha de emisión e importe total");
            for (Object[] row : c2) {
                Long numeroFactura = (Long) row[0];
                LocalDateTime fechaEmision = (LocalDateTime) row[1];
                Double importeTotal = (Double) row[2];

                System.out.println("Número de factura: " + numeroFactura + ", Fecha de emisión: " + fechaEmision + ", Importe total: " + importeTotal);
            }

            //Obtener la lista completa de todas las facturas de venta registradas en el sistema.

            List<FacturaVenta> facturas1 = em.createQuery("""
                                                SELECT f
                                                FROM FacturaVenta f""", FacturaVenta.class).getResultList();

            //Seleccionar únicamente el número de factura, la fecha de emisión y el
            //importe total de todas las facturas de venta.

            List<Object[]> facturas2 = em.createQuery("""
                                                SELECT f.numero, f.fechaEmision, f.importeTotal
                                                FROM FacturaVenta f""", Object[].class).getResultList();


            // Obtener todos los artículos que pertenecen a un rubro con una
            //denominación específica (ej. "Electrónica").

            List<Articulo> articulos1 = em.createQuery("""
                                            SELECT a
                                            FROM Articulo a
                                            WHERE LOWER(a.rubro.denominacion) = LOWER (:denominacion)""", Articulo.class).setParameter("denominacion", "Electronica").getResultList();

            //Listar todas las facturas de venta emitidas dentro de un rango de fechas
            //determinado.

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            Date desde = formato.parse("2026-09-09");
            Date hasta = formato.parse("2026-09-10");

            List<FacturaVenta> facturas3 = em.createQuery("""
                                                SELECT f
                                                FROM FacturaVenta f
                                                WHERE f.fechaEmision BETWEEN :desde AND :hasta""", FacturaVenta.class).setParameter("desde",  desde).setParameter("hasta", hasta).getResultList();

            //Obtener las facturas cuyo estado sea "EMITIDA", con un importe total
            //superior a $10,000 y que no hayan sido anuladas (fechaAnulacion sea nula).

            List<FacturaVenta> facturas4 = em.createQuery("""
                                                SELECT f
                                                FROM FacturaVenta f
                                                WHERE f.estado = :estado AND f.importeTotal > :importe AND f.fechaAnulacion IS NULL""", FacturaVenta.class).setParameter("estado", "EMITIDA").setParameter("importe", 10000.0).getResultList();

            //Buscar todos los clientes cuya denominación contenga un texto parcial
            //(sin importar mayúsculas/minúsculas) o cuyo CUIT/CUIL comience con "20-".

            List<Cliente> cliente1 = em.createQuery("""
                                        SELECT c
                                        FROM Cliente c
                                        WHERE LOWER(c.denominacion) LIKE LOWER(:denominacion) OR c.cuitCuil LIKE :cuit""", Cliente.class).setParameter("denominacion", "%Ar%").setParameter("cuit", "20-%").getResultList();

            //Obtener sin duplicados todos los estados posibles registrados en las
            //facturas de venta, ordenados alfabéticamente de forma ascendente.

            List<String> estados1 = em.createQuery("""
                                               SELECT DISTINCT f.estado
                                               FROM FacturaVenta f
                                               ORDER BY f.estado ASC""", String.class).getResultList();

            //Obtener la cantidad total de facturas emitidas, la suma acumulada de
            //sus importes totales y el importe promedio devuelto en un solo objeto/arreglo.

            Object[] objects1 = em.createQuery("""
                                                SELECT COUNT(f), SUM(f.importeTotal), AVG(f.importeTotal)
                                                FROM FacturaVenta f
                                                WHERE f.estado = :estado""", Object[].class).setParameter("estado", "EMITIDA").getSingleResult();

            //Obtener todos los puntos de venta cuyo número coincida con una lista
            //de enteros proporcionada por parámetro (ej. 1, 2, 5).

            List<Integer> numeros = List.of(1, 2, 5);

            List<PuntoVenta> puntoVentas1 = em.createQuery("""
                                                    SELECT pv
                                                    FROM PuntoVenta pv
                                                    WHERE pv.numero IN :numero""", PuntoVenta.class).setParameter("numero", numeros).getResultList();

            //consultar todas las facturas de venta creadas por un usuario en
            //particular navegando por su nombre de usuario de carga (usuarioCarga.usuario).

            List<FacturaVenta> facturas5 = em.createQuery("""
                                                SELECT f
                                                FROM FacturaVenta f
                                                WHERE f.usuarioCarga.usuario = :usuario""", FacturaVenta.class).setParameter("usuario", "admin").getResultList();

            //Obtener todos los detalles de factura (FacturaVentaDetalle) que
            //correspondan a facturas emitidas por un punto de venta determinado.

            List<FacturaVentaDetalle> facturasDetalles1 = em.createQuery("""
                                                                SELECT fv
                                                                FROM FacturaVentaDetalle fv
                                                                JOIN fv.factura f
                                                                WHERE f.puntoVenta.numero = :numero""", FacturaVentaDetalle.class).setParameter("numero", 1).getResultList();

            //Listar la denominación de todos los artículos junto con la denominación
            //de su marca asociada, incluyendo también aquellos artículos que no posean una
            //marca asignada.

            List<Object[]> denominaciones = em.createQuery("""
                                                SELECT a.denominacion, m.denominacion
                                                FROM Articulo a
                                                LEFT JOIN a.marca m""", Object[].class).getResultList();

            //Obtener todas las facturas de venta que contengan al menos un detalle
            //de artículo perteneciente a una marca específica.

            List<FacturaVenta> facturas6 = em.createQuery("""
                                                SELECT DISTINCT f
                                                FROM FacturaVenta f
                                                JOIN f.detalles d
                                                JOIN d.listaPrecioArticulo lpa
                                                JOIN lpa.articulo a
                                                JOIN a.marca m
                                                WHERE m.denominacion = :marca
                                                """, FacturaVenta.class)
                    .setParameter("marca", "Samsung")
                    .getResultList();

            //Listar las facturas de venta cuyo importeTotal sea estrictamente mayor
            //al promedio de importeTotal de todas las facturas registradas.

            List<FacturaVenta> facturas7 = em.createQuery("""
                                                SELECT f
                                                FROM FacturaVenta f
                                                WHERE f.importeTotal > (SELECT AVG(f2.importeTotal)
                                                                        FROM FacturaVenta f2)""", FacturaVenta.class).getResultList();

            // Obtener la descripción del punto de venta, la cantidad de facturas
            //emitidas por cada uno y la suma total facturada.

            List<Object[]> objects2 = em.createQuery("""
                                            SELECT f.puntoVenta.descripcion, COUNT(f), SUM(f.importeTotal)
                                            FROM FacturaVenta f
                                            WHERE f.estado = :estado
                                            GROUP BY f.puntoVenta.descripcion""", Object[].class).setParameter("estado", "EMITIDA").getResultList();


            //Obtener los nombres de los usuarios de carga que hayan registrado más
            //de 5 facturas de venta en el sistema.

            List<String> objects3 = em.createQuery("""
                                        SELECT u.usuario
                                        FROM FacturaVenta f
                                        JOIN f.usuarioCarga u
                                        GROUP BY u.usuario
                                        HAVING COUNT(f) > :numero""", String.class).setParameter("numero", 5).getResultList();

            //Obtener la denominación de cada marca, la cantidad total de unidades
            //vendidas (SUM(cantidad)) y el subtotal acumulado, agrupado por marca.

            List<Object[]> objects4 = em.createQuery("""
                                        SELECT m.denominacion, SUM(d.cantidad), SUM(d.importeSubtotal)
                                        FROM FacturaVentaDetalle d
                                        JOIN d.listaPrecioArticulo lpa
                                        JOIN lpa.articulo a
                                        JOIN a.marca m
                                        GROUP BY m.denominacion
                                        """, Object[].class).getResultList();

            //Obtener la lista de todas las marcas que tienen al menos un artículo que
            //haya sido facturado en alguna factura de venta.

            List<Marca> marcas = em.createQuery("""
                                    SELECT m
                                    FROM Marca m
                                    WHERE EXISTS (
                                        SELECT d
                                        FROM FacturaVentaDetalle d
                                        JOIN d.listaPrecioArticulo lpa
                                        JOIN lpa.articulo a
                                        WHERE a.marca = m
                                    )
                                    """, Marca.class)
                    .getResultList();

            //Obtener todos los artículos registrados en el sistema que nunca han sido
            //incluidos en ningún detalle de factura de venta.

            List<Articulo> articulos2 = em.createQuery("""
                                                   SELECT a
                                                   FROM Articulo a
                                                   WHERE NOT EXISTS (SELECT d
                                                                    FROM FacturaVentaDetalle d
                                                                    JOIN d.listaPrecioArticulo lpa
                                                                    JOIN lpa.articulo a1
                                                                    WHERE a1 = a)""", Articulo.class).getResultList();

            //Listar el número de factura, su importe total y una columna calculada
            //llamada "Categoría" que clasifique la factura como:
            //o "ALTO VALOR" si el importeTotal es mayor a $50,000.
            //o "MEDIO VALOR" si el importeTotal está entre $10,000 y $50,000.
            //o "BAJO VALOR" si el importeTotal es menor a $10,000. Ordenar los
            //resultados de mayor a menor importe.

            List<Object[]> object5 = em.createQuery("""
                                        SELECT f.numero, f.importeTotal, CASE 
                                                                                WHEN f.importeTotal > :importe1 THEN 'ALTO VALOR'
                                                                                WHEN f.importeTotal >= :importe2 THEN 'MEDIO VALOR'
                                                                                ELSE 'BAJO VALOR'
                                                                         END AS Categoria
                                        FROM FacturaVenta f
                                        ORDER BY f.importeTotal DESC""", Object[].class).setParameter("importe1", 50000.0).setParameter("importe2", 10000.0).getResultList();


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
}