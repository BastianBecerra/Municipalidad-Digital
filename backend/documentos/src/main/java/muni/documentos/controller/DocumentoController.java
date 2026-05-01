package muni.documentos.controller;

import lombok.RequiredArgsConstructor;
import muni.documentos.model.entity.*;
import muni.documentos.service.DocumentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping
    public List<Documento> getAll() {
        return documentoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Documento> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.findById(id));
    }

    // --- Creation Endpoints ---

    @PostMapping("/junta-vecinal")
    public DocumentoJuntaVecinal createJJVV(@RequestBody DocumentoJuntaVecinal doc,
                                            @RequestParam(defaultValue = "true") boolean isSimple) {
        return documentoService.createJuntaVecinalDoc(doc, isSimple);
    }

    @PostMapping("/licitacion")
    public DocumentoLicitacion createLicitacion(@RequestBody DocumentoLicitacion doc,
                                                @RequestParam(defaultValue = "false") boolean isSimple) {
        return documentoService.createLicitacionDoc(doc, isSimple);
    }

    @PostMapping("/contrato")
    public DocumentoContrato createContrato(@RequestBody DocumentoContrato doc,
                                            @RequestParam(defaultValue = "false") boolean isSimple) {
        return documentoService.createContratoDoc(doc, isSimple);
    }

    // --- Approval & Blockchain Endpoints ---

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Documento> approve(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.approveDocument(id));
    }

    @PostMapping("/{id}/blockchain")
    public ResponseEntity<Void> syncBlockchain(@PathVariable Long id) {
        documentoService.syncWithBlockchain(id);
        return ResponseEntity.ok().build();
    }
}
