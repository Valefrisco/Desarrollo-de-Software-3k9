package com.tup.programacion3.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.tup.programacion3.DTOs.FacturaReporteDTO;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReporteService {

    // 1. Generar TXT separado por tabulaciones (abrible en Excel)
    public byte[] generarTxtTabulado(List<FacturaReporteDTO> datos) {
        StringBuilder sb = new StringBuilder();
        // Encabezados
        sb.append("Numero\tFecha\tCliente\tCondicionIVA\tPuntoVenta\tTotal\tItems\n");

        for (FacturaReporteDTO d : datos) {
            sb.append(d.getNumeroFactura() != null ? d.getNumeroFactura() : "").append('\t')
                    .append(d.getFechaEmision() != null ? d.getFechaEmision() : "").append('\t')
                    .append(d.getClienteDenominacion() != null ? d.getClienteDenominacion() : "").append('\t')
                    .append(d.getCondicionIva() != null ? d.getCondicionIva() : "").append('\t')
                    .append(d.getPuntoVentaDescripcion() != null ? d.getPuntoVentaDescripcion() : "").append('\t')
                    .append(d.getImporteTotal() != null ? d.getImporteTotal() : "").append('\t')
                    .append(d.getCantidadItems() != null ? d.getCantidadItems() : "").append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // 2. Generar PDF con iText 7
    public byte[] generarPdf(List<FacturaReporteDTO> datos) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            document.add(new Paragraph("Reporte de Facturación").setBold().setFontSize(16));

            // Tabla de 7 columnas
            float[] columnWidths = {2, 2, 3, 2, 2, 2, 1};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.useAllAvailableWidth();

            // Headers
            table.addHeaderCell("N° Factura");
            table.addHeaderCell("Fecha");
            table.addHeaderCell("Cliente");
            table.addHeaderCell("Cond. IVA");
            table.addHeaderCell("Punto Venta");
            table.addHeaderCell("Total");
            table.addHeaderCell("Items");

            // Celdas de datos
            for (FacturaReporteDTO d : datos) {
                table.addCell(String.valueOf(d.getNumeroFactura() != null ? d.getNumeroFactura() : ""));
                table.addCell(String.valueOf(d.getFechaEmision() != null ? d.getFechaEmision() : ""));
                table.addCell(String.valueOf(d.getClienteDenominacion() != null ? d.getClienteDenominacion() : ""));
                table.addCell(String.valueOf(d.getCondicionIva() != null ? d.getCondicionIva() : ""));
                table.addCell(String.valueOf(d.getPuntoVentaDescripcion() != null ? d.getPuntoVentaDescripcion() : ""));
                table.addCell(String.valueOf(d.getImporteTotal() != null ? d.getImporteTotal() : ""));
                table.addCell(String.valueOf(d.getCantidadItems() != null ? d.getCantidadItems() : ""));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    // Método utilitario para guardar los bytes a un archivo en disco
    public void guardarEnArchivo(String rutaDestino, byte[] contenido) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
            fos.write(contenido);
        }
    }
}