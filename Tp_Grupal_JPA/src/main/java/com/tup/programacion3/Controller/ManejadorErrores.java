package com.tup.programacion3.Controller;

import com.tup.programacion3.DTOs.ErrorDTO;
import com.tup.programacion3.Exception.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Traduce las excepciones que lanza el service a respuestas HTTP con su codigo.
 * Aplica a TODOS los controllers, asi ningun endpoint necesita try/catch.
 */
@RestControllerAdvice
public class ManejadorErrores {

    // 404: se pidio algo que no existe (factura, cliente, precio de un articulo...)
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDTO> noEncontrado(RecursoNoEncontradoException ex) {
        return armarRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400: datos invalidos en el request (falta un campo, cantidad <= 0, estado desconocido...)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> datosInvalidos(IllegalArgumentException ex) {
        return armarRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 409: la operacion choca con el estado actual (ej. modificar una factura ANULADA)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDTO> conflicto(IllegalStateException ex) {
        return armarRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<ErrorDTO> armarRespuesta(HttpStatus status, String mensaje) {
        ErrorDTO error = new ErrorDTO(status.value(), mensaje, LocalDateTime.now());
        return ResponseEntity.status(status).body(error);
    }
}
