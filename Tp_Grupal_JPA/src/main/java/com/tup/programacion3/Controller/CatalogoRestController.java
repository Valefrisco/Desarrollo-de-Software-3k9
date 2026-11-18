package com.tup.programacion3.Controller;

import com.tup.programacion3.DTOs.CatalogosDTO;
import com.tup.programacion3.Service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
