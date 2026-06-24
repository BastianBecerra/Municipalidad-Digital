package muni.notificacion.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import muni.notificacion.dto.DocumentoAprobadoRequest;
import muni.notificacion.dto.RestablecerPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void enviarDocumentoAprobado(DocumentoAprobadoRequest request) {
        String htmlContent = buildDocumentoAprobadoHtml(request);
        String subject = "Documento Aprobado y Firmado - Municipalidad Digital (" + request.getDocumentId() + ")";
        
        log.info("=== GENERANDO CORREO DE APROBACIÓN DE DOCUMENTO ===");
        log.info("Destinatario: {} ({})", request.getNombreCompleto(), request.getEmail());
        log.info("Documento: {} - {}", request.getDocumentId(), request.getTitulo());
        
        boolean enviado = false;
        if (mailSender != null) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(request.getEmail());
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                log.info("✓ Correo enviado con éxito vía SMTP a {}", request.getEmail());
                enviado = true;
            } catch (Exception e) {
                log.warn("⚠ No se pudo enviar el correo vía SMTP (Servidor SMTP no disponible o mal configurado): {}", e.getMessage());
            }
        } else {
            log.info("ℹ JavaMailSender no está configurado o disponible.");
        }

        if (!enviado) {
            logSimulatedEmail(request.getEmail(), subject, htmlContent);
        }
    }

    public void enviarRestablecerPassword(RestablecerPasswordRequest request) {
        String htmlContent = buildRestablecerPasswordHtml(request);
        String subject = "Restablecer Contraseña - Municipalidad Digital";

        log.info("=== GENERANDO CORREO DE RECUPERACIÓN DE CONTRASEÑA ===");
        log.info("Destinatario: {} ({})", request.getNombreCompleto(), request.getEmail());

        boolean enviado = false;
        if (mailSender != null) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(request.getEmail());
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                log.info("✓ Correo enviado con éxito vía SMTP a {}", request.getEmail());
                enviado = true;
            } catch (Exception e) {
                log.warn("⚠ No se pudo enviar el correo vía SMTP (Servidor SMTP no disponible o mal configurado): {}", e.getMessage());
            }
        } else {
            log.info("ℹ JavaMailSender no está configurado o disponible.");
        }

        if (!enviado) {
            logSimulatedEmail(request.getEmail(), subject, htmlContent);
        }
    }

    private void logSimulatedEmail(String to, String subject, String htmlContent) {
        log.info("\n" +
                "┌────────────────────────────────────────────────────────────────────────┐\n" +
                "│                       SIMULADOR DE CORREO ELECTRONICO                  │\n" +
                "├────────────────────────────────────────────────────────────────────────┤\n" +
                "│ DE: no-reply@municipalidad.cl                                          │\n" +
                "│ PARA: " + String.format("%-56s", to) + " │\n" +
                "│ ASUNTO: " + String.format("%-54s", subject) + " │\n" +
                "├────────────────────────────────────────────────────────────────────────┤\n" +
                "│                                                                        │\n" +
                "│ [CONTENIDO HTML DEL CORREO]                                            │\n" +
                "│                                                                        │\n" +
                htmlContent + "\n" +
                "│                                                                        │\n" +
                "└────────────────────────────────────────────────────────────────────────┘\n");
    }

    private String buildDocumentoAprobadoHtml(DocumentoAprobadoRequest r) {
        String blockchainSection = "";
        if (r.getHashBlockchain() != null && !r.getHashBlockchain().trim().isEmpty() && !r.getHashBlockchain().equalsIgnoreCase("null")) {
            blockchainSection = 
                "        <div class=\"blockchain-box\">\n" +
                "          <div class=\"blockchain-header\">\n" +
                "            <span style=\"margin-right: 6px;\">🛡️</span> REGISTRO INMUTABLE BLOCKCHAIN\n" +
                "          </div>\n" +
                "          <div style=\"font-size: 13px; color: #d1d5db; margin-bottom: 12px; line-height: 1.5;\">\n" +
                "            Este documento ha sido sellado criptográficamente en la Blockchain Municipal, garantizando su autenticidad e inmutabilidad absoluta.\n" +
                "          </div>\n" +
                "          <div class=\"blockchain-hash\">" + r.getHashBlockchain() + "</div>\n" +
                "        </div>\n";
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Documento Aprobado</title>\n" +
                "  <style>\n" +
                "    body {\n" +
                "      font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\n" +
                "      background-color: #f4f6f9;\n" +
                "      margin: 0;\n" +
                "      padding: 0;\n" +
                "      -webkit-font-smoothing: antialiased;\n" +
                "    }\n" +
                "    .wrapper {\n" +
                "      width: 100%;\n" +
                "      background-color: #f4f6f9;\n" +
                "      padding: 40px 0;\n" +
                "    }\n" +
                "    .container {\n" +
                "      max-width: 600px;\n" +
                "      margin: 0 auto;\n" +
                "      background-color: #ffffff;\n" +
                "      border-radius: 12px;\n" +
                "      overflow: hidden;\n" +
                "      box-shadow: 0 8px 30px rgba(0,0,0,0.05);\n" +
                "    }\n" +
                "    .header {\n" +
                "      background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);\n" +
                "      padding: 35px 30px;\n" +
                "      text-align: center;\n" +
                "      color: #ffffff;\n" +
                "    }\n" +
                "    .header h1 {\n" +
                "      margin: 0;\n" +
                "      font-size: 24px;\n" +
                "      font-weight: 700;\n" +
                "      letter-spacing: 0.5px;\n" +
                "    }\n" +
                "    .header p {\n" +
                "      margin: 8px 0 0 0;\n" +
                "      font-size: 14px;\n" +
                "      color: #a0aec0;\n" +
                "    }\n" +
                "    .content {\n" +
                "      padding: 40px 30px;\n" +
                "    }\n" +
                "    .greeting {\n" +
                "      font-size: 18px;\n" +
                "      color: #2d3748;\n" +
                "      font-weight: 600;\n" +
                "      margin-bottom: 12px;\n" +
                "    }\n" +
                "    .intro {\n" +
                "      font-size: 15px;\n" +
                "      color: #718096;\n" +
                "      line-height: 1.6;\n" +
                "      margin-bottom: 30px;\n" +
                "    }\n" +
                "    .card {\n" +
                "      background-color: #f8fafc;\n" +
                "      border: 1px solid #edf2f7;\n" +
                "      border-radius: 8px;\n" +
                "      padding: 24px;\n" +
                "      margin-bottom: 30px;\n" +
                "    }\n" +
                "    .card-title {\n" +
                "      font-size: 13px;\n" +
                "      text-transform: uppercase;\n" +
                "      letter-spacing: 1px;\n" +
                "      color: #a0aec0;\n" +
                "      margin-bottom: 15px;\n" +
                "      font-weight: bold;\n" +
                "    }\n" +
                "    .detail-row {\n" +
                "      display: flex;\n" +
                "      justify-content: space-between;\n" +
                "      margin-bottom: 12px;\n" +
                "      border-bottom: 1px dashed #edf2f7;\n" +
                "      padding-bottom: 12px;\n" +
                "    }\n" +
                "    .detail-row:last-child {\n" +
                "      border-bottom: none;\n" +
                "      padding-bottom: 0;\n" +
                "      margin-bottom: 0;\n" +
                "    }\n" +
                "    .detail-label {\n" +
                "      font-size: 14px;\n" +
                "      color: #718096;\n" +
                "      font-weight: 500;\n" +
                "    }\n" +
                "    .detail-value {\n" +
                "      font-size: 14px;\n" +
                "      color: #2d3748;\n" +
                "      font-weight: 600;\n" +
                "      text-align: right;\n" +
                "    }\n" +
                "    .badge {\n" +
                "      display: inline-block;\n" +
                "      background-color: #e6fffa;\n" +
                "      color: #319795;\n" +
                "      padding: 6px 12px;\n" +
                "      border-radius: 9999px;\n" +
                "      font-size: 12px;\n" +
                "      font-weight: bold;\n" +
                "      margin-top: 10px;\n" +
                "      border: 1px solid #b2f5ea;\n" +
                "    }\n" +
                "    .blockchain-box {\n" +
                "      background: linear-gradient(135deg, #111827, #1f2937);\n" +
                "      border-radius: 8px;\n" +
                "      padding: 20px;\n" +
                "      color: #ffffff;\n" +
                "      margin-bottom: 30px;\n" +
                "    }\n" +
                "    .blockchain-header {\n" +
                "      font-size: 12px;\n" +
                "      color: #10b981;\n" +
                "      font-weight: bold;\n" +
                "      text-transform: uppercase;\n" +
                "      letter-spacing: 1px;\n" +
                "      margin-bottom: 8px;\n" +
                "    }\n" +
                "    .blockchain-hash {\n" +
                "      font-family: 'Courier New', Courier, monospace;\n" +
                "      font-size: 12px;\n" +
                "      background-color: #374151;\n" +
                "      padding: 8px 12px;\n" +
                "      border-radius: 6px;\n" +
                "      word-break: break-all;\n" +
                "      color: #e5e7eb;\n" +
                "    }\n" +
                "    .footer {\n" +
                "      background-color: #f7fafc;\n" +
                "      padding: 25px 30px;\n" +
                "      text-align: center;\n" +
                "      border-top: 1px solid #edf2f7;\n" +
                "      font-size: 12px;\n" +
                "      color: #a0aec0;\n" +
                "    }\n" +
                "    .footer a {\n" +
                "      color: #3182ce;\n" +
                "      text-decoration: none;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"wrapper\">\n" +
                "    <div class=\"container\">\n" +
                "      <div class=\"header\">\n" +
                "        <h1>MUNICIPALIDAD DIGITAL</h1>\n" +
                "        <p>Sistema de Gestión de Documentos Digitales</p>\n" +
                "      </div>\n" +
                "      <div class=\"content\">\n" +
                "        <div class=\"greeting\">Estimado(a) " + r.getNombreCompleto() + ",</div>\n" +
                "        <div class=\"intro\">\n" +
                "          Nos complace informarle que su documento administrativo ha sido revisado y <strong>aprobado con éxito</strong> por las autoridades municipales correspondientes.\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"card\">\n" +
                "          <div class=\"card-title\">Detalles del Documento</div>\n" +
                "          <div class=\"detail-row\">\n" +
                "            <div class=\"detail-label\">ID Documento</div>\n" +
                "            <div class=\"detail-value\">" + r.getDocumentId() + "</div>\n" +
                "          </div>\n" +
                "          <div class=\"detail-row\">\n" +
                "            <div class=\"detail-label\">Tipo de Trámite</div>\n" +
                "            <div class=\"detail-value\">" + r.getTipoDocumento() + "</div>\n" +
                "          </div>\n" +
                "          <div class=\"detail-row\">\n" +
                "            <div class=\"detail-label\">Título del Documento</div>\n" +
                "            <div class=\"detail-value\">" + r.getTitulo() + "</div>\n" +
                "          </div>\n" +
                "          <div class=\"detail-row\">\n" +
                "            <div class=\"detail-label\">Estado</div>\n" +
                "            <div class=\"detail-value\">\n" +
                "              <span class=\"badge\">APROBADO Y FIRMADO</span>\n" +
                "            </div>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "\n" +
                blockchainSection +
                "\n" +
                "        <div class=\"intro\">\n" +
                "          El documento ya cuenta con plena validez legal y puede ser consultado o verificado en nuestra plataforma oficial en cualquier momento.\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div class=\"footer\">\n" +
                "        Este es un correo automático, por favor no responda a este mensaje.<br>\n" +
                "        &copy; 2026 Municipalidad Digital. Todos los derechos reservados.<br>\n" +
                "        <a href=\"http://localhost:5173\">Ir al Portal Ciudadano</a>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String buildRestablecerPasswordHtml(RestablecerPasswordRequest r) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Restablecer Contraseña</title>\n" +
                "  <style>\n" +
                "    body {\n" +
                "      font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\n" +
                "      background-color: #f4f6f9;\n" +
                "      margin: 0;\n" +
                "      padding: 0;\n" +
                "      -webkit-font-smoothing: antialiased;\n" +
                "    }\n" +
                "    .wrapper {\n" +
                "      width: 100%;\n" +
                "      background-color: #f4f6f9;\n" +
                "      padding: 40px 0;\n" +
                "    }\n" +
                "    .container {\n" +
                "      max-width: 600px;\n" +
                "      margin: 0 auto;\n" +
                "      background-color: #ffffff;\n" +
                "      border-radius: 12px;\n" +
                "      overflow: hidden;\n" +
                "      box-shadow: 0 8px 30px rgba(0,0,0,0.05);\n" +
                "    }\n" +
                "    .header {\n" +
                "      background: linear-gradient(135deg, #1e3c72, #2a5298);\n" +
                "      padding: 35px 30px;\n" +
                "      text-align: center;\n" +
                "      color: #ffffff;\n" +
                "    }\n" +
                "    .header h1 {\n" +
                "      margin: 0;\n" +
                "      font-size: 24px;\n" +
                "      font-weight: 700;\n" +
                "      letter-spacing: 0.5px;\n" +
                "    }\n" +
                "    .header p {\n" +
                "      margin: 8px 0 0 0;\n" +
                "      font-size: 14px;\n" +
                "      color: #b0c4de;\n" +
                "    }\n" +
                "    .content {\n" +
                "      padding: 40px 30px;\n" +
                "      text-align: center;\n" +
                "    }\n" +
                "    .greeting {\n" +
                "      font-size: 18px;\n" +
                "      color: #2d3748;\n" +
                "      font-weight: 600;\n" +
                "      margin-bottom: 12px;\n" +
                "      text-align: left;\n" +
                "    }\n" +
                "    .intro {\n" +
                "      font-size: 15px;\n" +
                "      color: #718096;\n" +
                "      line-height: 1.6;\n" +
                "      margin-bottom: 30px;\n" +
                "      text-align: left;\n" +
                "    }\n" +
                "    .btn-container {\n" +
                "      margin: 35px 0;\n" +
                "    }\n" +
                "    .btn {\n" +
                "      display: inline-block;\n" +
                "      background-color: #3182ce;\n" +
                "      color: #ffffff !important;\n" +
                "      padding: 14px 30px;\n" +
                "      border-radius: 8px;\n" +
                "      font-size: 16px;\n" +
                "      font-weight: bold;\n" +
                "      text-decoration: none;\n" +
                "      box-shadow: 0 4px 14px rgba(49, 130, 206, 0.4);\n" +
                "    }\n" +
                "    .btn:hover {\n" +
                "      background-color: #2b6cb0;\n" +
                "    }\n" +
                "    .warning {\n" +
                "      background-color: #fffaf0;\n" +
                "      border-left: 4px solid #dd6b20;\n" +
                "      border-radius: 4px;\n" +
                "      padding: 16px;\n" +
                "      text-align: left;\n" +
                "      margin-top: 30px;\n" +
                "      margin-bottom: 20px;\n" +
                "    }\n" +
                "    .warning-title {\n" +
                "      font-weight: bold;\n" +
                "      color: #dd6b20;\n" +
                "      font-size: 14px;\n" +
                "      margin-bottom: 4px;\n" +
                "    }\n" +
                "    .warning-text {\n" +
                "      color: #718096;\n" +
                "      font-size: 13px;\n" +
                "      line-height: 1.5;\n" +
                "    }\n" +
                "    .link-fallback {\n" +
                "      font-size: 12px;\n" +
                "      color: #a0aec0;\n" +
                "      margin-top: 25px;\n" +
                "      word-break: break-all;\n" +
                "      text-align: left;\n" +
                "    }\n" +
                "    .footer {\n" +
                "      background-color: #f7fafc;\n" +
                "      padding: 25px 30px;\n" +
                "      text-align: center;\n" +
                "      border-top: 1px solid #edf2f7;\n" +
                "      font-size: 12px;\n" +
                "      color: #a0aec0;\n" +
                "    }\n" +
                "    .footer a {\n" +
                "      color: #3182ce;\n" +
                "      text-decoration: none;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"wrapper\">\n" +
                "    <div class=\"container\">\n" +
                "      <div class=\"header\">\n" +
                "        <h1>MUNICIPALIDAD DIGITAL</h1>\n" +
                "        <p>Recuperación de Acceso</p>\n" +
                "      </div>\n" +
                "      <div class=\"content\">\n" +
                "        <div class=\"greeting\">Hola " + r.getNombreCompleto() + ",</div>\n" +
                "        <div class=\"intro\">\n" +
                "          Recibimos una solicitud para restablecer la contraseña de su cuenta en el Portal de la Municipalidad Digital. Para proceder, haga clic en el siguiente botón:\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"btn-container\">\n" +
                "          <a href=\"" + r.getUrlRestablecer() + "\" target=\"_blank\" class=\"btn\">Restablecer Contraseña</a>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"warning\">\n" +
                "          <div class=\"warning-title\">¿No solicitó este cambio?</div>\n" +
                "          <div class=\"warning-text\">\n" +
                "            Si no ha solicitado restablecer su contraseña, ignore este correo con total tranquilidad. Su cuenta sigue estando segura y la contraseña no cambiará.\n" +
                "          </div>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"link-fallback\">\n" +
                "          Si tiene problemas con el botón, copie y pegue la siguiente URL en su navegador:<br>\n" +
                "          <a href=\"" + r.getUrlRestablecer() + "\">" + r.getUrlRestablecer() + "</a>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div class=\"footer\">\n" +
                "        Este es un correo automático, por favor no responda a este mensaje.<br>\n" +
                "        &copy; 2026 Municipalidad Digital. Todos los derechos reservados.<br>\n" +
                "        <a href=\"http://localhost:5173\">Ir al Portal Ciudadano</a>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
