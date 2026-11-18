package com.tup.programacion3.Service;

import com.tup.programacion3.DTOs.FacturaCompletaDTO;
import com.tup.programacion3.DTOs.FacturaDetalleRequestDTO;
import com.tup.programacion3.DTOs.FacturaReporteDTO;
import com.tup.programacion3.DTOs.FacturaRequestDTO;
import com.tup.programacion3.Entity.FacturaVenta;
import com.tup.programacion3.Entity.FacturaVentaDetalle;
import com.tup.programacion3.Entity.ListaPrecioArticulo;
import com.tup.programacion3.Entity.PuntoVenta;
import com.tup.programacion3.Entity.Usuario;
import com.tup.programacion3.Exception.RecursoNoEncontradoException;
import com.tup.programacion3.Repository.ClienteRepository;
import com.tup.programacion3.Repository.CondicionIvaRepository;
import com.tup.programacion3.Repository.FacturaReporteRepository;
import com.tup.programacion3.Repository.FacturaVentaRepository;
import com.tup.programacion3.Repository.ListaPrecioArticuloRepository;
import com.tup.programacion3.Repository.PuntoVentaRepository;
import com.tup.programacion3.Repository.TipoMonedaRepository;
import com.tup.programacion3.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturaService {

    // Ciclo de vida: BORRADOR -> EMITIDA -> FACTURADA
    //                                    \-> ANULADA
    // FACTURADA = ARCA autorizo el comprobante y le dio CAE (ver FacturacionArcaService).
    // Desde ahi la factura es de solo lectura: no se edita, no se anula y no se borra.
    public static final String ESTADO_BORRADOR = "BORRADOR";
    public static final String ESTADO_EMITIDA = "EMITIDA";
    public static final String ESTADO_FACTURADA = "FACTURADA";
    public static final String ESTADO_ANULADA = "ANULADA";

    // Los precios de lista son finales (IVA incluido). Es la misma regla que usa
    // el DataLoader: subtotal 5000 = neto 4132,23 + IVA 867,77.
    private static final double TASA_IVA = 0.21;

    // No hay login en el sistema: todas las operaciones quedan auditadas a nombre
    // de este usuario (lo crea el DataLoader). En un sistema real saldria de Spring Security.
    private static final String USUARIO_SISTEMA = "admin";

    private final FacturaReporteRepository facturaReporteRepository;
    private final FacturaVentaRepository facturaVentaRepository;
    private final PuntoVentaRepository puntoVentaRepository;
    private final ClienteRepository clienteRepository;
    private final CondicionIvaRepository condicionIvaRepository;
    private final TipoMonedaRepository tipoMonedaRepository;
    private final ListaPrecioArticuloRepository listaPrecioArticuloRepository;
    private final UsuarioRepository usuarioRepository;

    // =====================================================================
    // LECTURA (TP anterior)
    // =====================================================================

    @Transactional(readOnly = true)
    public List<FacturaReporteDTO> buscarFacturasFiltradas(
            LocalDateTime desde, LocalDateTime hasta, String estado, Double montoMinimo) {
        return facturaReporteRepository.buscarFacturasFiltradas(desde, hasta, estado, montoMinimo);
    }

    /** Factura completa (IDs y renglones) para cargarla en el formulario de edicion. */
    @Transactional(readOnly = true)
    public FacturaCompletaDTO obtener(Long id) {
        FacturaVenta f = buscarPorId(facturaVentaRepository, id, "Factura");

        List<FacturaDetalleRequestDTO> renglones = f.getDetalles().stream()
                .map(d -> new FacturaDetalleRequestDTO(
                        d.getListaPrecioArticulo().getArticulo().getId(),
                        d.getCantidad(),
                        d.getPorcentajeBonificacion()))
                .toList();

        // La factura no guarda la lista de precios: se deduce de sus renglones,
        // porque todos se cargan con la misma lista (ver cargarDetalles).
        Long listaPrecioId = f.getDetalles().isEmpty() ? null
                : f.getDetalles().getFirst().getListaPrecioArticulo().getListaPrecio().getId();

        return new FacturaCompletaDTO(
                f.getId(),
                f.getNumero(),
                f.getEstado(),
                f.getPuntoVenta().getId(),
                f.getCliente() != null ? f.getCliente().getId() : null,
                f.getCondicionIva().getId(),
                f.getTipoMoneda().getId(),
                listaPrecioId,
                f.getObservaciones(),
                f.getImporteTotal(),
                renglones,
                f.getCae(),
                f.getCaeFechaVencimiento() != null
                        ? new java.sql.Date(f.getCaeFechaVencimiento().getTime()).toLocalDate() : null,
                f.getNumeroArca(),
                f.getFechaFacturacion(),
                f.getMotivoRechazo());
    }

    // =====================================================================
    // CREAR (emitida en el momento, o como BORRADOR si el request lo pide)
    // =====================================================================

    @Transactional
    public FacturaReporteDTO crear(FacturaRequestDTO dto) {
        // Sin estado (como hasta ahora) o "EMITIDA" -> se emite. "BORRADOR" -> queda sin emitir.
        String estadoInicial = normalizarEstado(dto.getEstado());
        if (estadoInicial == null) {
            estadoInicial = ESTADO_EMITIDA;
        }
        if (ESTADO_ANULADA.equals(estadoInicial)) {
            throw new IllegalArgumentException("Una factura no se puede crear anulada");
        }

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un item");
        }
        if (dto.getListaPrecioId() == null) {
            throw new IllegalArgumentException("Falta el campo listaPrecioId");
        }

        Usuario usuario = usuarioActual();
        LocalDateTime ahora = LocalDateTime.now();
        FacturaVenta factura = new FacturaVenta();

        // 1) Relaciones: se buscan por ID. Si alguna no existe -> excepcion (404).
        PuntoVenta puntoVenta = buscarPorId(puntoVentaRepository, dto.getPuntoVentaId(), "PuntoVenta");
        factura.setPuntoVenta(puntoVenta);
        factura.setCondicionIva(buscarPorId(condicionIvaRepository, dto.getCondicionIvaId(), "CondicionIva"));
        factura.setTipoMoneda(buscarPorId(tipoMonedaRepository, dto.getTipoMonedaId(), "TipoMoneda"));
        // El cliente es opcional: sin cliente la factura es a "Consumidor Final"
        if (dto.getClienteId() != null) {
            factura.setCliente(buscarPorId(clienteRepository, dto.getClienteId(), "Cliente"));
        }

        // 2) Datos que asigna el servidor (no vienen en el request).
        // El borrador no lleva numero: se numera recien al emitirlo, asi un borrador
        // descartado no deja huecos en la numeracion del punto de venta.
        factura.setFechaEmision(ahora);
        factura.setEstado(estadoInicial);
        if (ESTADO_EMITIDA.equals(estadoInicial)) {
            asignarNumero(factura);
        }
        factura.setObservaciones(dto.getObservaciones());
        factura.setImporteCobrado(0);

        // Auditoria (campos obligatorios de AuditoriaApp)
        factura.setFechaAlta(ahora);
        factura.setFechaaModificar(ahora);
        factura.setUsuarioCarga(usuario);
        factura.setUsuarioModificacion(usuario);

        // 3) Items: se arman con su precio de lista y se calculan los importes
        cargarDetalles(factura, dto.getListaPrecioId(), dto.getDetalles());

        // 4) Un solo save de la cabecera: CascadeType.ALL persiste tambien los detalles
        FacturaVenta guardada = facturaVentaRepository.save(factura);
        return aReporteDTO(guardada);
    }

    // =====================================================================
    // ACTUALIZAR. Segun el estado de la factura y lo que venga en el JSON:
    //  - BORRADOR: se edita cabecera y/o renglones (null = no cambia) y, si viene
    //    "estado": "EMITIDA", ademas se emite.
    //  - EMITIDA: se edita cabecera y/o renglones conservando su numero (no puede
    //    cambiar de punto de venta), o se anula ("estado": "ANULADA" + motivo).
    //  - ANULADA: no se puede modificar.
    // =====================================================================

    @Transactional
    public FacturaReporteDTO actualizar(Long id, FacturaRequestDTO dto) {
        FacturaVenta factura = buscarPorId(facturaVentaRepository, id, "Factura");
        String estadoActual = factura.getEstado();

        // 0) Una factura con CAE ya esta registrada en ARCA: es de solo lectura
        if (ESTADO_FACTURADA.equals(estadoActual)) {
            throw new IllegalStateException(mensajeFacturada(factura, "modificar ni anular"));
        }

        String nuevoEstado = normalizarEstado(dto.getEstado());

        Usuario usuario = usuarioActual();
        LocalDateTime ahora = LocalDateTime.now();

        // 1) Anulacion: solo cambia el estado y guarda el motivo. No puede venir
        //    mezclada con cambios de cabecera o renglones.
        if (ESTADO_ANULADA.equals(nuevoEstado)) {
            if (dto.traeCambiosDeContenido()) {
                throw new IllegalArgumentException(
                        "Una anulacion solo admite estado y observaciones (motivo): no puede modificar la cabecera ni los renglones");
            }
            if (ESTADO_ANULADA.equals(estadoActual)) {
                throw new IllegalStateException("La factura " + factura.getNumero() + " ya esta anulada");
            }
            if (ESTADO_BORRADOR.equals(estadoActual)) {
                throw new IllegalStateException("Un borrador no se anula porque nunca se emitio: se puede eliminar");
            }
            factura.setEstado(ESTADO_ANULADA);
            factura.setFechaAnulacion(new Date());
            factura.setFechaBaja(ahora);       // la anulacion es la "baja" de la factura
            factura.setUsuarioBaja(usuario);
            if (dto.getObservaciones() != null) {
                factura.setObservaciones(dto.getObservaciones());   // motivo de la anulacion
            }
            registrarModificacion(factura, usuario, ahora);
            return aReporteDTO(factura);
        }

        // 2) Una anulada ya no admite cambios
        if (ESTADO_ANULADA.equals(estadoActual)) {
            throw new IllegalStateException("La factura " + factura.getNumero() + " esta anulada y no se puede modificar");
        }

        boolean emitida = ESTADO_EMITIDA.equals(estadoActual);
        if (emitida && ESTADO_BORRADOR.equals(nuevoEstado)) {
            throw new IllegalStateException("La factura " + factura.getNumero() + " ya esta emitida: no puede volver a borrador");
        }
        // El numero de una emitida es correlativo dentro de su punto de venta: cambiarla
        // de punto de venta romperia la numeracion. Un borrador si puede (todavia no tiene numero).
        if (emitida && dto.getPuntoVentaId() != null
                && !dto.getPuntoVentaId().equals(factura.getPuntoVenta().getId())) {
            throw new IllegalStateException("La factura " + factura.getNumero()
                    + " ya esta emitida: no se puede cambiar su punto de venta");
        }

        // 3) Cabecera (null = no se modifica)
        if (dto.getPuntoVentaId() != null) {
            factura.setPuntoVenta(buscarPorId(puntoVentaRepository, dto.getPuntoVentaId(), "PuntoVenta"));
        }
        if (dto.isClienteIdInformado()) {
            // "clienteId": null en el JSON = pasar a Consumidor Final
            factura.setCliente(dto.getClienteId() == null ? null
                    : buscarPorId(clienteRepository, dto.getClienteId(), "Cliente"));
        }
        if (dto.getCondicionIvaId() != null) {
            factura.setCondicionIva(buscarPorId(condicionIvaRepository, dto.getCondicionIvaId(), "CondicionIva"));
        }
        if (dto.getTipoMonedaId() != null) {
            factura.setTipoMoneda(buscarPorId(tipoMonedaRepository, dto.getTipoMonedaId(), "TipoMoneda"));
        }
        if (dto.getObservaciones() != null) {
            factura.setObservaciones(dto.getObservaciones());
        }

        // 4) Items: si vienen, reemplazan a los anteriores y se recalculan los importes
        if (dto.getDetalles() != null) {
            if (dto.getDetalles().isEmpty()) {
                throw new IllegalArgumentException("La factura debe tener al menos un item");
            }
            if (dto.getListaPrecioId() == null) {
                throw new IllegalArgumentException("Para modificar los items hay que indicar listaPrecioId");
            }
            factura.getDetalles().clear(); // orphanRemoval = true -> borra los renglones viejos
            cargarDetalles(factura, dto.getListaPrecioId(), dto.getDetalles());
        } else if (dto.getListaPrecioId() != null) {
            throw new IllegalArgumentException("Para cambiar la lista de precios hay que enviar tambien los renglones");
        }

        // 5) Emitir el borrador: recien aca toma numero y fecha de emision
        //    (si ya estaba emitida, "estado": "EMITIDA" no cambia nada)
        if (!emitida && ESTADO_EMITIDA.equals(nuevoEstado)) {
            factura.setFechaEmision(ahora);
            factura.setEstado(ESTADO_EMITIDA);
            asignarNumero(factura);
        }

        registrarModificacion(factura, usuario, ahora);

        // No hace falta save(): la factura fue leida dentro de esta transaccion
        // (entidad "managed") y JPA guarda los cambios solo al hacer commit (dirty checking).
        return aReporteDTO(factura);
    }

    // =====================================================================
    // ELIMINAR (borrado fisico)
    // =====================================================================

    @Transactional
    public void eliminar(Long id) {
        FacturaVenta factura = buscarPorId(facturaVentaRepository, id, "Factura");
        if (ESTADO_FACTURADA.equals(factura.getEstado())) {
            throw new IllegalStateException(mensajeFacturada(factura, "eliminar"));
        }
        // CascadeType.ALL + orphanRemoval: se borran tambien todos sus detalles
        facturaVentaRepository.delete(factura);
    }

    // =====================================================================
    // METODOS AUXILIARES
    // =====================================================================

    /**
     * Arma un FacturaVentaDetalle por cada item del request, con el precio sacado
     * de la lista de precios, y calcula subtotal, neto, IVA y el total de la factura.
     */
    private void cargarDetalles(FacturaVenta factura, Long listaPrecioId, List<FacturaDetalleRequestDTO> items) {
        double total = 0;

        for (FacturaDetalleRequestDTO item : items) {
            if (item.getArticuloId() == null) {
                throw new IllegalArgumentException("Cada item debe indicar articuloId");
            }
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada item debe ser mayor a 0");
            }
            double bonificacion = item.getPorcentajeBonificacion() == null ? 0 : item.getPorcentajeBonificacion();
            if (bonificacion < 0 || bonificacion > 100) {
                throw new IllegalArgumentException("La bonificacion debe estar entre 0 y 100");
            }

            ListaPrecioArticulo precioLista = listaPrecioArticuloRepository
                    .findByListaPrecioIdAndArticuloId(listaPrecioId, item.getArticuloId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "El articulo " + item.getArticuloId() + " no tiene precio en la lista " + listaPrecioId));

            double precioUnitario = precioLista.getPrecioVenta();
            double subtotal = redondear(item.getCantidad() * precioUnitario * (1 - bonificacion / 100));
            double neto = redondear(subtotal / (1 + TASA_IVA));
            double iva = redondear(subtotal - neto);

            FacturaVentaDetalle detalle = new FacturaVentaDetalle();
            detalle.setListaPrecioArticulo(precioLista);
            detalle.setDescripcion(precioLista.getArticulo().getDenominacion());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setPorcentajeBonificacion(bonificacion);
            detalle.setImporteSubtotal(subtotal);
            detalle.setImporteNeto(neto);
            detalle.setImporteIva(iva);

            factura.addDetalle(detalle); // vincula los dos lados de la relacion
            total += subtotal;
        }

        factura.setImporteTotal(redondear(total));
        factura.setImporteSaldo(redondear(total - factura.getImporteCobrado()));
    }

    /** Proximo numero correlativo dentro del punto de venta de la factura. */
    private void asignarNumero(FacturaVenta factura) {
        Long ultimoNumero = facturaVentaRepository.buscarUltimoNumero(factura.getPuntoVenta().getId());
        factura.setNumero(ultimoNumero == null ? 1L : ultimoNumero + 1);
    }

    private void registrarModificacion(FacturaVenta factura, Usuario usuario, LocalDateTime ahora) {
        factura.setFechaaModificar(ahora);
        factura.setUsuarioModificacion(usuario);
    }

    /** Pasa el estado a mayusculas y valida que sea uno conocido. null si no vino. */
    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        String normalizado = estado.trim().toUpperCase();
        if (ESTADO_FACTURADA.equals(normalizado)) {
            throw new IllegalArgumentException("El estado FACTURADA no se asigna a mano: se obtiene facturando en ARCA "
                    + "(POST /api/facturas/facturar-emitidas)");
        }
        if (!List.of(ESTADO_BORRADOR, ESTADO_EMITIDA, ESTADO_ANULADA).contains(normalizado)) {
            throw new IllegalArgumentException("Estado no valido: " + estado
                    + ". Valores permitidos: " + ESTADO_BORRADOR + ", " + ESTADO_EMITIDA + ", " + ESTADO_ANULADA);
        }
        return normalizado;
    }

    /** Busca cualquier entidad por ID o lanza RecursoNoEncontradoException (404). */
    private <T> T buscarPorId(JpaRepository<T, Long> repo, Long id, String nombre) {
        if (id == null) {
            throw new IllegalArgumentException("Falta el ID de " + nombre);
        }
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(nombre + " con id " + id + " no existe"));
    }

    private Usuario usuarioActual() {
        return usuarioRepository.findByUsuario(USUARIO_SISTEMA)
                .orElseThrow(() -> new IllegalStateException("No existe el usuario '" + USUARIO_SISTEMA + "'"));
    }

    /** Convierte la entidad al DTO de salida (el mismo que devuelve el GET). */
    private FacturaReporteDTO aReporteDTO(FacturaVenta f) {
        return new FacturaReporteDTO(
                f.getId(),
                f.getNumero(),
                f.getFechaEmision(),
                f.getCliente() != null ? f.getCliente().getDenominacion() : "Consumidor Final",
                f.getCondicionIva().getDenominacion(),
                f.getPuntoVenta().getDescripcion(),
                f.getImporteTotal(),
                f.getEstado(),
                f.getDetalles().size(),
                f.getCae(),
                f.getNumeroArca());
    }

    private static String mensajeFacturada(FacturaVenta f, String accion) {
        return "La factura " + f.getNumero() + " ya fue facturada en ARCA (CAE " + f.getCae()
                + "): no se puede " + accion;
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
