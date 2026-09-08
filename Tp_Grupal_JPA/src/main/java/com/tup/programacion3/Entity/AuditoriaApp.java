package com.tup.programacion3.Entity;
import jakarta.persistence.*;

import java.util.Date;

@MappedSuperclass
public abstract class AuditoriaApp extends EntityId {
    @Column(nullable = false)
    protected Date fechaAlta;
    protected Date fechaBaja;
    @Column(nullable = false)
    protected Date fechaaModificar;
    @ManyToOne
    @JoinColumn(name = "usuarioCarga_id ")
    protected Usuario usuarioCarga;
    @ManyToOne
    protected Usuario usuarioBaja;
    @ManyToOne
    @JoinColumn(nullable = false)
    protected Usuario usuarioModificacion;

    public AuditoriaApp() {
    }

    public AuditoriaApp(Date fechaAlta, Date fechaBaja, Date fechaaModificar, Usuario usuarioModificacion, Usuario usuarioBaja, Usuario usuarioCarga) {
        super();
        this.fechaAlta = fechaAlta;
        this.fechaBaja = fechaBaja;
        this.fechaaModificar = fechaaModificar;
        this.usuarioModificacion = usuarioModificacion;
        this.usuarioBaja = usuarioBaja;
        this.usuarioCarga = usuarioCarga;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Usuario getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(Usuario usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    public Usuario getUsuarioBaja() {
        return usuarioBaja;
    }

    public void setUsuarioBaja(Usuario usuarioBaja) {
        this.usuarioBaja = usuarioBaja;
    }

    public Usuario getUsuarioCarga() {
        return usuarioCarga;
    }

    public void setUsuarioCarga(Usuario usuarioCarga) {
        this.usuarioCarga = usuarioCarga;
    }

    public Date getFechaaModificar() {
        return fechaaModificar;
    }

    public void setFechaaModificar(Date fechaaModificar) {
        this.fechaaModificar = fechaaModificar;
    }

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }
    @Override
    public String toString() {
        return "AuditoriaApp{" +
                "fechaAlta=" + fechaAlta +
                ", fechaBaja=" + fechaBaja +
                ", fechaaModificar=" + fechaaModificar +
                ", idUsuarioCarga=" + (usuarioCarga != null ? usuarioCarga.getId() : "null") +
                ", idUsuarioBaja=" + (usuarioBaja != null ? usuarioBaja.getId() : "null") +
                ", idUsuarioModificacion=" + (usuarioModificacion != null ? usuarioModificacion.getId() : "null") +
                '}';
    }

}
