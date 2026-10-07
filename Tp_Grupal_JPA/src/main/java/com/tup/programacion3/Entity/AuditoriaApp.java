package com.tup.programacion3.Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;


import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AuditoriaApp extends EntityId {
    @Column(nullable = false)
    protected LocalDateTime fechaAlta;
    protected LocalDateTime fechaBaja;
    protected LocalDateTime fechaaModificar;
    @ManyToOne
    @JoinColumn(name = "usuarioCarga_id " , nullable = false)
    protected Usuario usuarioCarga;
    @ManyToOne
    protected Usuario usuarioBaja;
    @ManyToOne
    @JoinColumn(nullable = false)
    protected Usuario usuarioModificacion;







}
