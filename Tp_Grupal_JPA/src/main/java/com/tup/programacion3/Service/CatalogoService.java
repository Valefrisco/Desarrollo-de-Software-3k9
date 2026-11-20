package com.tup.programacion3.Service;

import com.tup.programacion3.DTOs.ArticuloExplorerDTO;
import com.tup.programacion3.DTOs.ArticuloPrecioDTO;
import com.tup.programacion3.DTOs.CatalogosDTO;
import com.tup.programacion3.DTOs.ClienteExplorerDTO;
import com.tup.programacion3.DTOs.OpcionDTO;
import com.tup.programacion3.Repository.ClienteRepository;
import com.tup.programacion3.Repository.CondicionIvaRepository;
import com.tup.programacion3.Repository.ListaPrecioArticuloRepository;
import com.tup.programacion3.Repository.ListaPrecioRepository;
import com.tup.programacion3.Repository.MarcaRepository;
import com.tup.programacion3.Repository.PuntoVentaRepository;
import com.tup.programacion3.Repository.RubroRepository;
import com.tup.programacion3.Repository.TipoMonedaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Arma las listas de opciones para los desplegables del formulario de factura. */
@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final PuntoVentaRepository puntoVentaRepository;
    private final ClienteRepository clienteRepository;
    private final CondicionIvaRepository condicionIvaRepository;
    private final TipoMonedaRepository tipoMonedaRepository;
    private final ListaPrecioRepository listaPrecioRepository;
    private final ListaPrecioArticuloRepository listaPrecioArticuloRepository;
    private final RubroRepository rubroRepository;
    private final MarcaRepository marcaRepository;

    @Transactional(readOnly = true)
    public CatalogosDTO obtenerCatalogos() {
        // Los catalogos fijos van por ID (la primera opcion, la que queda elegida por
        // defecto, es la de ID 1: punto de venta 1, pesos, consumidor final, lista mayorista).
        // Clientes y articulos van por nombre, para encontrarlos rapido.
        return new CatalogosDTO(
                porId(puntoVentaRepository.findAll().stream()
                        .map(pv -> new OpcionDTO(pv.getId(), pv.getNumero() + " - " + pv.getDescripcion()))
                        .toList()),
                porNombre(clienteRepository.findAll().stream()
                        .map(c -> new OpcionDTO(c.getId(), c.getDenominacion()))
                        .toList()),
                porId(condicionIvaRepository.findAll().stream()
                        .map(ci -> new OpcionDTO(ci.getId(), ci.getDenominacion()))
                        .toList()),
                porId(tipoMonedaRepository.findAll().stream()
                        .map(m -> new OpcionDTO(m.getId(), m.getDenominacion() + " (" + m.getSimbolo() + ")"))
                        .toList()),
                porId(listaPrecioRepository.findAll().stream()
                        .map(l -> new OpcionDTO(l.getId(), l.getDenominacion()))
                        .toList()),
                listaPrecioArticuloRepository.findAll().stream()
                        .map(lpa -> new ArticuloPrecioDTO(
                                lpa.getListaPrecio().getId(),
                                lpa.getArticulo().getId(),
                                lpa.getArticulo().getDenominacion(),
                                lpa.getPrecioVenta()))
                        .sorted(Comparator.comparing(ArticuloPrecioDTO::nombre))
                        .toList(),
                porNombre(rubroRepository.findAll().stream()
                        .map(r -> new OpcionDTO(r.getId(), r.getDenominacion()))
                        .toList()),
                porNombre(marcaRepository.findAll().stream()
                        .map(m -> new OpcionDTO(m.getId(), m.getDenominacion()))
                        .toList()));
    }

    /** Explorador de clientes: razon social o CUIT que contengan el texto (vacio = todos). */
    @Transactional(readOnly = true)
    public List<ClienteExplorerDTO> buscarClientes(String texto) {
        return clienteRepository.buscarPorTexto(patron(texto)).stream()
                .map(c -> new ClienteExplorerDTO(
                        c.getId(),
                        c.getDenominacion(),
                        c.getCuitCuil(),
                        c.getContacto() != null ? c.getContacto().getEmail() : null,
                        c.getContacto() != null ? c.getContacto().getTelefono() : null,
                        c.getDomicilio() != null
                                ? c.getDomicilio().getNombreCalle() + " " + c.getDomicilio().getNumeroCalle() : null))
                .toList();
    }

    /** Explorador de articulos: los de una lista de precios, filtrados por texto, rubro y marca. */
    @Transactional(readOnly = true)
    public List<ArticuloExplorerDTO> buscarArticulos(Long listaPrecioId, String texto, Long rubroId, Long marcaId) {
        if (listaPrecioId == null) {
            throw new IllegalArgumentException("Falta el parametro listaPrecioId");
        }
        return listaPrecioArticuloRepository.buscarParaExplorador(listaPrecioId, patron(texto),
                        rubroId != null ? rubroId : 0L, marcaId != null ? marcaId : 0L).stream()
                .map(lpa -> new ArticuloExplorerDTO(
                        lpa.getArticulo().getId(),
                        lpa.getArticulo().getCodigo(),
                        lpa.getArticulo().getDenominacion(),
                        lpa.getArticulo().getRubro() != null ? lpa.getArticulo().getRubro().getDenominacion() : null,
                        lpa.getArticulo().getMarca() != null ? lpa.getArticulo().getMarca().getDenominacion() : null,
                        lpa.getPrecioVenta()))
                .toList();
    }

    /** "%texto%" en minusculas; "%%" (todos) si no hay texto. */
    private static String patron(String texto) {
        return "%" + (texto == null ? "" : texto.trim().toLowerCase()) + "%";
    }

    private static List<OpcionDTO> porId(List<OpcionDTO> opciones) {
        return opciones.stream().sorted(Comparator.comparing(OpcionDTO::id)).toList();
    }

    private static List<OpcionDTO> porNombre(List<OpcionDTO> opciones) {
        return opciones.stream().sorted(Comparator.comparing(OpcionDTO::nombre)).toList();
    }
}
