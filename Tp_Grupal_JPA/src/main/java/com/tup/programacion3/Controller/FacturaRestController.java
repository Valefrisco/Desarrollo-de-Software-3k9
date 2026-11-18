package com.tup.programacion3.Controller;

import com.tup.programacion3.DTOs.AnulacionArcaDTO;
import com.tup.programacion3.DTOs.AnulacionArcaRequestDTO;
import com.tup.programacion3.DTOs.FacturaCompletaDTO;
import com.tup.programacion3.DTOs.FacturaReporteDTO;
import com.tup.programacion3.DTOs.FacturaRequestDTO;
import com.tup.programacion3.DTOs.PaginaDTO;
import com.tup.programacion3.DTOs.ResultadoFacturacionDTO;
import com.tup.programacion3.Service.FacturaService;
import com.tup.programacion3.Service.FacturacionArcaService;
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
// CORS: permite que el frontend (que corre en otro origen, ej. file:// o localhost:5500)
// llame a esta API desde el navegador. "*" = cualquier origen; valido para desarrollo.
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FacturaRestController {

    private final FacturaService facturaService;
    private final FacturacionArcaService facturacionArcaService;

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

    // GET /api/facturas/pagina?pagina=0&tamano=10 (+ los mismos filtros que GET /api/facturas)
    // -> una pagina del listado, de la mas nueva a la mas vieja, con el total de elementos,
    //    de paginas y la suma de importes de todos los resultados filtrados. 400 si pagina/tamano son invalidos.
    @GetMapping("/pagina")
    public PaginaDTO<FacturaReporteDTO> listarPagina(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        LocalDateTime desde = (fechaDesde != null) ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = (fechaHasta != null) ? fechaHasta.atTime(LocalTime.MAX) : null;

        return facturaService.buscarPaginaFiltrada(desde, hasta, estado, montoMinimo, pagina, tamano);
    }

    // GET /api/facturas/{id} -> factura con IDs y renglones (para cargarla en el formulario). 404 si no existe.
    @GetMapping("/{id}")
    public FacturaCompletaDTO obtener(@PathVariable Long id) {
        return facturaService.obtener(id);
    }

    // POST /api/facturas -> crea una factura. Responde 201 Created con la factura creada.
    // Sin "estado" se emite en el momento; con "estado": "BORRADOR" queda guardada sin emitir.
    @PostMapping
    public ResponseEntity<FacturaReporteDTO> crear(@RequestBody FacturaRequestDTO request) {
        FacturaReporteDTO creada = facturaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // PUT /api/facturas/{id} -> edita un BORRADOR (y lo emite si viene "estado": "EMITIDA"),
    // edita una EMITIDA (conserva su numero) o la anula ("estado": "ANULADA").
    // Responde 200 OK; 409 si la factura esta ANULADA o FACTURADA, o el cambio no se permite.
    @PutMapping("/{id}")
    public ResponseEntity<FacturaReporteDTO> actualizar(@PathVariable Long id,
                                                        @RequestBody FacturaRequestDTO request) {
        FacturaReporteDTO actualizada = facturaService.actualizar(id, request);
        return ResponseEntity.ok(actualizada);
    }

    // POST /api/facturas/facturar-emitidas -> pide el CAE a ARCA (homologacion) para todas las
    // facturas EMITIDA. Las aprobadas pasan a FACTURADA (solo lectura); las rechazadas siguen
    // EMITIDA con el motivo. Responde 200 con el resumen, aunque alguna haya sido rechazada.
    @PostMapping("/facturar-emitidas")
    public ResultadoFacturacionDTO facturarEmitidas() {
        return facturacionArcaService.facturarEmitidas();
    }

    // POST /api/facturas/{id}/anular-en-arca  body: { "motivo": "..." }
    // -> anula una FACTURADA emitiendo en ARCA una Nota de Credito C asociada. La factura pasa a
    //    ANULADA con el numero y CAE de la nota de credito. 409 si no esta FACTURADA o ARCA la rechaza.
    @PostMapping("/{id}/anular-en-arca")
    public AnulacionArcaDTO anularEnArca(@PathVariable Long id, @RequestBody AnulacionArcaRequestDTO request) {
        return facturacionArcaService.anularEnArca(id, request.motivo());
    }

    // DELETE /api/facturas/{id} -> borra la factura y sus detalles. Responde 204 No Content.
    // 409 si la factura ya esta FACTURADA en ARCA.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        facturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}