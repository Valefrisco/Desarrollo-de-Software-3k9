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
@Table(name = "ARTICULOSS")
public class Articulo extends AuditoriaApp{
    @ManyToOne
    @JoinColumn(name = "Rubro_id")
    private Rubro rubro;
    @Column(nullable = false)
    private String codigo;
    @Column(nullable = false)
    private String denominacion;
    @ManyToOne
    @JoinColumn(name = "Marca_id")
    private Marca marca;




}
