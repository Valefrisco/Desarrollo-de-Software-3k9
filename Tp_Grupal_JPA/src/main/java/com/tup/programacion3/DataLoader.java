package com.tup.programacion3;

import com.tup.programacion3.DTOs.FacturaDetalleRequestDTO;
import com.tup.programacion3.DTOs.FacturaRequestDTO;
import com.tup.programacion3.Entity.*;
import com.tup.programacion3.Repository.FacturaReporteRepository;
import com.tup.programacion3.Service.FacturaService;
import com.tup.programacion3.Service.ReporteService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.tup.programacion3.DTOs.FacturaReporteDTO;
import java.util.List;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;


@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    private final FacturaReporteRepository facturaReporteRepository;
    private final ReporteService reporteService;
    private final FacturaService facturaService;

    @Override
    @Transactional
    public void run(String... args) {
        LocalDateTime ahora = LocalDateTime.now();
        // 1. Instanciar y persistir Usuario (Auditoría)

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setUsuario("admin");
        usuarioAdmin.setClave("pass123");
        usuarioAdmin.setNombre("Valentino");
        usuarioAdmin.setApellido("Desarrollo");
        em.persist(usuarioAdmin);


        // 2. Instanciar y persistir Punto de Venta

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


        // 3. Instanciar y persistir Rubro y Marca

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

        // 4. Instanciar y persistir Artículos

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


        // 5. Instanciar y persistir Lista de Precios y sus Artículos

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


        // 6. Instanciar los datos obligatorios de la factura

        Contacto contactoCliente = new Contacto("valentinofriscolanti@gmail.com", "44445555", "1112345678");
        em.persist(contactoCliente);

        Domicilio domicilioCliente = new Domicilio("Av. Siempre Viva", "123");
        em.persist(domicilioCliente);

        Cliente cliente = new Cliente("20-12345678-9", "Cliente consumidor final", contactoCliente, domicilioCliente);
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

        TipoMoneda tipoMoneda = new TipoMoneda("PES", "Peso argentino", "$");
        tipoMoneda.setFechaAlta(ahora);
        tipoMoneda.setFechaaModificar(ahora);
        tipoMoneda.setUsuarioCarga(usuarioAdmin);
        tipoMoneda.setUsuarioModificacion(usuarioAdmin);
        em.persist(tipoMoneda);


        // 7. Instanciar Cabecera de FacturaVenta

        FacturaVenta factura = new FacturaVenta();
        factura.setNumero(1001L);
        factura.setFechaEmision(ahora);
        factura.setPuntoVenta(puntoVenta);


        // Sin esto, el JOIN f.condicionIva del reporte DTO descarta la factura (inner join con null).
        factura.setCliente(cliente);
        factura.setCondicionIva(condicionIva);
        factura.setTipoMoneda(tipoMoneda);


        factura.setEstado("EMITIDA");
        factura.setImporteCobrado(0.0);
        factura.setImporteSaldo(7000.0);
        factura.setImporteTotal(7000.0);
        factura.setFechaAlta(ahora);
        factura.setFechaaModificar(ahora);
        factura.setUsuarioCarga(usuarioAdmin);
        factura.setUsuarioModificacion(usuarioAdmin);

        // 8. Instanciar Detalles y asociar usando el método helper
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

        // 9. Persistir cabecera FacturaVenta
        em.persist(factura);

        // 10. Mas datos de prueba: opciones para los desplegables del formulario
        //     (los IDs 1 de cada tabla siguen siendo los de arriba)

        PuntoVenta pvNorte = guardar(new PuntoVenta(2, "Sucursal Norte", "Factura Electronica", "Av. Cabildo 2350"), usuarioAdmin, ahora);
        PuntoVenta pvOnline = guardar(new PuntoVenta(3, "Venta Online", "Factura Electronica", "Tienda web"), usuarioAdmin, ahora);
        guardar(new PuntoVenta(4, "Deposito Mayorista", "Factura Electronica", "Ruta 8 Km 42"), usuarioAdmin, ahora);

        CondicionIva ivaInscripto = guardar(new CondicionIva(1, "IVA Responsable Inscripto"), usuarioAdmin, ahora);
        CondicionIva ivaMonotributo = guardar(new CondicionIva(6, "Responsable Monotributo"), usuarioAdmin, ahora);
        guardar(new CondicionIva(4, "IVA Sujeto Exento"), usuarioAdmin, ahora);

        guardar(new TipoMoneda("DOL", "Dolar estadounidense", "US$"), usuarioAdmin, ahora);
        guardar(new TipoMoneda("060", "Euro", "EUR"), usuarioAdmin, ahora);

        Cliente distribuidora = nuevoCliente("30-71234567-8", "Distribuidora Del Sur S.A.", "compras@delsur.com.ar",
                "Av. Belgrano", "1450", usuarioAdmin, ahora);
        Cliente almacen = nuevoCliente("20-28456789-3", "Almacen Don Pedro", "donpedro@gmail.com",
                "Calle 12", "845", usuarioAdmin, ahora);
        Cliente maria = nuevoCliente("27-33456789-1", "Maria Fernandez", "mfernandez@hotmail.com",
                "Rivadavia", "3021", usuarioAdmin, ahora);
        Cliente kiosco = nuevoCliente("20-30111222-5", "Kiosco La Esquina", "laesquina@gmail.com",
                "Mitre", "77", usuarioAdmin, ahora);
        Cliente supermercado = nuevoCliente("30-70987654-2", "Supermercado El Trebol S.R.L.", "administracion@eltrebol.com.ar",
                "Av. Colon", "2200", usuarioAdmin, ahora);

        Rubro rubroAlmacen = guardar(new Rubro("Almacen.", 20), usuarioAdmin, ahora);
        Rubro rubroLimpieza = guardar(new Rubro("Limpieza.", 30), usuarioAdmin, ahora);
        Marca marcaSierra = guardar(new Marca("Sierra Pura.", 201), usuarioAdmin, ahora);
        Marca marcaMolino = guardar(new Marca("Don Molino.", 202), usuarioAdmin, ahora);
        Marca marcaBrillo = guardar(new Marca("Brillo Hogar.", 203), usuarioAdmin, ahora);

        Articulo agua = nuevoArticulo("ART-003", "Agua mineral 1.5L", rubroBebidas, marcaSierra, usuarioAdmin, ahora);
        Articulo yerba = nuevoArticulo("ART-004", "Yerba mate 1kg", rubroAlmacen, marcaMolino, usuarioAdmin, ahora);
        Articulo fideos = nuevoArticulo("ART-005", "Fideos spaghetti 500g", rubroAlmacen, marcaMolino, usuarioAdmin, ahora);
        Articulo aceite = nuevoArticulo("ART-006", "Aceite de girasol 900ml", rubroAlmacen, marcaMolino, usuarioAdmin, ahora);
        Articulo detergente = nuevoArticulo("ART-007", "Detergente 750ml", rubroLimpieza, marcaBrillo, usuarioAdmin, ahora);
        Articulo lavandina = nuevoArticulo("ART-008", "Lavandina 1L", rubroLimpieza, marcaBrillo, usuarioAdmin, ahora);

        // La lista mayorista (ID 1) suma los articulos nuevos; las otras dos listas los tienen todos
        nuevoPrecio(listaPrecio, agua, 1100, usuarioAdmin, ahora);
        nuevoPrecio(listaPrecio, yerba, 4800, usuarioAdmin, ahora);
        nuevoPrecio(listaPrecio, fideos, 1350, usuarioAdmin, ahora);
        nuevoPrecio(listaPrecio, aceite, 2900, usuarioAdmin, ahora);
        nuevoPrecio(listaPrecio, detergente, 1900, usuarioAdmin, ahora);
        nuevoPrecio(listaPrecio, lavandina, 1200, usuarioAdmin, ahora);

        ListaPrecio listaMinorista = guardar(new ListaPrecio("LP-MIN", "Lista Precios Minorista"), usuarioAdmin, ahora);
        ListaPrecio listaOnline = guardar(new ListaPrecio("LP-ONL", "Lista Precios Online"), usuarioAdmin, ahora);
        List<Articulo> todos = List.of(articulo1, articulo2, agua, yerba, fideos, aceite, detergente, lavandina);
        double[] preciosMinorista = {3000, 2400, 1350, 5800, 1600, 3500, 2300, 1450};
        double[] preciosOnline = {2800, 2250, 1250, 5400, 1500, 3250, 2150, 1350};
        for (int i = 0; i < todos.size(); i++) {
            nuevoPrecio(listaMinorista, todos.get(i), preciosMinorista[i], usuarioAdmin, ahora);
            nuevoPrecio(listaOnline, todos.get(i), preciosOnline[i], usuarioAdmin, ahora);
        }

        // 11. Facturas de ejemplo para el historico. Se crean con el mismo FacturaService
        //     que usa la API (mismas validaciones, calculo de importes y numeracion);
        //     despues se les corre la fecha de emision hacia atras.
        facturaEjemplo(puntoVenta, distribuidora, ivaInscripto, tipoMoneda, listaPrecio, "EMITIDA", 20, ahora,
                "Pedido semanal", renglon(articulo1, 24, 5), renglon(agua, 36, 5), renglon(yerba, 10, 0));
        facturaEjemplo(pvNorte, almacen, ivaMonotributo, tipoMoneda, listaMinorista, "EMITIDA", 12, ahora,
                null, renglon(fideos, 20, 0), renglon(aceite, 12, 0), renglon(lavandina, 6, 0));
        Long facturaAnulada = facturaEjemplo(puntoVenta, maria, condicionIva, tipoMoneda, listaMinorista, "EMITIDA", 8, ahora,
                null, renglon(articulo2, 2, 0), renglon(detergente, 1, 0));
        FacturaRequestDTO anulacion = new FacturaRequestDTO();
        anulacion.setEstado("ANULADA");
        anulacion.setObservaciones("Se cargo mal la cantidad");
        facturaService.actualizar(facturaAnulada, anulacion);
        facturaEjemplo(pvOnline, kiosco, ivaMonotributo, tipoMoneda, listaOnline, "EMITIDA", 3, ahora,
                "Compra web #4512", renglon(articulo1, 6, 0), renglon(articulo2, 6, 0), renglon(agua, 12, 10));
        facturaEjemplo(pvNorte, supermercado, ivaInscripto, tipoMoneda, listaPrecio, "BORRADOR", 1, ahora,
                "Esperando confirmacion del cliente", renglon(yerba, 30, 8), renglon(fideos, 48, 8));

        // 12. Mas facturas para probar la paginacion (10 por pagina). Con estas quedan 20 en total:
        //     12 EMITIDA, 5 BORRADOR y 3 ANULADA -> el historico y "Emitidas" ocupan 2 paginas.
        //     (Al facturar las emitidas en ARCA, "Facturadas" tambien pasa a tener 2 paginas.)
        facturaEjemplo(puntoVenta, distribuidora, ivaInscripto, tipoMoneda, listaMinorista, "EMITIDA", 60, ahora,
                "Reposicion mensual", renglon(agua, 48, 5), renglon(articulo1, 12, 0));
        facturaEjemplo(pvNorte, almacen, ivaMonotributo, tipoMoneda, listaMinorista, "EMITIDA", 55, ahora,
                null, renglon(yerba, 6, 0), renglon(aceite, 6, 0));
        facturaEjemplo(pvOnline, kiosco, ivaMonotributo, tipoMoneda, listaOnline, "EMITIDA", 50, ahora,
                "Compra web #4390", renglon(articulo2, 10, 0), renglon(agua, 10, 0));
        facturaEjemplo(puntoVenta, supermercado, ivaInscripto, tipoMoneda, listaMinorista, "EMITIDA", 45, ahora,
                "Pedido quincenal", renglon(fideos, 60, 10), renglon(detergente, 24, 5), renglon(lavandina, 24, 5));
        facturaEjemplo(pvNorte, maria, condicionIva, tipoMoneda, listaMinorista, "EMITIDA", 40, ahora,
                null, renglon(articulo1, 1, 0), renglon(yerba, 1, 0));
        facturaEjemplo(pvOnline, distribuidora, ivaInscripto, tipoMoneda, listaOnline, "EMITIDA", 35, ahora,
                "Compra web #4421", renglon(aceite, 30, 5), renglon(yerba, 20, 5));
        facturaEjemplo(puntoVenta, kiosco, ivaMonotributo, tipoMoneda, listaMinorista, "EMITIDA", 28, ahora,
                null, renglon(articulo2, 24, 0), renglon(agua, 24, 0));
        facturaEjemplo(pvNorte, almacen, ivaMonotributo, tipoMoneda, listaMinorista, "EMITIDA", 16, ahora,
                "Entrega en deposito", renglon(lavandina, 12, 0), renglon(detergente, 12, 0));
        facturaEjemplo(pvOnline, maria, condicionIva, tipoMoneda, listaOnline, "BORRADOR", 6, ahora,
                "Falta confirmar el envio", renglon(articulo1, 2, 0));
        facturaEjemplo(puntoVenta, supermercado, ivaInscripto, tipoMoneda, listaMinorista, "BORRADOR", 5, ahora,
                "Presupuesto pedido por compras", renglon(aceite, 48, 10), renglon(fideos, 48, 10));
        facturaEjemplo(pvNorte, distribuidora, ivaInscripto, tipoMoneda, listaMinorista, "BORRADOR", 4, ahora,
                null, renglon(agua, 120, 12));
        facturaEjemplo(pvOnline, kiosco, ivaMonotributo, tipoMoneda, listaOnline, "BORRADOR", 2, ahora,
                "Carrito abandonado", renglon(articulo2, 3, 0), renglon(yerba, 2, 0));
        anularEjemplo(facturaEjemplo(puntoVenta, almacen, ivaMonotributo, tipoMoneda, listaMinorista, "EMITIDA", 30, ahora,
                null, renglon(detergente, 5, 0)), "Cliente equivocado");
        anularEjemplo(facturaEjemplo(pvNorte, maria, condicionIva, tipoMoneda, listaMinorista, "EMITIDA", 10, ahora,
                null, renglon(aceite, 2, 0), renglon(lavandina, 1, 0)), "Factura duplicada");
        // ---------------- CONSULTAS JPQL ----------------

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

        // CAMBIO: ahora usa el repositorio inyectado (campo facturaReporteRepository) y el método con filtros
        imprimirSeccion(21, "Reporte con DTO (SELECT new)");
        List<FacturaReporteDTO> reporteDto =
                facturaReporteRepository.buscarFacturasFiltradas(null, null, null, null, null, null);
        reporteDto.forEach(d -> System.out.println(
                "-> Factura N° " + d.getNumeroFactura() +
                        " | " + d.getFechaEmision() +
                        " | Cliente: " + d.getClienteDenominacion() +
                        " | IVA: " + d.getCondicionIva() +
                        " | PV: " + d.getPuntoVentaDescripcion() +
                        " | Total: $" + d.getImporteTotal() +
                        " | Items: " + d.getCantidadItems()));

        List<FacturaReporteDTO> reporte =
                facturaReporteRepository.buscarFacturasFiltradas(null, null, null, null, null, null);

        reporte.forEach(d -> System.out.println(
                "-> Factura N° " + d.getNumeroFactura() +
                        " | " + d.getFechaEmision() +
                        " | Cliente: " + d.getClienteDenominacion() +
                        " | IVA: " + d.getCondicionIva() +
                        " | PV: " + d.getPuntoVentaDescripcion() +
                        " | Total: $" + d.getImporteTotal() +
                        " | Items: " + d.getCantidadItems()));

        try {
            reporteService.guardarEnArchivo("reporte-facturas.pdf", reporteService.generarPdf(reporte));
            reporteService.guardarEnArchivo("reporte-facturas.txt", reporteService.generarTxtTabulado(reporte));
            System.out.println("Reportes generados en: " + Paths.get("").toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    // ---------------- HELPERS PARA CARGAR DATOS ----------------

    /** Completa la auditoria (alta y modificacion por el usuario admin) y persiste. */
    private <T extends AuditoriaApp> T guardar(T entidad, Usuario usuario, LocalDateTime ahora) {
        entidad.setFechaAlta(ahora);
        entidad.setFechaaModificar(ahora);
        entidad.setUsuarioCarga(usuario);
        entidad.setUsuarioModificacion(usuario);
        em.persist(entidad);
        return entidad;
    }

    private Cliente nuevoCliente(String cuit, String denominacion, String email, String calle, String numero,
                                 Usuario usuario, LocalDateTime ahora) {
        Contacto contacto = new Contacto(email, null, null);
        em.persist(contacto);
        Domicilio domicilio = new Domicilio(calle, numero);
        em.persist(domicilio);
        return guardar(new Cliente(cuit, denominacion, contacto, domicilio), usuario, ahora);
    }

    private Articulo nuevoArticulo(String codigo, String denominacion, Rubro rubro, Marca marca,
                                   Usuario usuario, LocalDateTime ahora) {
        Articulo articulo = new Articulo();
        articulo.setCodigo(codigo);
        articulo.setDenominacion(denominacion);
        articulo.setRubro(rubro);
        articulo.setMarca(marca);
        return guardar(articulo, usuario, ahora);
    }

    private void nuevoPrecio(ListaPrecio lista, Articulo articulo, double precio, Usuario usuario, LocalDateTime ahora) {
        ListaPrecioArticulo lpa = new ListaPrecioArticulo();
        lpa.setListaPrecio(lista);
        lpa.setArticulo(articulo);
        lpa.setPrecioVenta(precio);
        guardar(lpa, usuario, ahora);
    }

    private static FacturaDetalleRequestDTO renglon(Articulo articulo, double cantidad, double bonificacion) {
        return new FacturaDetalleRequestDTO(articulo.getId(), cantidad, bonificacion);
    }

    /** Crea una factura con FacturaService (como si viniera de la API) y le corre la fecha de emision. */
    private Long facturaEjemplo(PuntoVenta pv, Cliente cliente, CondicionIva iva, TipoMoneda moneda, ListaPrecio lista,
                                String estado, int diasAtras, LocalDateTime ahora, String observaciones,
                                FacturaDetalleRequestDTO... renglones) {
        FacturaRequestDTO dto = new FacturaRequestDTO();
        dto.setPuntoVentaId(pv.getId());
        dto.setClienteId(cliente.getId());
        dto.setCondicionIvaId(iva.getId());
        dto.setTipoMonedaId(moneda.getId());
        dto.setListaPrecioId(lista.getId());
        dto.setEstado(estado);
        dto.setObservaciones(observaciones);
        dto.setDetalles(List.of(renglones));

        Long id = facturaService.crear(dto).getId();
        // La entidad esta "managed" (misma transaccion): el cambio se guarda solo
        em.find(FacturaVenta.class, id).setFechaEmision(ahora.minusDays(diasAtras));
        return id;
    }

    /** Anula una factura de ejemplo igual que lo haria la API (PUT con estado ANULADA y el motivo). */
    private void anularEjemplo(Long facturaId, String motivo) {
        FacturaRequestDTO anulacion = new FacturaRequestDTO();
        anulacion.setEstado("ANULADA");
        anulacion.setObservaciones(motivo);
        facturaService.actualizar(facturaId, anulacion);
    }

    // FUNCIÓN HELPER
    private static void imprimirSeccion(int numero, String titulo) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Consulta " + numero + ": " + titulo);
        System.out.println("=".repeat(80));
    }
}