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
 * Arma los datos del comprobante impreso (PDF) de una factura FACTURADA en ARCA.
 * Como en ARCA se informa como Factura C (codigo 11, ver WsfeService), el impreso tambien
 * es una Factura C: emisor monotributista y total sin IVA discriminado.
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
        if (!FacturaService.ESTADO_FACTURADA.equals(f.getEstado()) || f.getCae() == null) {
            throw new IllegalStateException("Solo se imprimen facturas FACTURADA en ARCA (con CAE). La factura "
                    + f.getNumero() + " esta " + f.getEstado() + ".");
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
                f.getPuntoVentaArca() != null ? f.getPuntoVentaArca() : arcaProperties.puntoVenta(),
                f.getNumeroArca(),
                f.getFechaFacturacion(),
                f.getNumero(),
                f.getPuntoVenta().getDescripcion(),
                cliente != null ? cliente.getDenominacion() : "Consumidor Final",
                cliente != null ? cliente.getCuitCuil() : null,
                domicilioCliente,
                f.getCondicionIva().getDenominacion(),
                f.getTipoMoneda().getDenominacion(),
                renglones,
                f.getImporteTotal(),
                f.getObservaciones(),
                f.getCae(),
                f.getCaeFechaVencimiento() != null
                        ? new java.sql.Date(f.getCaeFechaVencimiento().getTime()).toLocalDate() : null);
    }
}
