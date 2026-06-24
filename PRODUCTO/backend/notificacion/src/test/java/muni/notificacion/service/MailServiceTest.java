package muni.notificacion.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import muni.notificacion.dto.DocumentoAprobadoRequest;
import muni.notificacion.dto.RestablecerPasswordRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    void testEnviarDocumentoAprobado_Success_SMTP() {
        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("citizen@example.com");
        request.setNombreCompleto("Juan Pérez");
        request.setDocumentId("DOC-123");
        request.setTitulo("Permiso de Residencia");
        request.setHashBlockchain("0xabc123");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThatCode(() -> mailService.enviarDocumentoAprobado(request))
                .doesNotThrowAnyException();

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testEnviarDocumentoAprobado_WithEmptyBlockchainHash() {
        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("citizen@example.com");
        request.setNombreCompleto("Juan Pérez");
        request.setDocumentId("DOC-123");
        request.setTitulo("Permiso de Residencia");
        request.setHashBlockchain(""); // Empty to test alternative HTML branch

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThatCode(() -> mailService.enviarDocumentoAprobado(request))
                .doesNotThrowAnyException();
    }

    @Test
    void testEnviarDocumentoAprobado_BlockchainHashNullAndNullString() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Test with null hash
        DocumentoAprobadoRequest requestNull = new DocumentoAprobadoRequest();
        requestNull.setEmail("citizen@example.com");
        requestNull.setNombreCompleto("Juan Pérez");
        requestNull.setDocumentId("DOC-123");
        requestNull.setTitulo("Permiso de Residencia");
        requestNull.setHashBlockchain(null);

        assertThatCode(() -> mailService.enviarDocumentoAprobado(requestNull))
                .doesNotThrowAnyException();

        // Test with "null" string (case-insensitive)
        DocumentoAprobadoRequest requestNullStr = new DocumentoAprobadoRequest();
        requestNullStr.setEmail("citizen@example.com");
        requestNullStr.setNombreCompleto("Juan Pérez");
        requestNullStr.setDocumentId("DOC-123");
        requestNullStr.setTitulo("Permiso de Residencia");
        requestNullStr.setHashBlockchain("NuLL");

        assertThatCode(() -> mailService.enviarDocumentoAprobado(requestNullStr))
                .doesNotThrowAnyException();

        // Test with blank spaces
        DocumentoAprobadoRequest requestBlank = new DocumentoAprobadoRequest();
        requestBlank.setEmail("citizen@example.com");
        requestBlank.setNombreCompleto("Juan Pérez");
        requestBlank.setDocumentId("DOC-123");
        requestBlank.setTitulo("Permiso de Residencia");
        requestBlank.setHashBlockchain("   ");

        assertThatCode(() -> mailService.enviarDocumentoAprobado(requestBlank))
                .doesNotThrowAnyException();
    }

    @Test
    void testEnviarDocumentoAprobado_MailSenderException_Fallback() {
        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("citizen@example.com");
        request.setNombreCompleto("Juan Pérez");
        request.setDocumentId("DOC-123");
        request.setTitulo("Permiso de Residencia");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // Should catch the exception internally and run simulated logging
        assertThatCode(() -> mailService.enviarDocumentoAprobado(request))
                .doesNotThrowAnyException();
    }

    @Test
    void testEnviarDocumentoAprobado_MailSenderNull_Simulation() {
        MailService serviceWithNullSender = new MailService(); // mailSender is null

        DocumentoAprobadoRequest request = new DocumentoAprobadoRequest();
        request.setEmail("citizen@example.com");
        request.setNombreCompleto("Juan Pérez");
        request.setDocumentId("DOC-123");
        request.setTitulo("Permiso de Residencia");

        assertThatCode(() -> serviceWithNullSender.enviarDocumentoAprobado(request))
                .doesNotThrowAnyException();
    }

    @Test
    void testEnviarRestablecerPassword_Success_SMTP() {
        RestablecerPasswordRequest request = new RestablecerPasswordRequest();
        request.setEmail("user@example.com");
        request.setNombreCompleto("María Gómez");
        request.setUrlRestablecer("http://localhost:5173/reset?token=xyz");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThatCode(() -> mailService.enviarRestablecerPassword(request))
                .doesNotThrowAnyException();

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testEnviarRestablecerPassword_MailSenderException_Fallback() {
        RestablecerPasswordRequest request = new RestablecerPasswordRequest();
        request.setEmail("user@example.com");
        request.setNombreCompleto("María Gómez");
        request.setUrlRestablecer("http://localhost:5173/reset?token=xyz");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP timeout")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> mailService.enviarRestablecerPassword(request))
                .doesNotThrowAnyException();
    }

    @Test
    void testEnviarRestablecerPassword_MailSenderNull_Simulation() {
        MailService serviceWithNullSender = new MailService();

        RestablecerPasswordRequest request = new RestablecerPasswordRequest();
        request.setEmail("user@example.com");
        request.setNombreCompleto("María Gómez");
        request.setUrlRestablecer("http://localhost:5173/reset?token=xyz");

        assertThatCode(() -> serviceWithNullSender.enviarRestablecerPassword(request))
                .doesNotThrowAnyException();
    }
}
