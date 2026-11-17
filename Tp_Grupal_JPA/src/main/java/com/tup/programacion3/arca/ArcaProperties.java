package com.tup.programacion3.arca;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de ARCA leída desde application.properties / variables de entorno.
 * Cada desarrollador define sus propios valores (CUIT, rutas al .key y .crt).
 */
@ConfigurationProperties(prefix = "arca")
public record ArcaProperties(
        long cuit,
        String keyPath,
        String certPath,
        int puntoVenta,
        String wsaaUrl,
        String wsfeUrl
) {}
