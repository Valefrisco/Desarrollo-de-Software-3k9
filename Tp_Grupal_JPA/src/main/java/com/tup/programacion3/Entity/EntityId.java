package com.tup.programacion3.Entity;
import jakarta.persistence.*;

import java.util.Objects;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class EntityId {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;







}


