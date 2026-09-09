package com.tup.programacion3;

import com.tup.programacion3.Entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.ArrayList;
import java.util.Date;

public class Main {
    public static void main(String[] args) {

        // 1. Iniciar el contenedor de JPA mediante Persistence.createEntityManagerFactory y obtener el EntityManager[cite: 2]
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("FacturacionPU");
        EntityManager em = emf.createEntityManager();

        try {
            // 2. Iniciar una transacción[cite: 2]
            em.getTransaction().begin();

            // 3. Instanciar los objetos necesarios[cite: 2]
            // Nota: Se asume que PuntoVenta y otras dependencias se guardan previamente o tienen persistencia en cascada configurada.
            PuntoVenta puntoVenta = new PuntoVenta();
            puntoVenta.setNumero(1);
            // Asegúrate de setear el resto de campos nullable = false de PuntoVenta y AuditoriaApp

            // Persistimos el punto de venta primero para evitar errores de entidad transitoria,
            // a menos que hayas configurado CascadeType.PERSIST en la relación ManyToOne.
            em.persist(puntoVenta);

            // 4. Crear una cabecera de FacturaVenta[cite: 2]
            FacturaVenta factura = new FacturaVenta();
            factura.setFechaEmision(new Date());
            factura.setImporteTotal(1500.0);
            factura.setEstado("EMITIDA");
            factura.setPuntoVenta(puntoVenta);
            factura.setDetalles(new ArrayList<>()); // Inicializar la colección

            // 5. Asignarle uno o más ítems FacturaVentaDetalle[cite: 2]
            FacturaVentaDetalle detalle1 = new FacturaVentaDetalle();
            detalle1.setCantidad(2);
            detalle1.setPrecioUnitario(750.0);
            detalle1.setImporteSubtotal(1500.0);

            // 6. Asociándolos bidireccionalmente[cite: 2]
            detalle1.setFactura(factura);
            factura.getDetalles().add(detalle1);

            // 7. Requisito clave: Persistir únicamente el objeto cabecera FacturaVenta utilizando un solo llamado[cite: 2]
            em.persist(factura);

            // 8. Finalizar la transacción[cite: 2]
            em.getTransaction().commit();
            System.out.println("Factura y detalles persistidos exitosamente en cascada.");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            // 9. Cerrar el EntityManager y EntityManagerFactory[cite: 2]
            em.close();
            emf.close();
        }
    }
}