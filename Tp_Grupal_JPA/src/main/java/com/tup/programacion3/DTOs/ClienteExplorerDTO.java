package com.tup.programacion3.DTOs;

/** Una fila del explorador de clientes (GET /api/catalogos/clientes). */
public record ClienteExplorerDTO(
        Long id,
        String denominacion,
        String cuitCuil,
        String email,          // null si no tiene contacto
        String telefono,
        String domicilio) {    // "calle numero"
}
