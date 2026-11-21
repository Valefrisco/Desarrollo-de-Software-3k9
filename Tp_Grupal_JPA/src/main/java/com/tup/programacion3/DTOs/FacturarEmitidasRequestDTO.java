package com.tup.programacion3.DTOs;

import java.util.List;

/**
 * Cuerpo (opcional) de POST /api/facturas/facturar-emitidas.
 * Sin cuerpo, o con "ids": null -> se facturan TODAS las EMITIDA.
 * Con "ids": [1, 2, 3] -> solo esas (las que sigan EMITIDA), ej. las de la pagina actual del listado.
 */
public record FacturarEmitidasRequestDTO(List<Long> ids) {
}
