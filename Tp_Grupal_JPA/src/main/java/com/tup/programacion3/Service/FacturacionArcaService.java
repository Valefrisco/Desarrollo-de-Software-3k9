package com.tup.programacion3.Service;

import com.tup.programacion3.DTOs.ResultadoFacturacionDTO;
import com.tup.programacion3.Entity.FacturaVenta;
import com.tup.programacion3.Repository.FacturaVentaRepository;
import com.tup.programacion3.arca.WsfeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Pasa las facturas EMITIDA a FACTURADA pidiendo el CAE a ARCA (entorno de homologacion).
 *
 * Cada factura se informa a ARCA como Factura C (codigo 11) por su importe total.
 * Cada una se procesa en su PROPIA transaccion: si ARCA rechaza una (o falla la conexion),
 * esa queda EMITIDA con el motivo en motivoRechazo y las demas siguen su curso.
 * Asi nunca se pierde un CAE que ARCA ya otorgo por culpa de un error en otra factura.
 */
@Service
@RequiredArgsConstructor
public class FacturacionArcaService {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final FacturaVentaRepository facturaVentaRepository;
    private final WsfeService wsfeService;
    private final TransactionTemplate transaccion;   // lo crea Spring Boot automaticamente

    /**
     * Factura en ARCA todas las facturas EMITIDA, de la mas vieja a la mas nueva.
     * synchronized: evita que dos clics simultaneos manden la misma factura dos veces.
     */
    public synchronized ResultadoFacturacionDTO facturarEmitidas() {
        List<Long> ids = facturaVentaRepository.buscarIdsPorEstado(FacturaService.ESTADO_EMITIDA);

        List<ResultadoFacturacionDTO.Detalle> detalle = new ArrayList<>();
        for (Long id : ids) {
            detalle.add(facturarUna(id));
        }

        int facturadas = (int) detalle.stream().filter(d -> "A".equals(d.resultado())).count();
        return new ResultadoFacturacionDTO(facturadas, detalle.size() - facturadas, detalle);
    }

    private ResultadoFacturacionDTO.Detalle facturarUna(Long id) {
        try {
            // Lectura, llamada a ARCA y guardado dentro de una misma transaccion corta
            return transaccion.execute(estado -> {
                FacturaVenta f = facturaVentaRepository.findById(id).orElseThrow();
                if (!FacturaService.ESTADO_EMITIDA.equals(f.getEstado())) {
                    // Cambio de estado mientras tanto (ej. alguien la anulo): no se toca
                    return new ResultadoFacturacionDTO.Detalle(f.getId(), f.getNumero(), "R", null, null,
                            "Ya no estaba EMITIDA (estado actual: " + f.getEstado() + ")");
                }

                WsfeService.ResultadoFactura r = wsfeService.emitirFacturaC(BigDecimal.valueOf(f.getImporteTotal()));
                f.setResultadoAfip(r.resultado());

                if ("A".equals(r.resultado())) {
                    f.setEstado(FacturaService.ESTADO_FACTURADA);
                    f.setCae(r.cae());
                    f.setCaeFechaVencimiento(aDate(r.vencimientoCae()));
                    f.setNumeroArca(r.numero());
                    f.setFechaFacturacion(LocalDateTime.now(ZONA));
                    f.setMotivoRechazo(null);
                    return new ResultadoFacturacionDTO.Detalle(f.getId(), f.getNumero(), "A",
                            r.numero(), r.cae(), null);
                }

                String motivo = r.observaciones().isEmpty()
                        ? "ARCA rechazo el comprobante sin indicar el motivo"
                        : String.join(" | ", r.observaciones());
                f.setMotivoRechazo(recortar(motivo));
                return new ResultadoFacturacionDTO.Detalle(f.getId(), f.getNumero(), "R", null, null, motivo);
            });
        } catch (RuntimeException e) {
            // Error de conexion, de certificado o de validacion de ARCA: se guarda el motivo
            // en otra transaccion (la anterior se deshizo) y la factura sigue EMITIDA.
            String motivo = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            Long numero = transaccion.execute(estado -> facturaVentaRepository.findById(id).map(f -> {
                f.setResultadoAfip("R");
                f.setMotivoRechazo(recortar(motivo));
                return f.getNumero();
            }).orElse(null));
            return new ResultadoFacturacionDTO.Detalle(id, numero, "R", null, null, motivo);
        }
    }

    /** ARCA devuelve el vencimiento del CAE como "yyyyMMdd". */
    private static Date aDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.isBlank()) {
            return null;
        }
        LocalDate fecha = LocalDate.parse(yyyymmdd, DateTimeFormatter.BASIC_ISO_DATE);
        return java.sql.Date.valueOf(fecha);
    }

    /** La columna es VARCHAR(255): se recortan los mensajes largos de ARCA. */
    private static String recortar(String texto) {
        return texto.length() <= 255 ? texto : texto.substring(0, 252) + "...";
    }
}
