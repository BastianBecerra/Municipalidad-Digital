package muni.documentos.controller;

import lombok.RequiredArgsConstructor;
import muni.documentos.model.entity.*;
import muni.documentos.service.DocumentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    // --- Administrative Getters ---

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping
    public List<Documento> getAll() {
        return documentoService.findAll();
    }

    // --- Owner Check Getters ---

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'VECINO')")
    @GetMapping("/me")
    public List<Documento> getMyDocuments(Authentication authentication) {
        String rut = authentication.getName();
        return documentoService.findByUsuarioRut(rut);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'VECINO')")
    @GetMapping("/{id}")
    public ResponseEntity<Documento> getById(@PathVariable Long id, Authentication authentication) {
        Documento doc = documentoService.findById(id);
        
        if (!hasAdministrativePrivileges(authentication)) {
            String rut = authentication.getName();
            String docRut = getDocumentOwnerRut(doc);
            if (docRut == null || !docRut.equals(rut)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(doc);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'VECINO')")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, Authentication authentication) {
        Documento doc = documentoService.findById(id);
        
        if (!hasAdministrativePrivileges(authentication)) {
            String rut = authentication.getName();
            String docRut = getDocumentOwnerRut(doc);
            if (docRut == null || !docRut.equals(rut)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        byte[] pdf = documentoService.generatePdf(id);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documento_" + id + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // --- Creation Endpoints ---

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping({"/jjvv", "/junta-vecinal"})
    public DocumentoJuntaVecinal createJuntaVecinal(@RequestBody DocumentoJuntaVecinal doc,
                                                    @RequestParam(defaultValue = "true") boolean isSimple) {
        return documentoService.createJuntaVecinalDoc(doc, isSimple);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping("/licitacion")
    public DocumentoLicitacion createLicitacion(@RequestBody DocumentoLicitacion doc,
                                                @RequestParam(defaultValue = "true") boolean isSimple) {
        return documentoService.createLicitacionDoc(doc, isSimple);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping("/contrato")
    public DocumentoContrato createContrato(@RequestBody DocumentoContrato doc,
                                            @RequestParam(defaultValue = "true") boolean isSimple) {
        return documentoService.createContratoDoc(doc, isSimple);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'VECINO')")
    @PostMapping({"/salvoconducto", "/salvoconductos"})
    public ResponseEntity<DocumentoSalvoconducto> createSalvoconducto(@RequestBody DocumentoSalvoconducto doc,
                                                                      @RequestParam(defaultValue = "true") boolean isSimple,
                                                                      Authentication authentication) {
        if (!hasAdministrativePrivileges(authentication)) {
            // Force neighbor's own RUT from JWT
            doc.setUsuarioRut(authentication.getName());
        }
        return ResponseEntity.ok(documentoService.createSalvoconductoDoc(doc, isSimple));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'VECINO')")
    @PostMapping("/residencia")
    public ResponseEntity<DocumentoResidencia> createResidencia(@RequestBody DocumentoResidencia doc,
                                                                @RequestParam(defaultValue = "true") boolean isSimple,
                                                                Authentication authentication) {
        if (!hasAdministrativePrivileges(authentication)) {
            // Force neighbor's own RUT from JWT
            doc.setUsuarioRut(authentication.getName());
        }
        return ResponseEntity.ok(documentoService.createResidenciaDoc(doc, isSimple));
    }

    // --- Approval & Blockchain Endpoints ---

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Documento> approve(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.approveDocument(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping("/{id}/blockchain")
    public ResponseEntity<Void> syncBlockchain(@PathVariable Long id) {
        documentoService.syncWithBlockchain(id);
        return ResponseEntity.ok().build();
    }

    // --- Public Validation Endpoints ---

    @GetMapping("/public/hash/{hash}")
    public ResponseEntity<?> getPublicByHash(@PathVariable String hash) {
        if (hash == null || !hash.matches("^[a-fA-F0-9]{64}$")) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Formato de hash no válido"));
        }
        try {
            Documento doc = documentoService.findByHashSha256(hash);
            return ResponseEntity.ok(doc);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    // --- Helper Methods ---

    private boolean hasAdministrativePrivileges(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_FUNCIONARIO"));
    }

    private String getDocumentOwnerRut(Documento doc) {
        if (doc instanceof DocumentoResidencia r) {
            return r.getUsuarioRut();
        } else if (doc instanceof DocumentoSalvoconducto s) {
            return s.getUsuarioRut();
        }
        return null;
    }
}
