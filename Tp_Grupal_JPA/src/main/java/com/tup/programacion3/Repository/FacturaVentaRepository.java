package com.tup.programacion3.Repository;

import com.tup.programacion3.Entity.FacturaVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacturaVentaRepository extends JpaRepository<FacturaVenta, Long> {

    // Ultimo numero usado en un punto de venta (null si todavia no tiene facturas).
    // Se usa para numerar la proxima factura de forma correlativa.
    @Query("SELECT MAX(f.numero) FROM FacturaVenta f WHERE f.puntoVenta.id = :puntoVentaId")
    Long buscarUltimoNumero(@Param("puntoVentaId") Long puntoVentaId);
}
