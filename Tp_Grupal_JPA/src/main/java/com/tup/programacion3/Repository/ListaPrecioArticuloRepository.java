package com.tup.programacion3.Repository;

import com.tup.programacion3.Entity.ListaPrecioArticulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListaPrecioArticuloRepository extends JpaRepository<ListaPrecioArticulo, Long> {

    // Precio de un articulo dentro de una lista de precios.
    // Equivale a: SELECT lpa FROM ListaPrecioArticulo lpa
    //             WHERE lpa.listaPrecio.id = :listaPrecioId AND lpa.articulo.id = :articuloId
    Optional<ListaPrecioArticulo> findByListaPrecioIdAndArticuloId(Long listaPrecioId, Long articuloId);
}
