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
        int pv = props.puntoVenta();
        long numero = ultimoAutorizado(pv, FACTURA_C) + 1;
        String fecha = LocalDate.now(ZONA).format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String importe = total.setScale(2, RoundingMode.HALF_UP).toPlainString();

        // El orden de los campos importa: es el que define el WSDL
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
                    </ar:FECAEDetRequest>
                  </ar:FeDetReq>
                </ar:FeCAEReq>
                """.formatted(pv, FACTURA_C, numero, numero, fecha, importe, importe);

        Document doc = llamar("FECAESolicitar", body);

        List<String> obs = new ArrayList<>(SoapClient.codeMsgList(doc, "Obs"));
        String cae = SoapClient.text(doc, "CAE");
        return new ResultadoFactura(
                SoapClient.text(doc, "Resultado"), pv, FACTURA_C, numero,
                cae == null || cae.isBlank() ? null : cae,
                SoapClient.text(doc, "CAEFchVto"),
                obs);
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
