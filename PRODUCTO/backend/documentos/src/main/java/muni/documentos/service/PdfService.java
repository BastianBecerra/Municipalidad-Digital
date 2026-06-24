package muni.documentos.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import muni.documentos.model.entity.*;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    private static final Color COLOR_AZUL_MUNI  = new Color(0, 71, 133);
    private static final Color COLOR_AZUL_CLARO = new Color(220, 235, 250);
    private static final Color COLOR_GRIS_TEXTO = new Color(60, 60, 60);
    private static final Color COLOR_GRIS_LINEA = new Color(180, 180, 180);

    private String safe(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    public byte[] generateDocumentPdf(Documento doc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 60);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fTitulo    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, COLOR_AZUL_MUNI);
            Font fSubHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_GRIS_TEXTO);
            Font fNormal    = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_GRIS_TEXTO);
            Font fSmall     = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
            Font fSign      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_AZUL_MUNI);

            // CABECERA AZUL
            addHeader(document);

            // TÍTULO DEL DOCUMENTO
            Paragraph titulo = new Paragraph(getTipoDocumento(doc), fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingBefore(8);
            titulo.setSpacingAfter(4);
            document.add(titulo);
            addLineDivider(document);

            // CONTENIDO ESPECÍFICO POR TIPO
            if (doc instanceof DocumentoResidencia r) {
                addResidenciaContent(document, r, fSubHeader, fNormal);
            } else if (doc instanceof DocumentoSalvoconducto s) {
                addSalvoconductoContent(document, s, fSubHeader, fNormal);
            } else if (doc instanceof DocumentoJuntaVecinal j) {
                addJuntaVecinalContent(document, j, fSubHeader, fNormal);
            } else if (doc instanceof DocumentoLicitacion l) {
                addLicitacionContent(document, l, fSubHeader, fNormal);
            } else if (doc instanceof DocumentoContrato c) {
                addContratoContent(document, c, fSubHeader, fNormal);
            }

            // DESCRIPCIÓN GENERAL (si tiene)
            if (doc.getDescripcion() != null && !doc.getDescripcion().isBlank()) {
                document.add(new Paragraph(" "));
                Paragraph descLabel = new Paragraph("Descripción:", fSubHeader);
                document.add(descLabel);
                document.add(new Paragraph(doc.getDescripcion(), fNormal));
            }

            for (int i = 0; i < 3; i++) document.add(new Paragraph(" "));

            // FIRMA DIGITAL
            addFirma(document, doc, fSign, fSmall, fNormal);

            // CÓDIGO QR
            addQrCode(document, doc, fSmall);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    // ────────── CABECERA ──────────
    private void addHeader(Document document) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_AZUL_MUNI);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(12);

        Font fBlanco   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
        Font fBlancoSm = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.WHITE);

        Paragraph p1 = new Paragraph("MUNICIPALIDAD DIGITAL", fBlanco);
        p1.setAlignment(Element.ALIGN_CENTER);
        Paragraph p2 = new Paragraph("Departamento de Documentación y Atención al Ciudadano", fBlancoSm);
        p2.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(p1);
        cell.addElement(p2);
        headerTable.addCell(cell);
        document.add(headerTable);
    }

    // ────────── LÍNEA DIVISORA ──────────
    private void addLineDivider(Document document) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        line.setSpacingBefore(6);
        line.setSpacingAfter(10);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_AZUL_MUNI);
        cell.setFixedHeight(2f);
        cell.setBorder(Rectangle.NO_BORDER);
        line.addCell(cell);
        document.add(line);
    }

    // ────────── FILA DE DATOS ──────────
    private void addDataRow(PdfPTable table, String label, String value,
                             Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(COLOR_GRIS_LINEA);
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(new Color(245, 245, 245));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(COLOR_GRIS_LINEA);
        valueCell.setPadding(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // ────────── RESIDENCIA ──────────
    private void addResidenciaContent(Document document, DocumentoResidencia r,
                                       Font fSubHeader, Font fNormal) throws DocumentException {
        String comuna         = safe(r.getUsuarioComuna(), "la comuna");
        String nombreCompleto = safe(r.getUsuarioNombreCompleto(), "NOMBRE NO DISPONIBLE");
        String rut            = safe(r.getUsuarioRut(), "RUT NO DISPONIBLE");
        String direccion      = safe(r.getUsuarioDireccion(), "DIRECCIÓN NO DISPONIBLE");
        String nroCert        = r.getId() != null
                ? (r.getFechaCreacion().getYear() + "-" + String.format("%04d", r.getId()))
                : "N/A";

        Paragraph nroP = new Paragraph("CERTIFICADO DE RESIDENCIA N° " + nroCert, fSubHeader);
        nroP.setAlignment(Element.ALIGN_CENTER);
        nroP.setSpacingAfter(12);
        document.add(nroP);

        // Recuadro introductorio azul claro
        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100);
        box.setSpacingBefore(4);
        box.setSpacingAfter(14);
        PdfPCell boxCell = new PdfPCell();
        boxCell.setBackgroundColor(COLOR_AZUL_CLARO);
        boxCell.setBorderColor(COLOR_AZUL_MUNI);
        boxCell.setBorderWidth(1f);
        boxCell.setPadding(10);
        String comunaDisplay = comuna.substring(0, 1).toUpperCase()
                             + comuna.substring(1).toLowerCase();
        String intro = "La Ilustre Municipalidad de " + comunaDisplay
                + " certifica que, según los antecedentes validados en nuestra plataforma digital, "
                + "el/la ciudadano/a que a continuación se individualiza acredita residencia "
                + "en el domicilio indicado.";
        boxCell.addElement(new Paragraph(intro, fNormal));
        box.addCell(boxCell);
        document.add(box);

        // Tabla de datos del vecino
        PdfPTable dataTable = new PdfPTable(new float[]{35f, 65f});
        dataTable.setWidthPercentage(90);
        dataTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        dataTable.setSpacingAfter(12);
        addDataRow(dataTable, "Nombre completo:", nombreCompleto.toUpperCase(), fSubHeader, fNormal);
        addDataRow(dataTable, "RUT:", rut, fSubHeader, fNormal);
        addDataRow(dataTable, "Domicilio:", direccion + ", " + comuna + ".", fSubHeader, fNormal);
        document.add(dataTable);

        // Texto legal
        Paragraph legal = new Paragraph(
                "Se extiende el presente Certificado de Residencia a petición del interesado, "
                + "para los fines que estime conveniente.", fNormal);
        legal.setAlignment(Element.ALIGN_JUSTIFIED);
        legal.setSpacingAfter(6);
        document.add(legal);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "CL"));
        String fecha = r.getFechaCreacion() != null ? r.getFechaCreacion().format(fmt) : "N/A";
        document.add(new Paragraph("Fecha de Emisión: " + fecha, fNormal));
        document.add(new Paragraph("Vigencia: 90 días desde la fecha de emisión.", fNormal));
    }

    // ────────── SALVOCONDUCTO ──────────
    private void addSalvoconductoContent(Document document, DocumentoSalvoconducto s,
                                          Font fSubHeader, Font fNormal) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{35f, 65f});
        t.setWidthPercentage(90);
        t.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.setSpacingAfter(12);
        addDataRow(t, "Nombre:", safe(s.getUsuarioNombreCompleto(), "N/A"), fSubHeader, fNormal);
        addDataRow(t, "RUT:", safe(s.getUsuarioRut(), "N/A"), fSubHeader, fNormal);
        addDataRow(t, "Motivo:", safe(s.getMotivo(), "N/A"), fSubHeader, fNormal);
        addDataRow(t, "Dirección destino:", safe(s.getDireccionDestino(), "N/A"), fSubHeader, fNormal);
        if (s.getFechaVencimiento() != null)
            addDataRow(t, "Válido hasta:",
                    s.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    fSubHeader, fNormal);
        document.add(t);
    }

    // ────────── JUNTA VECINAL ──────────
    private void addJuntaVecinalContent(Document document, DocumentoJuntaVecinal j,
                                         Font fSubHeader, Font fNormal) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{35f, 65f});
        t.setWidthPercentage(90);
        t.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.setSpacingAfter(12);
        addDataRow(t, "Junta de Vecinos:", safe(j.getNombreJuntaVecinal(), "N/A"), fSubHeader, fNormal);
        addDataRow(t, "ID Registro:", String.valueOf(j.getJuntaVecinosId()), fSubHeader, fNormal);
        addDataRow(t, "Tipo de acta:", j.getTipoActa() != null ? j.getTipoActa().name() : "N/A", fSubHeader, fNormal);
        document.add(t);
    }

    // ────────── LICITACIÓN ──────────
    private void addLicitacionContent(Document document, DocumentoLicitacion l,
                                       Font fSubHeader, Font fNormal) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{35f, 65f});
        t.setWidthPercentage(90);
        t.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.setSpacingAfter(12);
        addDataRow(t, "Código:", safe(l.getCodigoLicitacion(), "N/A"), fSubHeader, fNormal);
        addDataRow(t, "Tipo:", l.getTipoLicitacion() != null ? l.getTipoLicitacion().name() : "N/A", fSubHeader, fNormal);
        addDataRow(t, "ID Proyecto:", String.valueOf(l.getProyectoId()), fSubHeader, fNormal);
        if (l.getFechaApertura() != null) addDataRow(t, "Fecha apertura:", l.getFechaApertura().toString(), fSubHeader, fNormal);
        if (l.getFechaCierre()   != null) addDataRow(t, "Fecha cierre:", l.getFechaCierre().toString(), fSubHeader, fNormal);
        document.add(t);
    }

    // ────────── CONTRATO ──────────
    private void addContratoContent(Document document, DocumentoContrato c,
                                     Font fSubHeader, Font fNormal) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{35f, 65f});
        t.setWidthPercentage(90);
        t.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.setSpacingAfter(12);
        addDataRow(t, "RUT Contratista:", safe(c.getRutContratista(), "N/A"), fSubHeader, fNormal);
        addDataRow(t, "Monto total:", "$" + String.format("%,.0f", c.getMontoTotal()), fSubHeader, fNormal);
        if (c.getFechaInicioContrato()  != null) addDataRow(t, "Fecha inicio:", c.getFechaInicioContrato().toString(), fSubHeader, fNormal);
        if (c.getFechaTerminoContrato() != null) addDataRow(t, "Fecha término:", c.getFechaTerminoContrato().toString(), fSubHeader, fNormal);
        document.add(t);
    }

    // ────────── FIRMA ──────────
    private void addFirma(Document document, Documento doc,
                           Font fSign, Font fSmall, Font fNormal) throws DocumentException {
        addLineDivider(document);

        try {
            InputStream is = getClass().getResourceAsStream("/static/images/muni.firma.png");
            if (is != null) {
                byte[] imageBytes = is.readAllBytes();
                Image signatureImg = Image.getInstance(imageBytes);
                signatureImg.scaleToFit(120, 80);
                signatureImg.setAlignment(Element.ALIGN_CENTER);
                document.add(signatureImg);
                is.close();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar firma: " + e.getMessage());
        }

        Paragraph signedBy = new Paragraph("FIRMADO ELECTRÓNICAMENTE", fSign);
        signedBy.setAlignment(Element.ALIGN_CENTER);
        document.add(signedBy);

        Paragraph firmadoPorP = new Paragraph(
                "Por: " + safe(doc.getFirmadoPor(), "SISTEMA MUNICIPAL AUTOMÁTICO"), fNormal);
        firmadoPorP.setAlignment(Element.ALIGN_CENTER);
        document.add(firmadoPorP);

        if (doc.getFechaCreacion() != null) {
            Paragraph emitido = new Paragraph(
                    "Fecha de emisión: " + doc.getFechaCreacion()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), fSmall);
            emitido.setAlignment(Element.ALIGN_CENTER);
            document.add(emitido);
        }
    }

    // ────────── QR ──────────
    private void addQrCode(Document document, Documento doc, Font fSmall) {
        try {
            String qrUrl = safe(doc.getCodigoQrUrl(), "https://muni.digital/validar/sin-hash");
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, 120, 120);
            ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrOut);

            Image qrImage = Image.getInstance(qrOut.toByteArray());
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(new Paragraph(" "));
            document.add(qrImage);

            Paragraph qrInfo = new Paragraph(
                    "Escanee el código QR para verificar la autenticidad de este documento en muni.digital",
                    fSmall);
            qrInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(qrInfo);
        } catch (Exception e) {
            System.err.println("Error al generar QR: " + e.getMessage());
        }
    }

    // ────────── HELPERS ──────────
    private String getTipoDocumento(Documento doc) {
        if (doc instanceof DocumentoResidencia)    return "CERTIFICADO DE RESIDENCIA";
        if (doc instanceof DocumentoSalvoconducto) return "SALVOCONDUCTO MUNICIPAL";
        if (doc instanceof DocumentoJuntaVecinal)  return "ACTA DE JUNTA VECINAL";
        if (doc instanceof DocumentoLicitacion)    return "BASES DE LICITACIÓN";
        if (doc instanceof DocumentoContrato)      return "CONTRATO MUNICIPAL";
        return doc.getTitulo() != null ? doc.getTitulo().toUpperCase() : "DOCUMENTO MUNICIPAL";
    }
}
