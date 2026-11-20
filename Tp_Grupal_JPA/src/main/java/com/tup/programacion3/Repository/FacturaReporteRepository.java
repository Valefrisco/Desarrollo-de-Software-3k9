package com.tup.programacion3.Repository;

import com.tup.programacion3.DTOs.FacturaReporteDTO;
import com.tup.programacion3.DTOs.PaginaDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
    public class FacturaReporteRepository {

    private static final String SELECT_REPORTE =
            "SELECT new com.tup.programacion3.DTOs.FacturaReporteDTO(" +
                    " f.id, f.numero, f.fechaEmision, COALESCE(c.denominacion, 'Consumidor Final'), " +
                    " ci.denominacion, pv.descripcion, f.importeTotal, f.estado, COUNT(d), f.cae, f.numeroArca, f.numeroNotaCredito, f.caeNotaCredito, c.cuitCuil) " +
                    "FROM FacturaVenta f " +
                    "LEFT JOIN f.cliente c " +
                    "JOIN f.condicionIva ci " +
                    "JOIN f.puntoVenta pv " +
                    "LEFT JOIN f.detalles d ";

    private static final String GROUP_BY =
            "GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, ci.denominacion, pv.descripcion, " +
                    "f.importeTotal, f.estado, f.cae, f.numeroArca, f.numeroNotaCredito, f.caeNotaCredito, c.cuitCuil ";

    // Orden fijo (mas nuevas primero; el id desempata): sin un orden estable,
    // la misma factura podria aparecer en dos paginas o en ninguna.
    private static final String ORDER_BY = "ORDER BY f.fechaEmision DESC, f.id DESC";

    @PersistenceContext
    private EntityManager em;

    /** Todas las facturas que cumplen los filtros, sin paginar (se usa para los contadores del menu). */
    public List<FacturaReporteDTO> buscarFacturasFiltradas(
            LocalDateTime fechaDesde, LocalDateTime fechaHasta, String estado, Double montoMinimo,
            Long numero, String cliente) {

        Map<String, Object> params = new HashMap<>();
        String where = construirWhere(fechaDesde, fechaHasta, estado, montoMinimo, numero, cliente, params);

        TypedQuery<FacturaReporteDTO> query =
                em.createQuery(SELECT_REPORTE + where + GROUP_BY + ORDER_BY, FacturaReporteDTO.class);
        params.forEach(query::setParameter);
        return query.getResultList();
    }

    /**
     * Una pagina de facturas que cumplen los filtros.
     * Hace dos consultas: una trae solo las filas de la pagina (LIMIT/OFFSET en SQL)
     * y otra cuenta y suma TODAS las que cumplen los filtros, para calcular las paginas y el total.
     */
    public PaginaDTO<FacturaReporteDTO> buscarPaginaFiltrada(
            LocalDateTime fechaDesde, LocalDateTime fechaHasta, String estado, Double montoMinimo,
            Long numero, String cliente, int pagina, int tamano) {

        Map<String, Object> params = new HashMap<>();
        String where = construirWhere(fechaDesde, fechaHasta, estado, montoMinimo, numero, cliente, params);

        // 1) Cantidad total y suma de importes (las anuladas no suman, salvo que se pidan solo anuladas)
        String suma = "ANULADA".equalsIgnoreCase(estado)
                ? "f.importeTotal"
                : "CASE WHEN f.estado <> 'ANULADA' THEN f.importeTotal ELSE 0.0 END";
        Query totales = em.createQuery(
                "SELECT COUNT(f), COALESCE(SUM(" + suma + "), 0.0) FROM FacturaVenta f LEFT JOIN f.cliente c " + where);
        params.forEach(totales::setParameter);
        Object[] fila = (Object[]) totales.getSingleResult();
        long totalElementos = ((Number) fila[0]).longValue();
        double importeTotal = ((Number) fila[1]).doubleValue();

        int totalPaginas = (int) Math.ceil((double) totalElementos / tamano);
        // Si piden una pagina que ya no existe (ej. se borro la ultima factura de la ultima pagina),
        // se devuelve la ultima que si existe
        int paginaReal = Math.max(0, Math.min(pagina, totalPaginas - 1));

        // 2) Solo las filas de esta pagina
        TypedQuery<FacturaReporteDTO> query =
                em.createQuery(SELECT_REPORTE + where + GROUP_BY + ORDER_BY, FacturaReporteDTO.class);
        params.forEach(query::setParameter);
        query.setFirstResult(paginaReal * tamano);   // desde que fila (OFFSET)
        query.setMaxResults(tamano);                 // cuantas filas (LIMIT)

        return new PaginaDTO<>(query.getResultList(), paginaReal, tamano, totalElementos, totalPaginas, importeTotal);
    }

    /**
     * Arma el WHERE con los filtros que vienen con valor y carga sus parametros.
     * Usa columnas de f y de c (cliente, con LEFT JOIN: las de Consumidor Final no tienen cliente).
     */
    private static String construirWhere(LocalDateTime fechaDesde, LocalDateTime fechaHasta, String estado,
                                         Double montoMinimo, Long numero, String cliente,
                                         Map<String, Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (fechaDesde != null) { where.append("AND f.fechaEmision >= :fechaDesde "); params.put("fechaDesde", fechaDesde); }
        if (fechaHasta != null) { where.append("AND f.fechaEmision <= :fechaHasta "); params.put("fechaHasta", fechaHasta); }
        if (estado != null && !estado.isBlank()) { where.append("AND f.estado = :estado "); params.put("estado", estado); }
        if (montoMinimo != null) { where.append("AND f.importeTotal >= :montoMinimo "); params.put("montoMinimo", montoMinimo); }
        // Numero de factura: coincidencia exacta (los borradores no tienen numero, nunca coinciden)
        if (numero != null) { where.append("AND f.numero = :numero "); params.put("numero", numero); }
        // Cliente: busca el texto dentro de la razon social o del CUIT/CUIL, sin distinguir mayusculas.
        // Las facturas sin cliente se buscan como "Consumidor Final".
        if (cliente != null && !cliente.isBlank()) {
            where.append("AND (LOWER(COALESCE(c.denominacion, 'Consumidor Final')) LIKE :cliente ")
                 .append("OR LOWER(COALESCE(c.cuitCuil, '')) LIKE :cliente) ");
            params.put("cliente", "%" + cliente.trim().toLowerCase() + "%");
        }
        return where.toString();
    }
}
