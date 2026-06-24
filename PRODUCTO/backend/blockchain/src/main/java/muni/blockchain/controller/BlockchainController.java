package muni.blockchain.controller;

import lombok.RequiredArgsConstructor;
import muni.blockchain.dto.DocumentResponse;
import muni.blockchain.service.BlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BlockchainController {

    private final BlockchainService blockchainService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> request) {
        try {
            String documentId = request.get("documentId");
            String content = request.get("content");
            
            String txHash = blockchainService.registrarDocumento(documentId, content);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "transactionHash", txHash,
                    "message", "Documento registrado exitosamente en la blockchain"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/consultar/{documentId}")
    public ResponseEntity<?> consultar(@PathVariable String documentId) {
        try {
            DocumentResponse response = blockchainService.consultarDocumento(documentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Documento no encontrado o error en consulta: " + e.getMessage()
            ));
        }
    }
}
