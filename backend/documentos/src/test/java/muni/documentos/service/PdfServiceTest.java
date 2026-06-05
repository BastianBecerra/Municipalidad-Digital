package muni.documentos.service;

import muni.documentos.model.entity.*;
import muni.documentos.model.enums.EstadoDocumento;
import muni.documentos.model.enums.TipoDocumentoJJVV;
import muni.documentos.model.enums.TipoDocumentoLicitacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PdfServiceTest {

    private final PdfService pdfService = new PdfService();

    @Test
    void testGeneratePdf_Residencia() {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(1L);
        doc.setTitulo("Certificado Residencia");
        doc.setDescripcion("Vecino certificado");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setUsuarioRut("12345678-9");
        doc.setUsuarioNombreCompleto("Diego Maradona");
        doc.setUsuarioDireccion("Av Siempre Viva 742");
        doc.setUsuarioComuna("Santiago");
        doc.setFirmaDigital("SIG_MOCK");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_Salvoconducto() {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setId(2L);
        doc.setTitulo("Salvoconducto Mudanza");
        doc.setDescripcion("Mudanza intercomunal");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setUsuarioRut("98765432-1");
        doc.setUsuarioNombreCompleto("Lionel Messi");
        doc.setMotivo("Mudanza de hogar");
        doc.setDireccionDestino("Calle B 456");
        doc.setFechaVencimiento(LocalDateTime.now().plusDays(2));
        doc.setFirmaDigital("SIG_MOCK");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_JuntaVecinal() {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setId(3L);
        doc.setTitulo("Acta Junta Vecinal");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setNombreJuntaVecinal("Junta Vecinos N1");
        doc.setRutMinistroDeFe("11111111-1");
        doc.setTipoActa(TipoDocumentoJJVV.ACTA_ASAMBLEA);
        doc.setFirmaDigital("SIG_MOCK");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_Licitacion() {
        DocumentoLicitacion doc = new DocumentoLicitacion();
        doc.setId(4L);
        doc.setTitulo("Licitacion Luminarias");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setCodigoLicitacion("LIC-2026-009");
        doc.setTipoLicitacion(TipoDocumentoLicitacion.PUBLICA);
        doc.setFechaApertura(LocalDate.now());
        doc.setFechaCierre(LocalDate.now().plusDays(30));
        doc.setFirmaDigital("SIG_MOCK");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_Contrato() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(5L);
        doc.setTitulo("Contrato de Mantencion");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setRutContratista("77777777-7");
        doc.setMontoTotal(25000000.0);
        doc.setFechaInicioContrato(LocalDate.now());
        doc.setFechaTerminoContrato(LocalDate.now().plusYears(1));
        doc.setFirmaDigital("SIG_MOCK");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }
}
