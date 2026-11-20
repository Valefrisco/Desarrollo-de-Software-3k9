package com.tup.programacion3.Repository;

import com.tup.programacion3.Entity.ListaPrecioArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface ListaPrecioArticuloRepository extends JpaRepository<ListaPrecioArticulo, Long> {

    // Precio de un articulo dentro de una lista de precios.
    // Equivale a: SELECT lpa FROM ListaPrecioArticulo lpa
    //             WHERE lpa.listaPrecio.id = :listaPrecioId AND lpa.articulo.id = :articuloId
    Optional<ListaPrecioArticulo> findByListaPrecioIdAndArticuloId(Long listaPrecioId, Long articuloId);

    // Explorador de articulos: los articulos de una lista de precios, filtrados por texto
    // (codigo o denominacion, patron en minusculas con %), rubro y marca.
    // rubroId/marcaId = 0 significa "cualquiera" (se evita pasar null por PostgreSQL).
    @Query("SELECT lpa FROM ListaPrecioArticulo lpa " +
            "JOIN lpa.articulo a LEFT JOIN a.rubro r LEFT JOIN a.marca m " +
            "WHERE lpa.listaPrecio.id = :listaPrecioId " +
            "AND (LOWER(a.denominacion) LIKE :patron OR LOWER(a.codigo) LIKE :patron) " +
            "AND (:rubroId = 0 OR r.id = :rubroId) " +
            "AND (:marcaId = 0 OR m.id = :marcaId) " +
            "ORDER BY a.denominacion")
    List<ListaPrecioArticulo> buscarParaExplorador(@Param("listaPrecioId") Long listaPrecioId,
                                                  @Param("patron") String patron,
                                                  @Param("rubroId") Long rubroId,
                                                  @Param("marcaId") Long marcaId);
}
