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

    @Test
    void testGeneratePdf_Residencia_NullFields() {
        // Test safe() null branch and null fechaCreacion branch
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(null);
        doc.setTitulo(null);
        doc.setDescripcion(null);
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(null);
        doc.setFirmadoPor(null);
        doc.setCodigoQrUrl(null);

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_Salvoconducto_NullFechaVencimiento() {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setId(10L);
        doc.setTitulo("Salvo Null");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setFechaVencimiento(null);  // covers the null check branch
        doc.setFirmaDigital("SIG");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_Licitacion_NullFechas() {
        DocumentoLicitacion doc = new DocumentoLicitacion();
        doc.setId(11L);
        doc.setTitulo("Licit Null Fechas");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setFechaApertura(null);   // covers null fechaApertura branch
        doc.setFechaCierre(null);     // covers null fechaCierre branch
        doc.setFirmaDigital("SIG");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_Contrato_NullFechas() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(12L);
        doc.setTitulo("Contrato Null Fechas");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setMontoTotal(0.0);
        doc.setFechaInicioContrato(null);    // covers null fechaInicio branch
        doc.setFechaTerminoContrato(null);   // covers null fechaTermino branch
        doc.setFirmaDigital("SIG");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_JuntaVecinal_NullTipoActa() {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setId(13L);
        doc.setTitulo("Acta Sin Tipo");
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setNombreJuntaVecinal(null);  // tests safe() null branch
        doc.setTipoActa(null);            // covers tipoActa null branch
        doc.setFirmaDigital("SIG");
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }

    @Test
    void testGeneratePdf_WithDescripcion_AndFirmadoPor() {
        // Cover doc.getDescripcion != null branch and doc.getFechaCreacion != null in addFirma
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(14L);
        doc.setTitulo("Residencia Full");
        doc.setDescripcion("Descripcion presente");  // cover the if (doc.getDescripcion() != null) branch
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setFirmadoPor("ALCALDE");  // cover safe() non-null branch in addFirma
        doc.setCodigoQrUrl("https://muni.cl/validar/mock");
        doc.setUsuarioComuna("Providencia");
        doc.setUsuarioNombreCompleto("Vecino Test");
        doc.setUsuarioRut("12345678-9");
        doc.setUsuarioDireccion("Calle X");

        byte[] pdf = pdfService.generateDocumentPdf(doc);
        assertThat(pdf).isNotEmpty();
    }
}

