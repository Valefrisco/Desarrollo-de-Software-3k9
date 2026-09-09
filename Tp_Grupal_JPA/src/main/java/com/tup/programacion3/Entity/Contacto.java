package com.tup.programacion3.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CONTACTOS")
public class Contacto extends  EntityId{
    private String email;
    private String telefono;
    private String celular;

    public Contacto() {
    }

    public Contacto(String email, String celular, String telefono) {
        this.email = email;
        this.celular = celular;
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return "Contacto{" +
                "email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", celular='" + celular + '\'' +
                "} " + super.toString();
    }
}
