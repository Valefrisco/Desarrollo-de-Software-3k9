package com.tup.programacion3.Service;

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

    public static final String ESTADO_EMITIDA = "EMITIDA";
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

    // =====================================================================
    // CREAR
    // =====================================================================

    @Transactional
    public FacturaReporteDTO crear(FacturaRequestDTO dto) {
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

        // 2) Datos que asigna el servidor (no vienen en el request)
        Long ultimoNumero = facturaVentaRepository.buscarUltimoNumero(puntoVenta.getId());
        factura.setNumero(ultimoNumero == null ? 1L : ultimoNumero + 1);
        factura.setFechaEmision(ahora);
        factura.setEstado(ESTADO_EMITIDA);
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
    // ACTUALIZAR (cabecera, items y/o estado). Solo cambia lo que viene en el JSON.
    // =====================================================================

    @Transactional
    public FacturaReporteDTO actualizar(Long id, FacturaRequestDTO dto) {
        FacturaVenta factura = buscarPorId(facturaVentaRepository, id, "Factura");

        if (ESTADO_ANULADA.equals(factura.getEstado())) {
            throw new IllegalStateException("La factura " + factura.getNumero() + " esta anulada y no se puede modificar");
        }

        Usuario usuario = usuarioActual();
        LocalDateTime ahora = LocalDateTime.now();

        // Cabecera (null = no se modifica). El punto de venta no se puede cambiar
        // porque el numero de factura es correlativo dentro de cada punto de venta.
        if (dto.getClienteId() != null) {
            factura.setCliente(buscarPorId(clienteRepository, dto.getClienteId(), "Cliente"));
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

        // Items: si vienen, reemplazan a los anteriores y se recalculan los importes
        if (dto.getDetalles() != null) {
            if (dto.getDetalles().isEmpty()) {
                throw new IllegalArgumentException("La factura debe tener al menos un item");
            }
            if (dto.getListaPrecioId() == null) {
                throw new IllegalArgumentException("Para modificar los items hay que indicar listaPrecioId");
            }
            factura.getDetalles().clear(); // orphanRemoval = true -> borra los renglones viejos
            cargarDetalles(factura, dto.getListaPrecioId(), dto.getDetalles());
        }

        // Estado: la unica transicion posible es EMITIDA -> ANULADA (anulacion logica)
        if (dto.getEstado() != null) {
            String nuevoEstado = dto.getEstado().trim().toUpperCase();
            if (ESTADO_ANULADA.equals(nuevoEstado)) {
                factura.setEstado(ESTADO_ANULADA);
                factura.setFechaAnulacion(new Date());
                factura.setFechaBaja(ahora);       // la anulacion es la "baja" de la factura
                factura.setUsuarioBaja(usuario);
            } else if (!ESTADO_EMITIDA.equals(nuevoEstado)) {
                throw new IllegalArgumentException("Estado no valido: " + dto.getEstado()
                        + ". Valores permitidos: " + ESTADO_EMITIDA + ", " + ESTADO_ANULADA);
            }
        }

        factura.setFechaaModificar(ahora);
        factura.setUsuarioModificacion(usuario);

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
                f.getDetalles().size());
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
