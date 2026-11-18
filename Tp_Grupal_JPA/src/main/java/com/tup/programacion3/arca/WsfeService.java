package com.tup.programacion3.arca;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Cliente del WSFEv1: último comprobante autorizado y solicitud de CAE. */
@Service
public class WsfeService {

    private static final String NS = "http://ar.gov.afip.dif.FEV1/";
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    public static final int FACTURA_C = 11;
    public static final int NOTA_CREDITO_C = 13;

    private final ArcaProperties props;
    private final WsaaService wsaa;

    public record ResultadoFactura(String resultado, int puntoVenta, int tipo, long numero,
                                   String cae, String vencimientoCae, List<String> observaciones) {}

    public WsfeService(ArcaProperties props, WsaaService wsaa) {
        this.props = props;
        this.wsaa = wsaa;
    }

    public long ultimoAutorizado(int puntoVenta, int tipoComprobante) {
        String body = auth()
                + "<ar:PtoVta>" + puntoVenta + "</ar:PtoVta>"
                + "<ar:CbteTipo>" + tipoComprobante + "</ar:CbteTipo>";
        Document doc = llamar("FECompUltimoAutorizado", body);
        return Long.parseLong(SoapClient.text(doc, "CbteNro"));
    }

    /**
     * Emite una Factura C a Consumidor Final por el total indicado (sin IVA discriminado).
     * Si ARCA la rechaza, devuelve resultado "R" con las observaciones.
     */
    public synchronized ResultadoFactura emitirFacturaC(BigDecimal total) {
        return solicitarCae(FACTURA_C, total, "");
    }

    /**
     * Emite una Nota de Credito C (codigo 13) por el total indicado, asociada a una Factura C
     * ya autorizada. Es la forma de "anular" en ARCA una factura que tiene CAE: el comprobante
     * original no se borra, la nota de credito lo compensa.
     *
     * @param ptoVtaFactura punto de venta de ARCA con el que se emitio la factura
     * @param numeroFactura numero de la factura en ARCA
     * @param fechaFactura  fecha con la que se emitio la factura en ARCA
     */
    public synchronized ResultadoFactura emitirNotaCreditoC(BigDecimal total, int ptoVtaFactura,
                                                           long numeroFactura, LocalDate fechaFactura) {
        // CbtesAsoc: el comprobante que anula esta nota de credito (ARCA lo exige en las NC)
        String asociado = """
                      <ar:CbtesAsoc>
                        <ar:CbteAsoc>
                          <ar:Tipo>%d</ar:Tipo>
                          <ar:PtoVta>%d</ar:PtoVta>
                          <ar:Nro>%d</ar:Nro>
                          <ar:Cuit>%d</ar:Cuit>
                          <ar:CbteFch>%s</ar:CbteFch>
                        </ar:CbteAsoc>
                      </ar:CbtesAsoc>
                """.formatted(FACTURA_C, ptoVtaFactura, numeroFactura, props.cuit(),
                fechaFactura.format(DateTimeFormatter.BASIC_ISO_DATE));
        return solicitarCae(NOTA_CREDITO_C, total, asociado);
    }

    /**
     * Pide el CAE (FECAESolicitar) para un comprobante C a Consumidor Final.
     * El numero es el ultimo autorizado para ese tipo + 1 (cada tipo tiene su propia numeracion).
     * Los metodos publicos que lo llaman son synchronized: entre consultar el ultimo numero
     * y pedir el CAE no se puede meter otro pedido que use el mismo numero.
     *
     * @param asociado XML de CbtesAsoc (vacio si no hay comprobante asociado)
     */
    private ResultadoFactura solicitarCae(int tipo, BigDecimal total, String asociado) {
        int pv = props.puntoVenta();
        long numero = ultimoAutorizado(pv, tipo) + 1;
        String fecha = LocalDate.now(ZONA).format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String importe = total.setScale(2, RoundingMode.HALF_UP).toPlainString();

        // El orden de los campos importa: es el que define el WSDL (CbtesAsoc va despues de CondicionIVAReceptorId)
        String body = auth() + """
                <ar:FeCAEReq>
                  <ar:FeCabReq>
                    <ar:CantReg>1</ar:CantReg>
                    <ar:PtoVta>%d</ar:PtoVta>
                    <ar:CbteTipo>%d</ar:CbteTipo>
                  </ar:FeCabReq>
                  <ar:FeDetReq>
                    <ar:FECAEDetRequest>
                      <ar:Concepto>1</ar:Concepto>
                      <ar:DocTipo>99</ar:DocTipo>
                      <ar:DocNro>0</ar:DocNro>
                      <ar:CbteDesde>%d</ar:CbteDesde>
                      <ar:CbteHasta>%d</ar:CbteHasta>
                      <ar:CbteFch>%s</ar:CbteFch>
                      <ar:ImpTotal>%s</ar:ImpTotal>
                      <ar:ImpTotConc>0</ar:ImpTotConc>
                      <ar:ImpNeto>%s</ar:ImpNeto>
                      <ar:ImpOpEx>0</ar:ImpOpEx>
                      <ar:ImpTrib>0</ar:ImpTrib>
                      <ar:ImpIVA>0</ar:ImpIVA>
                      <ar:MonId>PES</ar:MonId>
                      <ar:MonCotiz>1</ar:MonCotiz>
                      <ar:CondicionIVAReceptorId>5</ar:CondicionIVAReceptorId>
                %s    </ar:FECAEDetRequest>
                  </ar:FeDetReq>
                </ar:FeCAEReq>
                """.formatted(pv, tipo, numero, numero, fecha, importe, importe, asociado);

        Document doc = llamar("FECAESolicitar", body);

        List<String> obs = new ArrayList<>(SoapClient.codeMsgList(doc, "Obs"));
        String cae = SoapClient.text(doc, "CAE");
        return new ResultadoFactura(
                SoapClient.text(doc, "Resultado"), pv, tipo, numero,
                cae == null || cae.isBlank() ? null : cae,
                SoapClient.text(doc, "CAEFchVto"),
                obs);
    }

    /** Punto de venta de ARCA configurado (arca.punto-venta). */
    public int puntoVenta() {
        return props.puntoVenta();
    }

    /** Sirve para probar conectividad sin autenticación. */
    public String dummy() {
        Document doc = llamarSinValidar("FEDummy", "");
        return "AppServer=" + SoapClient.text(doc, "AppServer")
                + ", DbServer=" + SoapClient.text(doc, "DbServer")
                + ", AuthServer=" + SoapClient.text(doc, "AuthServer");
    }

    // ------------------------------------------------------------------

    private String auth() {
        WsaaService.Ticket t = wsaa.getTicket();
        return "<ar:Auth><ar:Token>" + t.token() + "</ar:Token><ar:Sign>" + t.sign()
                + "</ar:Sign><ar:Cuit>" + props.cuit() + "</ar:Cuit></ar:Auth>";
    }

    private Document llamar(String metodo, String body) {
        Document doc = llamarSinValidar(metodo, body);
        List<String> errores = SoapClient.codeMsgList(doc, "Err");
        if (!errores.isEmpty()) {
            throw new ArcaException(metodo + " rechazado por ARCA: " + String.join(" | ", errores));
        }
        return doc;
    }

    private Document llamarSinValidar(String metodo, String body) {
        String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:ar=\"" + NS + "\"><soapenv:Header/><soapenv:Body>"
                + "<ar:" + metodo + ">" + body + "</ar:" + metodo + ">"
                + "</soapenv:Body></soapenv:Envelope>";
        return SoapClient.parse(SoapClient.post(props.wsfeUrl(), NS + metodo, envelope));
    }
}
