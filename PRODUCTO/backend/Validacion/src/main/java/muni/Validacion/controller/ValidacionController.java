package muni.Validacion.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/api/validacion")
@CrossOrigin(origins = "*")
public class ValidacionController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${muni.documentos.url}")
    private String documentosUrl;

    @Value("${muni.blockchain.url}")
    private String blockchainUrl;

    @GetMapping("/public/validar/{hash}")
    public ResponseEntity<?> validarDocumento(@PathVariable String hash) {
        // 1. Validar estrictamente el formato del hash (64 hex characters) para evitar inyecciones/vulnerabilidades
        if (hash == null || !hash.matches("^[a-fA-F0-9]{64}$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valido", false,
                    "motivo", "Formato de hash inválido. Debe ser un hash SHA-256 de 64 caracteres hexadecimales.",
                    "status", "INVALID_FORMAT"
            ));
        }

        Map<?, ?> doc;
        // 2. Consultar el microservicio de Documentos
        try {
            String url = documentosUrl + "/public/hash/" + hash;
            doc = restTemplate.getForObject(url, Map.class);
            if (doc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "valido", false,
                        "motivo", "El documento no se encuentra registrado en el sistema municipal.",
                        "status", "NOT_FOUND"
                ));
            }
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "valido", false,
                    "motivo", "El documento no está registrado en la base de datos municipal o ha sido alterado.",
                    "status", "NOT_FOUND"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "valido", false,
                    "motivo", "Error de comunicación con el servicio de documentos: " + e.getMessage(),
                    "status", "SERVICE_ERROR"
            ));
        }

        // 3. Consultar la Blockchain usando el ID del documento
        Object docIdObj = doc.get("id");
        if (docIdObj == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "valido", false,
                    "motivo", "El documento recuperado no posee un identificador válido.",
                    "status", "INVALID_DOCUMENT_ID",
                    "documento", doc
            ));
        }

        String documentBlockchainId = "DOC-" + docIdObj.toString();
        Map<String, Object> blockchainData;
        try {
            String url = blockchainUrl + "/consultar/" + documentBlockchainId;
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = restTemplate.getForObject(url, Map.class);
            blockchainData = responseMap;
            if (blockchainData == null) {
                return ResponseEntity.ok(Map.of(
                        "valido", false,
                        "motivo", "El documento no tiene un registro de autenticidad en la blockchain.",
                        "status", "NOT_REGISTERED_ON_BLOCKCHAIN",
                        "documento", doc
                ));
            }
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.ok(Map.of(
                    "valido", false,
                    "motivo", "El documento no posee un registro de autenticidad en la blockchain.",
                    "status", "NOT_REGISTERED_ON_BLOCKCHAIN",
                    "documento", doc
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "valido", false,
                    "motivo", "Error de comunicación con el servicio Blockchain: " + e.getMessage(),
                    "status", "SERVICE_ERROR",
                    "documento", doc
            ));
        }

        // 4. Comparar el hash registrado en Blockchain con el hash del documento
        String bcHash = (String) blockchainData.get("hash");
        String docHash = (String) doc.get("hashSha256");

        if (bcHash == null || docHash == null) {
            return ResponseEntity.ok(Map.of(
                    "valido", false,
                    "motivo", "No se pudo recuperar alguno de los hashes para contrastar la autenticidad.",
                    "status", "MISSING_HASHES",
                    "documento", doc,
                    "blockchain", blockchainData
            ));
        }

        // Normalizar hashes (remover "0x" si existe y comparar sin distinguir mayúsculas)
        String cleanBcHash = bcHash.startsWith("0x") ? bcHash.substring(2) : bcHash;
        String cleanDocHash = docHash.startsWith("0x") ? docHash.substring(2) : docHash;

        boolean isAuthentic = cleanBcHash.equalsIgnoreCase(cleanDocHash);
        
        // Actualizar el estado de verificación en el objeto de retorno de blockchain
        blockchainData.put("verified", isAuthentic);

        if (isAuthentic) {
            return ResponseEntity.ok(Map.of(
                    "valido", true,
                    "motivo", "¡Documento Verificado! El certificado es 100% auténtico y la firma digital coincide con la Blockchain.",
                    "status", "VERIFIED",
                    "documento", doc,
                    "blockchain", blockchainData
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "valido", false,
                    "motivo", "¡ALERTA DE SEGURIDAD! El hash del documento actual no coincide con la versión sellada en la Blockchain. El documento ha sido modificado.",
                    "status", "HASH_MISMATCH",
                    "documento", doc,
                    "blockchain", blockchainData
            ));
        }
    }
}
