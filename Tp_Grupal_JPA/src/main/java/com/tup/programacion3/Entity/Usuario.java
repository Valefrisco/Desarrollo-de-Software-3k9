package com.tup.programacion3.Entity;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table (name = "USUARIOS")
public class Usuario extends EntityId {
    @Column(nullable = false)
    private String usuario;
    @Column(nullable = false)
    private String clave;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String apellido;

    public Usuario() {
    }

    public Usuario(String usuario, String clave, String nombre, String apellido) {
        super();
        this.usuario = usuario;
        this.clave = clave;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }


    @Override
    public String toString() {
        return "Usuario{" +
                "usuario='" + usuario + '\'' +
                ", clave='" + clave + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", id=" + id +
                "} ";
    }
}
