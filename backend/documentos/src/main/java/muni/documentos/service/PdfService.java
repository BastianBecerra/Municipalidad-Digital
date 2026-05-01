package muni.documentos.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
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

@Service
public class PdfService {

    public byte[] generateDocumentPdf(Documento doc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

            // Header - Municipalidad Digital
            Paragraph muniHeader = new Paragraph("MUNICIPALIDAD DIGITAL - DEPARTAMENTO DE DOCUMENTACIÓN", headerFont);
            muniHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(muniHeader);
            document.add(new Paragraph(" "));

            // Title
            Paragraph title = new Paragraph(doc.getTitulo().toUpperCase(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Content
            document.add(new Paragraph("Descripción:", headerFont));
            document.add(new Paragraph(doc.getDescripcion(), normalFont));
            document.add(new Paragraph(" "));

            // Specific fields based on type
            if (doc instanceof DocumentoSalvoconducto s) {
                document.add(new Paragraph("DATOS DEL SOLICITANTE", headerFont));
                document.add(new Paragraph("Nombre: " + s.getUsuarioNombreCompleto(), normalFont));
                document.add(new Paragraph("RUT: " + s.getUsuarioRut(), normalFont));
                document.add(new Paragraph("Motivo: " + s.getMotivo(), normalFont));
                document.add(new Paragraph("Dirección Destino: " + s.getDireccionDestino(), normalFont));
                if (s.getFechaVencimiento() != null) {
                    document.add(new Paragraph(
                            "Válido hasta: "
                                    + s.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            normalFont));
                }
            } else if (doc instanceof DocumentoJuntaVecinal j) {
                document.add(new Paragraph("DATOS JUNTA DE VECINOS", headerFont));
                document.add(new Paragraph("Nombre: " + j.getNombreJuntaVecinal(), normalFont));
                document.add(new Paragraph("ID Registro: " + j.getJuntaVecinosId(), normalFont));
                document.add(new Paragraph("Tipo de Acta: " + j.getTipoActa(), normalFont));
            } else if (doc instanceof DocumentoLicitacion l) {
                document.add(new Paragraph("DATOS DE LA LICITACIÓN", headerFont));
                document.add(new Paragraph("Código: " + l.getCodigoLicitacion(), normalFont));
                document.add(new Paragraph("Tipo: " + l.getTipoLicitacion(), normalFont));
                document.add(new Paragraph("ID Proyecto Asociado: " + l.getProyectoId(), normalFont));
                if (l.getFechaApertura() != null)
                    document.add(new Paragraph("Fecha Apertura: " + l.getFechaApertura(), normalFont));
                if (l.getFechaCierre() != null)
                    document.add(new Paragraph("Fecha Cierre: " + l.getFechaCierre(), normalFont));
            } else if (doc instanceof DocumentoContrato c) {
                document.add(new Paragraph("DATOS DEL CONTRATO", headerFont));
                document.add(new Paragraph("RUT Contratista: " + c.getRutContratista(), normalFont));
                document.add(new Paragraph("Monto Total: $" + String.format("%,.0f", c.getMontoTotal()), normalFont));
                if (c.getFechaInicioContrato() != null)
                    document.add(new Paragraph("Fecha Inicio: " + c.getFechaInicioContrato(), normalFont));
                if (c.getFechaTerminoContrato() != null)
                    document.add(new Paragraph("Fecha Término: " + c.getFechaTerminoContrato(), normalFont));
            } else if (doc instanceof DocumentoResidencia r) {
                // Formato específico solicitado
                Paragraph muniName = new Paragraph(
                        "MUNICIPALIDAD DE "
                                + (r.getUsuarioComuna() != null ? r.getUsuarioComuna().toUpperCase() : "VIÑA DEL MAR"),
                        headerFont);
                muniName.setAlignment(Element.ALIGN_CENTER);
                document.add(muniName);

                Paragraph dac = new Paragraph("DIRECCIÓN DE ATENCIÓN AL CIUDADANO", headerFont);
                dac.setAlignment(Element.ALIGN_CENTER);
                document.add(dac);
                document.add(new Paragraph(" "));

                Paragraph nro = new Paragraph(
                        "CERTIFICADO DE RESIDENCIA N°: " + r.getFechaCreacion().getYear() + "-000" + r.getId(),
                        headerFont);
                nro.setAlignment(Element.ALIGN_CENTER);
                document.add(nro);
                document.add(new Paragraph(" "));

                String intro = "La Ilustre Municipalidad de "
                        + (r.getUsuarioComuna() != null ? r.getUsuarioComuna() : "Viña del Mar") +
                        " certifica que, según los antecedentes validados en nuestra plataforma digital:";
                document.add(new Paragraph(intro, normalFont));
                document.add(new Paragraph(" "));

                document.add(new Paragraph("DON/DOÑA: " + r.getUsuarioNombreCompleto().toUpperCase(), normalFont));
                document.add(new Paragraph("RUT: " + r.getUsuarioRut(), normalFont));
                document.add(new Paragraph("DOMICILIO: " + r.getUsuarioDireccion() + ", " + r.getUsuarioComuna() + ".",
                        normalFont));
                document.add(new Paragraph(" "));

                document.add(new Paragraph(
                        "Se extiende el presente certificado a petición del interesado para fines particulares.",
                        normalFont));
                document.add(new Paragraph(" "));

                document.add(new Paragraph(
                        "Fecha de Emisión: "
                                + r.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")),
                        normalFont));
                document.add(new Paragraph("Vigencia: 90 días.", normalFont));
            }
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Digital Signature Info
            Font signFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(0, 102, 204));

            // Add Digital Signature Image
            try {
                InputStream is = getClass().getResourceAsStream("/static/images/muni.firma.png");
                if (is != null) {
                    byte[] imageBytes = is.readAllBytes();
                    Image signatureImg = Image.getInstance(imageBytes);
                    signatureImg.scaleToFit(140, 100);
                    signatureImg.setAlignment(Element.ALIGN_CENTER);
                    document.add(signatureImg);
                    is.close();
                }
            } catch (Exception e) {
                System.err.println("Error al cargar firma manual: " + e.getMessage());
            }

            Paragraph signedBy = new Paragraph("FIRMADO ELECTRÓNICAMENTE", signFont);
            signedBy.setAlignment(Element.ALIGN_CENTER);
            document.add(signedBy);

            Paragraph signedName = new Paragraph("Por: " + doc.getFirmadoPor(), normalFont);
            signedName.setAlignment(Element.ALIGN_CENTER);
            document.add(signedName);

            Paragraph emissionDate = new Paragraph(
                    "Fecha de Emisión: "
                            + doc.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    footerFont);
            emissionDate.setAlignment(Element.ALIGN_CENTER);
            document.add(emissionDate);

            document.add(new Paragraph(" "));

            // QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(doc.getCodigoQrUrl(), BarcodeFormat.QR_CODE, 150, 150);
            ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrOut);

            Image qrImage = Image.getInstance(qrOut.toByteArray());
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);

            Paragraph qrInfo = new Paragraph(
                    "Escanee el código QR para verificar la autenticidad de este documento en muni.digital",
                    footerFont);
            qrInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(qrInfo);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
