package com.tup.programacion3.Service;

import com.tup.programacion3.DTOs.ArticuloPrecioDTO;
import com.tup.programacion3.DTOs.CatalogosDTO;
import com.tup.programacion3.DTOs.OpcionDTO;
import com.tup.programacion3.Repository.ClienteRepository;
import com.tup.programacion3.Repository.CondicionIvaRepository;
import com.tup.programacion3.Repository.ListaPrecioArticuloRepository;
import com.tup.programacion3.Repository.ListaPrecioRepository;
import com.tup.programacion3.Repository.PuntoVentaRepository;
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
                        .toList());
    }

    private static List<OpcionDTO> porId(List<OpcionDTO> opciones) {
        return opciones.stream().sorted(Comparator.comparing(OpcionDTO::id)).toList();
    }

    private static List<OpcionDTO> porNombre(List<OpcionDTO> opciones) {
        return opciones.stream().sorted(Comparator.comparing(OpcionDTO::nombre)).toList();
    }
}
