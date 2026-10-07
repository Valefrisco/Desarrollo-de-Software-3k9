package com.tup.programacion3.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CLIENTES")
public class Cliente extends  AuditoriaApp{

    @Column(nullable = false)
    private String cuitCuil;
    @Column(nullable = false)
    private String denominacion;
    @OneToOne
    @JoinColumn(nullable = false)
    private Contacto contacto;
    @OneToOne
    @JoinColumn(nullable = false)
    private Domicilio domicilio;






}
