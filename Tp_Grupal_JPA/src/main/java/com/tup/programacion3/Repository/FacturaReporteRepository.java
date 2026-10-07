package com.tup.programacion3.Repository;

import com.tup.programacion3.DTOs.FacturaReporteDTO;
import jakarta.persistence.EntityManager;   // o javax.persistence si tu proyecto usa javax
import java.util.List;

public class FacturaReporteRepository {

    private final EntityManager em;

    public FacturaReporteRepository(EntityManager em) {
        this.em = em;
    }

    public List<FacturaReporteDTO> obtenerReporte() {
        String jpql =
                "SELECT new com.tup.programacion3.DTOs.FacturaReporteDTO(" +
                        "  f.numero, " +
                        "  f.fechaEmision, " +
                        "  COALESCE(c.denominacion, 'Consumidor Final'), " +
                        "  ci.denominacion, " +
                        "  pv.descripcion, " +
                        "  f.importeTotal, " +
                        "  COUNT(d) " +
                        ") " +
                        "FROM FacturaVenta f " +
                        "LEFT JOIN f.cliente c " +
                        "JOIN f.condicionIva ci " +
                        "JOIN f.puntoVenta pv " +
                        "LEFT JOIN f.detalles d " +
                        "GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, " +
                        "         ci.denominacion, pv.descripcion, f.importeTotal";

        return em.createQuery(jpql, FacturaReporteDTO.class).getResultList();
    }
}