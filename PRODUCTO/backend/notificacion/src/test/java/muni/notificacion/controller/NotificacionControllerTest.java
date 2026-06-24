package muni.notificacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni.notificacion.dto.DocumentoAprobadoRequest;
import muni.notificacion.dto.RestablecerPasswordRequest;
import muni.notificacion.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MailService mailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testEnviarDocumentoAprobado_Authorized_Admin() throws Exception {
        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("admin@example.com");
        request.setNombreCompleto("Admin User");
        request.setDocumentId("DOC-999");
        request.setTitulo("Decreto Alcaldicio");

        doNothing().when(mailService).enviarDocumentoAprobado(any(DocumentoAprobadoRequest.class));

        mockMvc.perform(post("/api/notificaciones/public/documento-aprobado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificación de documento aprobado procesada con éxito."));
    }

    @Test
    @WithMockUser(username = "funcionario", roles = {"FUNCIONARIO"})
    void testEnviarDocumentoAprobado_Authorized_Funcionario() throws Exception {
        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("func@example.com");
        request.setNombreCompleto("Funcionario");
        request.setDocumentId("DOC-998");
        request.setTitulo("Salvoconducto");

        doNothing().when(mailService).enviarDocumentoAprobado(any(DocumentoAprobadoRequest.class));

        mockMvc.perform(post("/api/notificaciones/public/documento-aprobado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "vecino", roles = {"VECINO"})
    void testEnviarDocumentoAprobado_Unauthorized_RoleVecino() throws Exception {
        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("vecino@example.com");

        mockMvc.perform(post("/api/notificaciones/public/documento-aprobado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEnviarDocumentoAprobado_Unauthorized_Anonymous() throws Exception {
        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("anon@example.com");

        mockMvc.perform(post("/api/notificaciones/public/documento-aprobado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEnviarRestablecerPassword_PermitAll() throws Exception {
        RestablecerPasswordRequest request = new RestablecerPasswordRequest();
        request.setEmail("citizen@example.com");
        request.setNombreCompleto("Ciudadano");
        request.setUrlRestablecer("http://reset");

        doNothing().when(mailService).enviarRestablecerPassword(any(RestablecerPasswordRequest.class));

        mockMvc.perform(post("/api/notificaciones/public/restablecer-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificación de restablecimiento de contraseña procesada con éxito."));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testEnviarDocumentoAprobado_InternalServerError() throws Exception {
        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("admin@example.com");

        doThrow(new RuntimeException("Simulated service error")).when(mailService).enviarDocumentoAprobado(any());

        mockMvc.perform(post("/api/notificaciones/public/documento-aprobado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al procesar la notificación: Simulated service error"));
    }

    @Test
    void testEnviarRestablecerPassword_InternalServerError() throws Exception {
        RestablecerPasswordRequest request = new RestablecerPasswordRequest();
        request.setEmail("user@example.com");

        doThrow(new RuntimeException("Simulated mailer error")).when(mailService).enviarRestablecerPassword(any());

        mockMvc.perform(post("/api/notificaciones/public/restablecer-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al procesar la notificación: Simulated mailer error"));
    }
}
