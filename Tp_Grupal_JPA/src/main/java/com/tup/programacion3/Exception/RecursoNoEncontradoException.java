package com.tup.programacion3.Exception;

/**
 * Se lanza cuando se busca por ID algo que no existe (factura, cliente, etc.).
 * El controller la traduce a una respuesta HTTP 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
