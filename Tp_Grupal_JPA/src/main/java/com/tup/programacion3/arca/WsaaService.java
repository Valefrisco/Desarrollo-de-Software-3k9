package com.tup.programacion3.arca;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * Obtiene el Ticket de Acceso (token + sign) del WSAA para el servicio "wsfe".
 * El ticket dura ~12 h y ARCA NO entrega otro mientras haya uno vigente,
 * por eso se guarda en memoria y en un archivo (ta-cache.properties, junto al .crt).
 */
@Service
public class WsaaService {

    private static final String SERVICIO = "wsfe";
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private final ArcaProperties props;
    private Ticket ticket;

    public record Ticket(String token, String sign, OffsetDateTime vencimiento) {
        boolean vigente() {
            return vencimiento.isAfter(OffsetDateTime.now().plusMinutes(5));
        }
    }

    public WsaaService(ArcaProperties props) {
        this.props = props;
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public synchronized Ticket getTicket() {
        if (ticket != null && ticket.vigente()) return ticket;

        Ticket cacheado = leerCache();
        if (cacheado != null && cacheado.vigente()) {
            ticket = cacheado;
            return ticket;
        }

        ticket = pedirTicketNuevo();
        guardarCache(ticket);
        return ticket;
    }

    // ---------------------------------------------------------------- WSAA

    private Ticket pedirTicketNuevo() {
        try {
            PrivateKey clave = cargarClavePrivada(Path.of(props.keyPath()));
            X509Certificate cert = cargarCertificado(Path.of(props.certPath()));

            OffsetDateTime ahora = OffsetDateTime.now(ZONA);
            String tra = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <loginTicketRequest version="1.0">
                      <header>
                        <uniqueId>%d</uniqueId>
                        <generationTime>%s</generationTime>
                        <expirationTime>%s</expirationTime>
                      </header>
                      <service>%s</service>
                    </loginTicketRequest>
                    """.formatted(ahora.toEpochSecond(),
                    ahora.minusMinutes(10).format(FMT),
                    ahora.plusMinutes(10).format(FMT),
                    SERVICIO);

            // Firma CMS (PKCS#7) del TRA con la clave privada y el certificado
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(clave);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(signer, cert));
            gen.addCertificates(new JcaCertStore(List.of(cert)));
            CMSSignedData firmado = gen.generate(new CMSProcessableByteArray(tra.getBytes(StandardCharsets.UTF_8)), true);
            String cms = Base64.getEncoder().encodeToString(firmado.getEncoded());

            String envelope = """
                    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" \
                    xmlns:wsaa="http://wsaa.view.sua.dvadac.desein.afip.gov">\
                    <soapenv:Header/><soapenv:Body><wsaa:loginCms><wsaa:in0>%s</wsaa:in0></wsaa:loginCms>\
                    </soapenv:Body></soapenv:Envelope>""".formatted(cms);

            String respuesta = SoapClient.post(props.wsaaUrl(), "", envelope);

            // loginCmsReturn trae OTRO XML escapado adentro (el loginTicketResponse)
            String loginTicketResponse = SoapClient.text(SoapClient.parse(respuesta), "loginCmsReturn");
            if (loginTicketResponse == null) {
                throw new ArcaException("WSAA respondió sin loginCmsReturn: " + respuesta);
            }
            Document ta = SoapClient.parse(loginTicketResponse);
            return new Ticket(
                    SoapClient.text(ta, "token"),
                    SoapClient.text(ta, "sign"),
                    OffsetDateTime.parse(SoapClient.text(ta, "expirationTime")));
        } catch (ArcaException e) {
            if (e.getMessage() != null && e.getMessage().contains("TA valido")) {
                throw new ArcaException("ARCA dice que ya existe un ticket vigente para este certificado, pero no está en "
                        + cachePath() + ". Esperá a que venza (hasta 12 h) o generá otro certificado en WSASS.", e);
            }
            throw e;
        } catch (Exception e) {
            throw new ArcaException("Error obteniendo ticket del WSAA: " + e.getMessage(), e);
        }
    }

    private static PrivateKey cargarClavePrivada(Path path) throws Exception {
        // Acepta tanto "BEGIN PRIVATE KEY" (OpenSSL 3) como "BEGIN RSA PRIVATE KEY" (OpenSSL 1.x)
        try (Reader reader = Files.newBufferedReader(path); PEMParser parser = new PEMParser(reader)) {
            Object obj = parser.readObject();
            JcaPEMKeyConverter conv = new JcaPEMKeyConverter();
            if (obj instanceof PEMKeyPair kp) return conv.getPrivateKey(kp.getPrivateKeyInfo());
            if (obj instanceof PrivateKeyInfo pki) return conv.getPrivateKey(pki);
            throw new ArcaException("No se reconoce el formato de la clave privada en " + path);
        }
    }

    private static X509Certificate cargarCertificado(Path path) throws Exception {
        try (InputStream in = Files.newInputStream(path)) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
        }
    }

    // --------------------------------------------------------------- caché

    private Path cachePath() {
        return Path.of(props.certPath()).toAbsolutePath().resolveSibling("ta-cache.properties");
    }

    private Ticket leerCache() {
        Path path = cachePath();
        if (!Files.exists(path)) return null;
        try (Reader reader = Files.newBufferedReader(path)) {
            Properties p = new Properties();
            p.load(reader);
            return new Ticket(p.getProperty("token"), p.getProperty("sign"),
                    OffsetDateTime.parse(p.getProperty("vencimiento")));
        } catch (Exception e) {
            return null; // caché corrupta: se pide uno nuevo
        }
    }

    private void guardarCache(Ticket t) {
        try (Writer writer = Files.newBufferedWriter(cachePath())) {
            Properties p = new Properties();
            p.setProperty("token", t.token());
            p.setProperty("sign", t.sign());
            p.setProperty("vencimiento", t.vencimiento().toString());
            p.store(writer, "Ticket de acceso WSAA (wsfe) - no compartir");
        } catch (Exception e) {
            // No es fatal: el ticket sigue en memoria mientras la app esté corriendo
        }
    }
}
