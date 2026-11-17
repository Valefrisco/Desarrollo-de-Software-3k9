package com.tup.programacion3.arca;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/** Endpoints de prueba para homologación. Sacalos (o protegelos) antes de producción. */
@RestController
@RequestMapping("/api/arca")
public class ArcaTestController {

    private final WsfeService wsfe;
    private final ArcaProperties props;

    public ArcaTestController(WsfeService wsfe, ArcaProperties props) {
        this.wsfe = wsfe;
        this.props = props;
    }

    @GetMapping("/dummy")
    public String dummy() {
        return wsfe.dummy();
    }

    @GetMapping("/ultimo")
    public Map<String, Object> ultimo() {
        long ultimo = wsfe.ultimoAutorizado(props.puntoVenta(), WsfeService.FACTURA_C);
        return Map.of("puntoVenta", props.puntoVenta(), "tipo", "Factura C", "ultimoNumero", ultimo);
    }

    @PostMapping("/factura-prueba")
    public WsfeService.ResultadoFactura facturaPrueba(@RequestParam BigDecimal total) {
        return wsfe.emitirFacturaC(total);
    }

    @ExceptionHandler(ArcaException.class)
    public ResponseEntity<Map<String, String>> error(ArcaException e) {
        return ResponseEntity.status(502).body(Map.of("error", e.getMessage()));
    }
}
