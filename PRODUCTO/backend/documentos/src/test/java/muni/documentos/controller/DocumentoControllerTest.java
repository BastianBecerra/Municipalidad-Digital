package muni.documentos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni.documentos.model.entity.*;
import muni.documentos.model.enums.EstadoDocumento;
import muni.documentos.service.DocumentoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentoService documentoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "funcionario", roles = {"FUNCIONARIO"})
    void testGetAllDocuments_Authorized() throws Exception {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(10L);
        doc.setTitulo("Licitacion Basura");

        when(documentoService.findAll()).thenReturn(List.of(doc));

        mockMvc.perform(get("/documentos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].titulo").value("Licitacion Basura"));
    }

    @Test
    void testGetAllDocuments_Unauthorized() throws Exception {
        mockMvc.perform(get("/documentos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "12345678-9", roles = {"VECINO"})
    void testGetMyDocuments() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(15L);
        doc.setUsuarioRut("12345678-9");

        when(documentoService.findByUsuarioRut("12345678-9")).thenReturn(List.of(doc));

        mockMvc.perform(get("/documentos/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(15))
                .andExpect(jsonPath("$[0].usuarioRut").value("12345678-9"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetById_AdminAllowed() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(1L);
        doc.setUsuarioRut("11111111-1");

        when(documentoService.findById(1L)).thenReturn(doc);

        mockMvc.perform(get("/documentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "22222222-2", roles = {"VECINO"})
    void testGetById_VecinoNotOwner_Forbidden() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(1L);
        doc.setUsuarioRut("11111111-1"); // Owner is different

        when(documentoService.findById(1L)).thenReturn(doc);

        mockMvc.perform(get("/documentos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "11111111-1", roles = {"VECINO"})
    void testGetById_VecinoOwner_Allowed() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(1L);
        doc.setUsuarioRut("11111111-1");

        when(documentoService.findById(1L)).thenReturn(doc);

        mockMvc.perform(get("/documentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioRut").value("11111111-1"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDownloadPdf() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(1L);
        byte[] pdfBytes = new byte[]{9, 8, 7};

        when(documentoService.findById(1L)).thenReturn(doc);
        when(documentoService.generatePdf(1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/documentos/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=documento_1.pdf"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testApproveDocument() throws Exception {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(1L);
        doc.setEstado(EstadoDocumento.FIRMADO);

        when(documentoService.approveDocument(1L)).thenReturn(doc);

        mockMvc.perform(post("/documentos/1/aprobar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("FIRMADO"));
    }

    @Test
    @WithMockUser(username = "funcionario", roles = {"FUNCIONARIO"})
    void testCreateJuntaVecinal() throws Exception {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setTitulo("Acta Junta Vecinal");
        doc.setNombreJuntaVecinal("Junta A");

        when(documentoService.createJuntaVecinalDoc(any(), eq(true))).thenReturn(doc);

        mockMvc.perform(post("/documentos/jjvv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Acta Junta Vecinal"));
    }

    @Test
    @WithMockUser(username = "funcionario", roles = {"FUNCIONARIO"})
    void testCreateLicitacion() throws Exception {
        DocumentoLicitacion doc = new DocumentoLicitacion();
        doc.setTitulo("Licitacion A");
        doc.setCodigoLicitacion("LIC-123");

        when(documentoService.createLicitacionDoc(any(), eq(true))).thenReturn(doc);

        mockMvc.perform(post("/documentos/licitacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Licitacion A"));
    }

    @Test
    @WithMockUser(username = "funcionario", roles = {"FUNCIONARIO"})
    void testCreateContrato() throws Exception {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setTitulo("Contrato A");

        when(documentoService.createContratoDoc(any(), eq(true))).thenReturn(doc);

        mockMvc.perform(post("/documentos/contrato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Contrato A"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSyncBlockchain() throws Exception {
        doNothing().when(documentoService).syncWithBlockchain(1L);

        mockMvc.perform(post("/documentos/1/blockchain"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPublicByHash_Success() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setTitulo("Doc Publico");
        String hash = "aefacda123456789012345678901234567890123456789012345678901234567";

        when(documentoService.findByHashSha256(hash)).thenReturn(doc);

        mockMvc.perform(get("/documentos/public/hash/" + hash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Doc Publico"));
    }

    @Test
    void testGetPublicByHash_InvalidFormat() throws Exception {
        mockMvc.perform(get("/documentos/public/hash/invalid-hash"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Formato de hash no válido"));
    }

    @Test
    void testGetPublicByHash_NotFound() throws Exception {
        String hash = "aefacda123456789012345678901234567890123456789012345678901234567";
        when(documentoService.findByHashSha256(hash)).thenThrow(new RuntimeException("No encontrado"));

        mockMvc.perform(get("/documentos/public/hash/" + hash))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No encontrado"));
    }

    @Test
    @WithMockUser(username = "11111111-1", roles = {"VECINO"})
    void testCreateSalvoconducto_Vecino() throws Exception {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setTitulo("Salvo Vecino");
        doc.setDescripcion("Viaje urgente");

        when(documentoService.createSalvoconductoDoc(any(), eq(true))).thenReturn(doc);

        mockMvc.perform(post("/documentos/salvoconducto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Salvo Vecino"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateSalvoconducto_Admin() throws Exception {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setTitulo("Salvo Admin");

        when(documentoService.createSalvoconductoDoc(any(), eq(true))).thenReturn(doc);

        mockMvc.perform(post("/documentos/salvoconducto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Salvo Admin"));
    }

    @Test
    @WithMockUser(username = "11111111-1", roles = {"VECINO"})
    void testCreateResidencia_Vecino() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setTitulo("Residencia Vecino");

        when(documentoService.createResidenciaDoc(any(), eq(true))).thenReturn(doc);

        mockMvc.perform(post("/documentos/residencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Residencia Vecino"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateResidencia_Admin() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setTitulo("Residencia Admin");

        when(documentoService.createResidenciaDoc(any(), eq(true))).thenReturn(doc);

        mockMvc.perform(post("/documentos/residencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Residencia Admin"));
    }

    @Test
    @WithMockUser(username = "22222222-2", roles = {"VECINO"})
    void testDownloadPdf_VecinoNotOwner_Forbidden() throws Exception {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(1L);
        doc.setUsuarioRut("11111111-1"); // different owner

        when(documentoService.findById(1L)).thenReturn(doc);

        mockMvc.perform(get("/documentos/1/pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateJuntaVecinalAlt() throws Exception {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setTitulo("Acta Alt");
        doc.setNombreJuntaVecinal("Junta B");

        when(documentoService.createJuntaVecinalDoc(any(), eq(true))).thenReturn(doc);

        mockMvc.perform(post("/documentos/junta-vecinal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Acta Alt"));
    }
}

