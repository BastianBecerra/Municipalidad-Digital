package muni.documentos.service;

import lombok.RequiredArgsConstructor;
import muni.documentos.model.entity.*;
import muni.documentos.model.dto.UsuarioDTO;
import muni.documentos.model.dto.TerritorioDTO;
import muni.documentos.model.enums.EstadoBlockchain;
import muni.documentos.model.enums.EstadoDocumento;
import muni.documentos.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final DocumentoSalvoconductoRepository salvoconductoRepository;
    private final DocumentoResidenciaRepository residenciaRepository;
    private final RestTemplate restTemplate;
    private final PdfService pdfService;

    @Value("${muni.usuarios.url:http://app-usuarios:8086/usuarios}")
    private String usuariosApiUrl;

    @Value("${muni.territorios.url:http://app-usuarios:8086/territorios}")
    private String territoriosApiUrl;

    @Value("${muni.blockchain.url:http://localhost:8087/api/blockchain}")
    private String blockchainApiUrl;

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
    public List<Documento> findByUsuarioRut(String rut) {
        return documentoRepository.findByUsuarioRut(rut);
    }

    @Override
    public byte[] generatePdf(Long id) {
        Documento doc = findById(id);
        return pdfService.generateDocumentPdf(doc);
    }

    @Override
    @Transactional
    public DocumentoJuntaVecinal createJuntaVecinalDoc(DocumentoJuntaVecinal doc, boolean isSimple) {
        if (doc.getJuntaVecinosId() != null) {
            try {
                TerritorioDTO territorio = restTemplate.exchange(
                        territoriosApiUrl + "/" + doc.getJuntaVecinosId(),
                        HttpMethod.GET,
                        new HttpEntity<>(getForwardedHeaders()),
                        TerritorioDTO.class).getBody();

                if (territorio != null) {
                    doc.setNombreJuntaVecinal(territorio.getNombre());
                }
            } catch (Exception e) {
                System.err.println("Error al obtener territorio: " + e.getMessage());
            }
        }
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
    public DocumentoSalvoconducto createSalvoconductoDoc(DocumentoSalvoconducto doc, boolean isSimple) {
        try {
            // Primero intentar con /usuarios/me (accesible para VECINO)
            UsuarioDTO usuario = fetchUsuarioMe();
            if (usuario != null) {
                doc.setUsuarioRut(usuario.getRut());
                doc.setUsuarioNombreCompleto(usuario.getNombres() + " " + usuario.getApellidoPaterno());
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario para salvoconducto: " + e.getMessage());
            // Fallback: intentar con /usuarios/{id} (para ADMIN/FUNCIONARIO)
            if (doc.getUsuarioId() != null) {
                try {
                    UsuarioDTO usuario = fetchUsuario(doc.getUsuarioId());
                    if (usuario != null) {
                        doc.setUsuarioRut(usuario.getRut());
                        doc.setUsuarioNombreCompleto(usuario.getNombres() + " " + usuario.getApellidoPaterno());
                    }
                } catch (Exception ex) {
                    System.err.println("Error fallback fetchUsuario: " + ex.getMessage());
                }
            }
        }

        processDocument(doc, isSimple);
        return salvoconductoRepository.save(doc);
    }

    @Override
    @Transactional
    public DocumentoResidencia createResidenciaDoc(DocumentoResidencia doc, boolean isSimple) {
        try {
            // Primero intentar con /usuarios/me (accesible para VECINO)
            UsuarioDTO usuario = fetchUsuarioMe();
            if (usuario != null) {
                doc.setUsuarioNombreCompleto(usuario.getNombres() + " " + usuario.getApellidoPaterno() + " "
                        + (usuario.getApellidoMaterno() != null ? usuario.getApellidoMaterno() : ""));
                doc.setUsuarioRut(usuario.getRut());
                doc.setUsuarioDireccion(usuario.getDireccion());
                doc.setUsuarioComuna(usuario.getComuna());
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario con /me para residencia: " + e.getMessage());
            // Fallback: intentar con /usuarios/{id} (para ADMIN/FUNCIONARIO)
            if (doc.getUsuarioId() != null) {
                try {
                    UsuarioDTO usuario = fetchUsuario(doc.getUsuarioId());
                    if (usuario != null) {
                        doc.setUsuarioNombreCompleto(usuario.getNombres() + " " + usuario.getApellidoPaterno() + " "
                                + (usuario.getApellidoMaterno() != null ? usuario.getApellidoMaterno() : ""));
                        doc.setUsuarioRut(usuario.getRut());
                        doc.setUsuarioDireccion(usuario.getDireccion());
                        doc.setUsuarioComuna(usuario.getComuna());
                    }
                } catch (Exception ex) {
                    System.err.println("Error fallback fetchUsuario: " + ex.getMessage());
                }
            }
        }
        processDocument(doc, isSimple);
        return residenciaRepository.save(doc);
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
    @Transactional
    public void syncWithBlockchain(Long id) {
        Documento doc = findById(id);
        if (doc.getEstado() != EstadoDocumento.FIRMADO && doc.getEstado() != EstadoDocumento.APROBADO) {
            throw new RuntimeException("El documento debe estar firmado o aprobado para subirse a Blockchain");
        }

        doc.setEstadoBlockchain(EstadoBlockchain.PROCESANDO);
        documentoRepository.saveAndFlush(doc);

        try {
            java.util.Map<String, String> request = new java.util.HashMap<>();
            request.put("documentId", "DOC-" + doc.getId());
            request.put("content", doc.getHashSha256());

            java.util.Map<?, ?> response = restTemplate.postForObject(
                    blockchainApiUrl + "/registrar",
                    request,
                    java.util.Map.class
            );

            if (response != null && "success".equals(response.get("status"))) {
                String txHash = (String) response.get("transactionHash");
                doc.setBlockchainTxHash(txHash);
                doc.setEstadoBlockchain(EstadoBlockchain.CONFIRMADO);
            } else {
                doc.setEstadoBlockchain(EstadoBlockchain.ERROR);
                String msg = response != null ? (String) response.get("message") : "Respuesta vacía";
                throw new RuntimeException("Error en respuesta de blockchain: " + msg);
            }
        } catch (Exception e) {
            doc.setEstadoBlockchain(EstadoBlockchain.ERROR);
            documentoRepository.save(doc);
            throw new RuntimeException("Error al sincronizar con blockchain: " + e.getMessage(), e);
        }

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

        doc.setEstado(EstadoDocumento.FIRMADO);

        // 4. Generar el archivo PDF real y guardarlo (simulado como byte array en BD
        // para este ejemplo rápido)
        // En un caso real guardaríamos en S3 o disco y pondríamos la ruta en
        // doc.setRutaArchivoPdf()
        byte[] pdfBytes = pdfService.generateDocumentPdf(doc);
        // Para este demo, lo dejamos así. En producción: storageService.save(pdfBytes,
        // fileName);
    }

    private HttpHeaders getForwardedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        try {
            org.springframework.web.context.request.ServletRequestAttributes attributes = 
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    headers.set("Authorization", authHeader);
                    return headers;
                }
            }
        } catch (Exception e) {
            System.err.println("Error propagating authentication header: " + e.getMessage());
        }
        return headers;
    }

    private UsuarioDTO fetchUsuario(Long usuarioId) {
        return restTemplate.exchange(
                usuariosApiUrl + "/" + usuarioId,
                HttpMethod.GET,
                new HttpEntity<>(getForwardedHeaders()),
                UsuarioDTO.class).getBody();
    }

    /**
     * Obtiene datos del usuario autenticado usando /usuarios/me.
     * Este endpoint es accesible para todos los roles (ADMIN, FUNCIONARIO, VECINO).
     */
    private UsuarioDTO fetchUsuarioMe() {
        return restTemplate.exchange(
                usuariosApiUrl + "/me",
                HttpMethod.GET,
                new HttpEntity<>(getForwardedHeaders()),
                UsuarioDTO.class).getBody();
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
