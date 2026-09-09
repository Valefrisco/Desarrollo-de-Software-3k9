package com.tup.programacion3.Entity;

import jakarta.persistence.*;

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

    public Cliente() {
    }

    public Cliente(String cuitCuil, Domicilio domicilio, Contacto contacto, String denominacion) {
        this.cuitCuil = cuitCuil;
        this.domicilio = domicilio;
        this.contacto = contacto;
        this.denominacion = denominacion;
    }

    public String getCuitCuil() {
        return cuitCuil;
    }

    public void setCuitCuil(String cuitCuil) {
        this.cuitCuil = cuitCuil;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Contacto getContacto() {
        return contacto;
    }

    public void setContacto(Contacto contacto) {
        this.contacto = contacto;
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "cuitCuil='" + cuitCuil + '\'' +
                ", denominacion='" + denominacion + '\'' +
                ", contacto_id=" + (contacto != null ? contacto.getId() : "null") +    // aca solo saco el id de contacto y domicilio
                ", domicilio_id=" + (domicilio != null ? domicilio.getId() : "null") + // para que no se haga despelote
                "} " + super.toString();
    }
}
