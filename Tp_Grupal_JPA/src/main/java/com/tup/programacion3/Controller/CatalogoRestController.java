package com.tup.programacion3.Controller;

import com.tup.programacion3.DTOs.ArticuloExplorerDTO;
import com.tup.programacion3.DTOs.CatalogosDTO;
import com.tup.programacion3.DTOs.ClienteExplorerDTO;
import com.tup.programacion3.Service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@CrossOrigin(origins = "*")   // igual que FacturaRestController: el frontend corre en otro puerto
@RequiredArgsConstructor
public class CatalogoRestController {

    private final CatalogoService catalogoService;

    // GET /api/catalogos -> puntos de venta, clientes, condiciones IVA, monedas,
    // listas de precios y articulos con su precio, para los desplegables del formulario.
    @GetMapping
    public CatalogosDTO obtener() {
        return catalogoService.obtenerCatalogos();
    }

    // GET /api/catalogos/clientes?texto=  -> explorador de clientes.
    // texto: parte de la razon social o del CUIT (vacio = todos). Ordenados por razon social.
    @GetMapping("/clientes")
    public List<ClienteExplorerDTO> buscarClientes(@RequestParam(required = false) String texto) {
        return catalogoService.buscarClientes(texto);
    }

    // GET /api/catalogos/articulos?listaPrecioId=1&texto=&rubroId=&marcaId=  -> explorador de articulos.
    // Solo los articulos con precio en esa lista; texto busca en codigo o denominacion.
    @GetMapping("/articulos")
    public List<ArticuloExplorerDTO> buscarArticulos(
            @RequestParam Long listaPrecioId,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long rubroId,
            @RequestParam(required = false) Long marcaId) {
        return catalogoService.buscarArticulos(listaPrecioId, texto, rubroId, marcaId);
    }
}
