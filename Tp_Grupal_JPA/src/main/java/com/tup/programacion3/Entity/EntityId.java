package com.tup.programacion3.Entity;
import jakarta.persistence.*;

import java.util.Objects;

@MappedSuperclass
public abstract class EntityId {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;


    public EntityId() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EntityId entityId = (EntityId) o;
        return Objects.equals(id, entityId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EntityId{" +
                "id=" + id +
                '}';
    }
}


