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
@Table(name = "DOMICILIOS")
public class Domicilio extends EntityId{
    private String nombreCalle;
    private String numeroCalle;






}
