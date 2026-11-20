package com.tup.programacion3.Repository;

import com.tup.programacion3.Entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Explorador de clientes: busca el texto (ya en minusculas y con %) en la razon social o el CUIT.
    // Con texto "%%" trae todos. Se pasa siempre un patron (nunca null) para que PostgreSQL
    // pueda deducir el tipo del parametro.
    @Query("SELECT c FROM Cliente c " +
            "WHERE LOWER(c.denominacion) LIKE :patron OR LOWER(c.cuitCuil) LIKE :patron " +
            "ORDER BY c.denominacion")
    List<Cliente> buscarPorTexto(@Param("patron") String patron);
}
