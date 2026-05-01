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
                    document.add(new Paragraph("Válido hasta: " + s.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
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
                if (l.getFechaApertura() != null) document.add(new Paragraph("Fecha Apertura: " + l.getFechaApertura(), normalFont));
                if (l.getFechaCierre() != null) document.add(new Paragraph("Fecha Cierre: " + l.getFechaCierre(), normalFont));
            } else if (doc instanceof DocumentoContrato c) {
                document.add(new Paragraph("DATOS DEL CONTRATO", headerFont));
                document.add(new Paragraph("RUT Contratista: " + c.getRutContratista(), normalFont));
                document.add(new Paragraph("Monto Total: $" + String.format("%,.0f", c.getMontoTotal()), normalFont));
                if (c.getFechaInicioContrato() != null) document.add(new Paragraph("Fecha Inicio: " + c.getFechaInicioContrato(), normalFont));
                if (c.getFechaTerminoContrato() != null) document.add(new Paragraph("Fecha Término: " + c.getFechaTerminoContrato(), normalFont));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Digital Signature Info
            document.add(new Paragraph("INFORMACIÓN DE SEGURIDAD Y FIRMA", headerFont));
            document.add(new Paragraph("Firmado por: " + doc.getFirmadoPor(), normalFont));
            document.add(new Paragraph("Fecha de Firma: " + doc.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
            document.add(new Paragraph("Hash SHA-256: " + doc.getHashSha256(), footerFont));
            
            document.add(new Paragraph(" "));

            // QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(doc.getCodigoQrUrl(), BarcodeFormat.QR_CODE, 150, 150);
            ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrOut);
            
            Image qrImage = Image.getInstance(qrOut.toByteArray());
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);

            Paragraph qrInfo = new Paragraph("Escanee el código QR para verificar la autenticidad de este documento en muni.digital", footerFont);
            qrInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(qrInfo);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
