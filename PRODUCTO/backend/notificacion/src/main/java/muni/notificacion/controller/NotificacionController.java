package muni.notificacion.controller;

import muni.notificacion.dto.DocumentoAprobadoRequest;
import muni.notificacion.dto.RestablecerPasswordRequest;
import muni.notificacion.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones/public")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired
    private MailService mailService;

    @PostMapping("/documento-aprobado")
    public ResponseEntity<String> enviarDocumentoAprobado(@RequestBody DocumentoAprobadoRequest request) {
        try {
            mailService.enviarDocumentoAprobado(request);
            return ResponseEntity.ok("Notificación de documento aprobado procesada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al procesar la notificación: " + e.getMessage());
        }
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<String> enviarRestablecerPassword(@RequestBody RestablecerPasswordRequest request) {
        try {
            mailService.enviarRestablecerPassword(request);
            return ResponseEntity.ok("Notificación de restablecimiento de contraseña procesada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al procesar la notificación: " + e.getMessage());
        }
    }
}
