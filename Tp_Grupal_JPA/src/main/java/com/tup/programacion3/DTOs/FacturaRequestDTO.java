package com.tup.programacion3.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;



@Getter
@Setter
@NoArgsConstructor
public class FacturaRequestDTO {

    // ----- Relaciones (solo IDs) -----
    private Long puntoVentaId;
    private Long clienteId;        // opcional: null = Consumidor Final
    private Long condicionIvaId;
    private Long tipoMonedaId;
    private Long listaPrecioId;    // lista de la que se toman los precios de los articulos

    // ----- Datos de cabecera -----
    // POST: null o "EMITIDA" = se emite en el momento; "BORRADOR" = queda guardada sin emitir.
    // PUT:  "EMITIDA" = emitir un borrador; "ANULADA" = anular una emitida; null = no cambia.
    private String estado;
    private String observaciones;  // en una anulacion se usa como motivo

    // ----- Items -----
    private List<FacturaDetalleRequestDTO> detalles;

    // En un PUT, "clienteId": null significa "pasar a Consumidor Final", y que no venga
    // clienteId significa "no tocar el cliente". Con solo mirar el campo no se distinguen
    // (los dos quedan en null), asi que Jackson nos avisa por el setter si la clave vino en el JSON.
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private boolean clienteIdInformado;

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
        this.clienteIdInformado = true;
    }

    /** true si el request trae algun dato de cabecera o renglones (sin contar estado ni observaciones). */
    @JsonIgnore
    public boolean traeCambiosDeContenido() {
        return puntoVentaId != null || clienteIdInformado || condicionIvaId != null
                || tipoMonedaId != null || listaPrecioId != null || detalles != null;
    }
}
