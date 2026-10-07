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
@Table(name = "RUBROS")
public class Rubro extends AuditoriaApp{

@Column(nullable = false)
    private String denominacion;
@Column(nullable = false)
    private Integer codigo;






}
