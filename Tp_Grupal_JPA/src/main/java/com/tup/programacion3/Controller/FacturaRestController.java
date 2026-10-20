package com.tup.programacion3.Controller;

import com.tup.programacion3.DTOs.FacturaReporteDTO;
import com.tup.programacion3.DTOs.FacturaRequestDTO;
import com.tup.programacion3.Service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaRestController {

    private final FacturaService facturaService;

    @GetMapping
    public List<FacturaReporteDTO> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo) {

        LocalDateTime desde = (fechaDesde != null) ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = (fechaHasta != null) ? fechaHasta.atTime(LocalTime.MAX) : null;

        return facturaService.buscarFacturasFiltradas(desde, hasta, estado, montoMinimo);
    }

    // POST /api/facturas -> crea una factura. Responde 201 Created con la factura creada.
    @PostMapping
    public ResponseEntity<FacturaReporteDTO> crear(@RequestBody FacturaRequestDTO request) {
        FacturaReporteDTO creada = facturaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // PUT /api/facturas/{id} -> modifica cabecera, items y/o estado (ej. anular). Responde 200 OK.
    @PutMapping("/{id}")
    public ResponseEntity<FacturaReporteDTO> actualizar(@PathVariable Long id,
                                                        @RequestBody FacturaRequestDTO request) {
        FacturaReporteDTO actualizada = facturaService.actualizar(id, request);
        return ResponseEntity.ok(actualizada);
    }

    // DELETE /api/facturas/{id} -> borra la factura y sus detalles. Responde 204 No Content.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        facturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}