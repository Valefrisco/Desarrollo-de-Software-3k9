package com.tup.programacion3.DTOs;

import java.util.List;

/**
 * Una pagina de resultados (GET /api/facturas/pagina).
 *
 * @param contenido      los elementos de esta pagina
 * @param pagina         numero de pagina, empieza en 0
 * @param tamano         cantidad maxima de elementos por pagina
 * @param totalElementos cuantos elementos hay en total con esos filtros (todas las paginas)
 * @param totalPaginas   cuantas paginas hay en total (0 si no hay resultados)
 * @param importeTotal   suma de importes de TODOS los resultados filtrados, no solo de esta pagina
 *                       (sin las anuladas, salvo que se este filtrando justamente por ANULADA)
 */
public record PaginaDTO<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        double importeTotal) {
}
