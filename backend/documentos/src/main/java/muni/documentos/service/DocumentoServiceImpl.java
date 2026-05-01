package muni.documentos.service;

import lombok.RequiredArgsConstructor;
import muni.documentos.model.entity.*;
import muni.documentos.model.enums.EstadoBlockchain;
import muni.documentos.model.enums.EstadoDocumento;
import muni.documentos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final DocumentoJuntaVecinalRepository jjvvRepository;
    private final DocumentoLicitacionRepository licitacionRepository;
    private final DocumentoContratoRepository contratoRepository;

    @Override
    public List<Documento> findAll() {
        return documentoRepository.findAll();
    }

    @Override
    public Documento findById(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public DocumentoJuntaVecinal createJuntaVecinalDoc(DocumentoJuntaVecinal doc, boolean isSimple) {
        processDocument(doc, isSimple);
        return jjvvRepository.save(doc);
    }

    @Override
    @Transactional
    public DocumentoLicitacion createLicitacionDoc(DocumentoLicitacion doc, boolean isSimple) {
        processDocument(doc, isSimple);
        return licitacionRepository.save(doc);
    }

    @Override
    @Transactional
    public DocumentoContrato createContratoDoc(DocumentoContrato doc, boolean isSimple) {
        processDocument(doc, isSimple);
        return contratoRepository.save(doc);
    }

    @Override
    @Transactional
    public Documento approveDocument(Long id) {
        Documento doc = findById(id);
        if (doc.getEstado() != EstadoDocumento.BORRADOR) {
            throw new RuntimeException("Solo se pueden aprobar documentos en estado BORRADOR");
        }
        
        finalizeDocument(doc);
        return documentoRepository.save(doc);
    }

    @Override
    public void syncWithBlockchain(Long id) {
        Documento doc = findById(id);
        if (doc.getEstado() != EstadoDocumento.FIRMADO && doc.getEstado() != EstadoDocumento.APROBADO) {
            throw new RuntimeException("El documento debe estar firmado o aprobado para subirse a Blockchain");
        }
        
        // Simulación de envío a Blockchain
        doc.setEstadoBlockchain(EstadoBlockchain.PROCESANDO);
        doc.setBlockchainTxHash("0x" + generateHash(doc.getHashSha256() + System.currentTimeMillis()));
        doc.setEstadoBlockchain(EstadoBlockchain.CONFIRMADO);
        
        documentoRepository.save(doc);
    }

    // --- Private Helper Methods ---

    private void processDocument(Documento doc, boolean isSimple) {
        if (isSimple) {
            finalizeDocument(doc);
        } else {
            doc.setEstado(EstadoDocumento.BORRADOR);
            doc.setEstadoBlockchain(EstadoBlockchain.PENDIENTE);
        }
    }

    private void finalizeDocument(Documento doc) {
        // 1. Generar Hash SHA-256
        String contentToHash = doc.getTitulo() + doc.getDescripcion() + System.currentTimeMillis();
        String hash = generateHash(contentToHash);
        doc.setHashSha256(hash);

        // 2. Simular Firma y QR
        doc.setFirmaDigital("DIGITAL_SIGNATURE_" + Base64.getEncoder().encodeToString(hash.getBytes()));
        doc.setFirmadoPor("SISTEMA_MUNICIPAL_AUTOMATICO");
        doc.setCodigoQrUrl("https://muni.digital/validar/" + hash);

        // 3. Cambiar estados
        doc.setEstado(EstadoDocumento.FIRMADO);
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA-256", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
