package com.tup.programacion3.Service;

import com.tup.programacion3.DTOs.ComprobanteImpresionDTO;
import com.tup.programacion3.Entity.FacturaVenta;
import com.tup.programacion3.Exception.RecursoNoEncontradoException;
import com.tup.programacion3.Repository.FacturaVentaRepository;
import com.tup.programacion3.arca.ArcaProperties;
import com.tup.programacion3.arca.WsfeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma los datos del comprobante impreso (PDF) de una factura EMITIDA o FACTURADA en ARCA.
 * Como en ARCA se informa como Factura C (codigo 11, ver WsfeService), el impreso tambien
 * es una Factura C: emisor monotributista y total sin IVA discriminado.
 * Una EMITIDA todavia no tiene CAE: se imprime con su numero del sistema y el aviso de que
 * aun no fue autorizada por ARCA (no es valida como factura).
 */
@Service
@RequiredArgsConstructor
public class ComprobanteImpresionService {

    // Datos fijos del emisor (en un sistema real saldrian de una tabla de configuracion)
    private static final String EMISOR_RAZON_SOCIAL = "Facturación 3K9";
    private static final String EMISOR_CONDICION_IVA = "Responsable Monotributo";

    private final FacturaVentaRepository facturaVentaRepository;
    private final ArcaProperties arcaProperties;

    @Transactional(readOnly = true)
    public ComprobanteImpresionDTO obtener(Long id) {
        FacturaVenta f = facturaVentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura con id " + id + " no existe"));
        boolean facturada = FacturaService.ESTADO_FACTURADA.equals(f.getEstado()) && f.getCae() != null;
        boolean emitida = FacturaService.ESTADO_EMITIDA.equals(f.getEstado());
        if (!facturada && !emitida) {
            throw new IllegalStateException("Solo se imprimen facturas EMITIDA o FACTURADA. La factura "
                    + (f.getNumero() != null ? f.getNumero() + " " : "") + "esta " + f.getEstado() + ".");
        }

        var renglones = f.getDetalles().stream()
                .map(d -> new ComprobanteImpresionDTO.Renglon(
                        d.getListaPrecioArticulo().getArticulo().getCodigo(),
                        d.getDescripcion(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPorcentajeBonificacion(),
                        d.getImporteSubtotal()))
                .toList();

        var cliente = f.getCliente();
        String domicilioCliente = cliente != null && cliente.getDomicilio() != null
                ? cliente.getDomicilio().getNombreCalle() + " " + cliente.getDomicilio().getNumeroCalle() : null;

        return new ComprobanteImpresionDTO(
                EMISOR_RAZON_SOCIAL,
                arcaProperties.cuit(),
                f.getPuntoVenta().getDomicilioComercial(),
                EMISOR_CONDICION_IVA,
                "FACTURA",
                "C",
                String.format("%03d", WsfeService.FACTURA_C),
                f.getEstado(),
                facturada ? (f.getPuntoVentaArca() != null ? f.getPuntoVentaArca() : arcaProperties.puntoVenta()) : null,
                facturada ? f.getNumeroArca() : null,
                facturada ? f.getFechaFacturacion() : null,
                f.getNumero(),
                f.getPuntoVenta().getNumero(),
                f.getPuntoVenta().getDescripcion(),
                f.getFechaEmision(),
                cliente != null ? cliente.getDenominacion() : "Consumidor Final",
                cliente != null ? cliente.getCuitCuil() : null,
                domicilioCliente,
                f.getCondicionIva().getDenominacion(),
                f.getTipoMoneda().getDenominacion(),
                renglones,
                f.getImporteTotal(),
                f.getObservaciones(),
                facturada ? f.getCae() : null,
                facturada && f.getCaeFechaVencimiento() != null
                        ? new java.sql.Date(f.getCaeFechaVencimiento().getTime()).toLocalDate() : null);
    }
}
