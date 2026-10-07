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
@Table(name = "LISTAPRECIO")
public class ListaPrecio extends AuditoriaApp{

    @Column(nullable = false)
    private String codigo;
    @Column(nullable = false)
    private String denominacion;






}
