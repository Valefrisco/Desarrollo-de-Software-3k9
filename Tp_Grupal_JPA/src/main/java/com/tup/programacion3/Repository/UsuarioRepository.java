package com.tup.programacion3.Repository;

import com.tup.programacion3.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Data arma la consulta a partir del nombre del metodo:
    // SELECT u FROM Usuario u WHERE u.usuario = :usuario
    Optional<Usuario> findByUsuario(String usuario);
}
