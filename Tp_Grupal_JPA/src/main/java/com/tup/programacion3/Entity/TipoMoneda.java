package com.tup.programacion3.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "TIPOMONEDA")
public class TipoMoneda extends AuditoriaApp{

    @Column(nullable = false)
    private String codigoAfip;
    @Column(nullable = false)
    private String denominacion;
    @Column(nullable = false)
    private String simbolo;




}
