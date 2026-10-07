package com.tup.programacion3.Controller;

import com.tup.programacion3.DTOs.FacturaReporteDTO;
import com.tup.programacion3.Service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
}