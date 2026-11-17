package com.tup.programacion3.arca;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Utilidades mínimas para enviar SOAP 1.1 y leer la respuesta XML. */
final class SoapClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private SoapClient() {}

    static String post(String url, String soapAction, String envelope) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .header("SOAPAction", "\"" + soapAction + "\"")
                    .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                String detalle = response.body();
                try {
                    String fault = text(parse(detalle), "faultstring");
                    if (fault != null) detalle = fault;
                } catch (Exception ignored) { }
                throw new ArcaException("HTTP " + response.statusCode() + " desde " + url + ": " + detalle);
            }
            return response.body();
        } catch (ArcaException e) {
            throw e;
        } catch (Exception e) {
            throw new ArcaException("No se pudo conectar con " + url + ": " + e.getMessage(), e);
        }
    }

    static Document parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ArcaException("Respuesta XML inválida: " + e.getMessage(), e);
        }
    }

    /** Texto del primer elemento con ese nombre local (ignora namespaces), o null. */
    static String text(Document doc, String localName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent().trim();
    }

    /** Lista "Code - Msg" de todos los elementos con ese nombre (Err, Obs, Evt). */
    static List<String> codeMsgList(Document doc, String localName) {
        List<String> result = new ArrayList<>();
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            NodeList code = e.getElementsByTagNameNS("*", "Code");
            NodeList msg = e.getElementsByTagNameNS("*", "Msg");
            result.add((code.getLength() > 0 ? code.item(0).getTextContent() : "?") + " - "
                    + (msg.getLength() > 0 ? msg.item(0).getTextContent() : ""));
        }
        return result;
    }
}
