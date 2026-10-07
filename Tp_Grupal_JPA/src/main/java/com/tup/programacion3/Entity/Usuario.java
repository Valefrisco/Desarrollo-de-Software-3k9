package com.tup.programacion3.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table (name = "USUARIOS")
@Getter @Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends EntityId {
    @Column(nullable = false)
    private String usuario;
    @Column(nullable = false)
    private String clave;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String apellido;
}