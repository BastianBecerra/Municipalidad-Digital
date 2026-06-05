package muni.documentos.service;

import muni.documentos.integration.IntegrationClient;
import muni.documentos.model.entity.*;
import muni.documentos.model.enums.EstadoBlockchain;
import muni.documentos.model.enums.EstadoDocumento;
import muni.documentos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import muni.documentos.model.dto.UsuarioDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private DocumentoJuntaVecinalRepository jjvvRepository;
    @Mock
    private DocumentoLicitacionRepository licitacionRepository;
    @Mock
    private DocumentoContratoRepository contratoRepository;
    @Mock
    private DocumentoSalvoconductoRepository salvoconductoRepository;
    @Mock
    private DocumentoResidenciaRepository residenciaRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PdfService pdfService;
    @Mock
    private IntegrationClient integrationClient;

    @InjectMocks
    private DocumentoServiceImpl documentoService;

    @Test
    void testFindAll() {
        List<Documento> documentos = new ArrayList<>();
        documentos.add(new DocumentoContrato());
        when(documentoRepository.findAll()).thenReturn(documentos);

        List<Documento> result = documentoService.findAll();
        assertThat(result).hasSize(1);
        verify(documentoRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        Documento doc = new DocumentoContrato();
        doc.setId(1L);
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));

        Documento result = documentoService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Documento no encontrado con ID: 99");
    }

    @Test
    void testFindByUsuarioRut() {
        List<Documento> list = List.of(new DocumentoResidencia());
        when(documentoRepository.findByUsuarioRut("12345678-9")).thenReturn(list);

        List<Documento> result = documentoService.findByUsuarioRut("12345678-9");
        assertThat(result).hasSize(1);
    }

    @Test
    void testGeneratePdf() {
        Documento doc = new DocumentoContrato();
        doc.setId(1L);
        byte[] mockPdf = new byte[]{1, 2, 3};
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(pdfService.generateDocumentPdf(doc)).thenReturn(mockPdf);

        byte[] result = documentoService.generatePdf(1L);
        assertThat(result).isEqualTo(mockPdf);
    }

    @Test
    void testCreateContratoDoc_Borrador() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setTitulo("Contrato de Basura");
        doc.setDescripcion("Servicios de aseo");
        when(contratoRepository.save(any(DocumentoContrato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentoContrato saved = documentoService.createContratoDoc(doc, false);
        assertThat(saved.getEstado()).isEqualTo(EstadoDocumento.BORRADOR);
        assertThat(saved.getEstadoBlockchain()).isEqualTo(EstadoBlockchain.PENDIENTE);
        verify(contratoRepository, times(1)).save(doc);
    }

    @Test
    void testCreateContratoDoc_SimpleFinalized() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setTitulo("Contrato de Basura");
        doc.setDescripcion("Servicios de aseo");
        when(contratoRepository.save(any(DocumentoContrato.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        DocumentoContrato saved = documentoService.createContratoDoc(doc, true);
        assertThat(saved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
        assertThat(saved.getFirmaDigital()).isNotNull();
        assertThat(saved.getCodigoQrUrl()).isNotNull();
    }

    @Test
    void testApproveDocument_Success() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(1L);
        doc.setTitulo("Contrato Test");
        doc.setDescripcion("Desc");
        doc.setEstado(EstadoDocumento.BORRADOR);

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(1L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
        verify(integrationClient, times(1)).notificarAprobacion(any());
    }

    @Test
    void testApproveDocument_NotBorrador_ThrowsException() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(1L);
        doc.setEstado(EstadoDocumento.FIRMADO);

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> documentoService.approveDocument(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Solo se pueden aprobar documentos en estado BORRADOR");
    }

    @Test
    void testSyncWithBlockchain_Success() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(1L);
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setHashSha256("somehash");

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        doReturn(Map.of("status", "success", "transactionHash", "0xtxhash"))
                .when(integrationClient).registrarEnBlockchain(eq("DOC-1"), eq("somehash"));

        documentoService.syncWithBlockchain(1L);

        assertThat(doc.getEstadoBlockchain()).isEqualTo(EstadoBlockchain.CONFIRMADO);
        assertThat(doc.getBlockchainTxHash()).isEqualTo("0xtxhash");
        verify(documentoRepository, times(1)).saveAndFlush(doc);
    }

    @Test
    void testSyncWithBlockchain_FallbackOrError() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(1L);
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setHashSha256("somehash");

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        doReturn(Map.of("status", "error", "message", "Error simulado"))
                .when(integrationClient).registrarEnBlockchain(eq("DOC-1"), eq("somehash"));

        assertThatThrownBy(() -> documentoService.syncWithBlockchain(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error o Fallback activado en respuesta de blockchain");

        assertThat(doc.getEstadoBlockchain()).isEqualTo(EstadoBlockchain.ERROR);
    }

    @Test
    void testSyncWithBlockchain_WrongState_ThrowsException() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(1L);
        doc.setEstado(EstadoDocumento.BORRADOR);

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> documentoService.syncWithBlockchain(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El documento debe estar firmado o aprobado para subirse a Blockchain");
    }

    @Test
    void testCreateSalvoconductoDoc_WithFetchUsuarioMe() {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setTitulo("Salvoconducto Test");
        doc.setDescripcion("Motivo personal");

        UsuarioDTO mockUser = new UsuarioDTO();
        mockUser.setRut("11111111-1");
        mockUser.setNombres("Juan");
        mockUser.setApellidoPaterno("Pérez");

        org.springframework.http.ResponseEntity<UsuarioDTO> myResponse = 
                new org.springframework.http.ResponseEntity<>(mockUser, org.springframework.http.HttpStatus.OK);

        doReturn(myResponse).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(salvoconductoRepository.save(any(DocumentoSalvoconducto.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoSalvoconducto result = documentoService.createSalvoconductoDoc(doc, false);

        assertThat(result.getUsuarioRut()).isEqualTo("11111111-1");
        assertThat(result.getUsuarioNombreCompleto()).isEqualTo("Juan Pérez");
    }

    @Test
    void testCreateResidenciaDoc_WithFetchUsuarioMe() {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setTitulo("Residencia Test");

        UsuarioDTO mockUser = new UsuarioDTO();
        mockUser.setRut("22222222-2");
        mockUser.setNombres("María");
        mockUser.setApellidoPaterno("López");
        mockUser.setApellidoMaterno("Soto");
        mockUser.setDireccion("Calle Falsa 123");
        mockUser.setComuna("Providencia");

        org.springframework.http.ResponseEntity<UsuarioDTO> myResponse = 
                new org.springframework.http.ResponseEntity<>(mockUser, org.springframework.http.HttpStatus.OK);

        doReturn(myResponse).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(residenciaRepository.save(any(DocumentoResidencia.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoResidencia result = documentoService.createResidenciaDoc(doc, false);

        assertThat(result.getUsuarioRut()).isEqualTo("22222222-2");
        assertThat(result.getUsuarioNombreCompleto()).isEqualTo("María López Soto");
        assertThat(result.getUsuarioDireccion()).isEqualTo("Calle Falsa 123");
    }

    @Test
    void testCreateSalvoconductoDoc_FetchUsuarioMeFails_FallbackToFetchUsuario() {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setTitulo("Salvoconducto Test");
        doc.setUsuarioId(5L);

        // First call to /me throws exception
        doThrow(new RuntimeException("Unauthorized for me")).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        UsuarioDTO mockUser = new UsuarioDTO();
        mockUser.setRut("33333333-3");
        mockUser.setNombres("Carlos");
        mockUser.setApellidoPaterno("Gómez");

        org.springframework.http.ResponseEntity<UsuarioDTO> fallbackResponse = 
                new org.springframework.http.ResponseEntity<>(mockUser, org.springframework.http.HttpStatus.OK);

        // Second call to /5 returns user
        doReturn(fallbackResponse).when(restTemplate).exchange(
                contains("/5"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(salvoconductoRepository.save(any(DocumentoSalvoconducto.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoSalvoconducto result = documentoService.createSalvoconductoDoc(doc, false);

        assertThat(result.getUsuarioRut()).isEqualTo("33333333-3");
        assertThat(result.getUsuarioNombreCompleto()).isEqualTo("Carlos Gómez");
    }

    @Test
    void testCreateSalvoconductoDoc_FetchFails_NoUsuarioId() {
        // /me fails, but no usuarioId to fallback -> doc still created without user info
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setTitulo("Salvo sin ID");
        doc.setUsuarioId(null);

        doThrow(new RuntimeException("Unauthorized")).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(salvoconductoRepository.save(any(DocumentoSalvoconducto.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoSalvoconducto result = documentoService.createSalvoconductoDoc(doc, false);
        assertThat(result).isNotNull();
    }

    @Test
    void testCreateSalvoconductoDoc_IsSimple_SyncsBlockchain() {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setTitulo("Salvo Simple");
        doc.setDescripcion("motivo");

        doThrow(new RuntimeException("No /me")).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(salvoconductoRepository.save(any(DocumentoSalvoconducto.class))).thenAnswer(i -> {
            DocumentoSalvoconducto saved = i.getArgument(0);
            saved.setId(99L);
            saved.setEstado(EstadoDocumento.FIRMADO);
            return saved;
        });
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});
        when(documentoRepository.findById(99L)).thenReturn(Optional.of(doc));
        doReturn(Map.of("status", "success", "transactionHash", "0xtx"))
                .when(integrationClient).registrarEnBlockchain(anyString(), anyString());

        DocumentoSalvoconducto result = documentoService.createSalvoconductoDoc(doc, true);
        assertThat(result).isNotNull();
    }

    @Test
    void testCreateResidenciaDoc_FetchFails_FallbackSucceeds() {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setTitulo("Residencia Fallback");
        doc.setUsuarioId(10L);

        doThrow(new RuntimeException("No /me")).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        UsuarioDTO mockUser = new UsuarioDTO();
        mockUser.setRut("44444444-4");
        mockUser.setNombres("Pedro");
        mockUser.setApellidoPaterno("Soto");
        mockUser.setApellidoMaterno(null);
        mockUser.setDireccion("Av. Principal 100");
        mockUser.setComuna("Santiago");

        org.springframework.http.ResponseEntity<UsuarioDTO> fallbackResponse =
                new org.springframework.http.ResponseEntity<>(mockUser, org.springframework.http.HttpStatus.OK);

        doReturn(fallbackResponse).when(restTemplate).exchange(
                contains("/10"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(residenciaRepository.save(any(DocumentoResidencia.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoResidencia result = documentoService.createResidenciaDoc(doc, false);
        assertThat(result.getUsuarioRut()).isEqualTo("44444444-4");
    }

    @Test
    void testCreateResidenciaDoc_FetchFails_NoUsuarioId() {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setTitulo("Residencia sin ID");
        doc.setUsuarioId(null);

        doThrow(new RuntimeException("No /me")).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(residenciaRepository.save(any(DocumentoResidencia.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoResidencia result = documentoService.createResidenciaDoc(doc, false);
        assertThat(result).isNotNull();
    }

    @Test
    void testCreateResidenciaDoc_IsSimple_SyncsBlockchain() {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setTitulo("Residencia Simple");
        doc.setDescripcion("descripcion");

        doThrow(new RuntimeException("No /me")).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(residenciaRepository.save(any(DocumentoResidencia.class))).thenAnswer(i -> {
            DocumentoResidencia saved = i.getArgument(0);
            saved.setId(88L);
            saved.setEstado(EstadoDocumento.FIRMADO);
            return saved;
        });
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});
        when(documentoRepository.findById(88L)).thenReturn(Optional.of(doc));
        doReturn(Map.of("status", "success", "transactionHash", "0xtx"))
                .when(integrationClient).registrarEnBlockchain(anyString(), anyString());

        DocumentoResidencia result = documentoService.createResidenciaDoc(doc, true);
        assertThat(result).isNotNull();
    }

    @Test
    void testCreateJuntaVecinalDoc_WithTerritory() {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setTitulo("Acta Junta");
        doc.setJuntaVecinosId(1L);

        muni.documentos.model.dto.TerritorioDTO territorio = new muni.documentos.model.dto.TerritorioDTO();
        territorio.setNombre("Junta Los Pinos");

        org.springframework.http.ResponseEntity<muni.documentos.model.dto.TerritorioDTO> terrResponse =
                new org.springframework.http.ResponseEntity<>(territorio, org.springframework.http.HttpStatus.OK);

        doReturn(terrResponse).when(restTemplate).exchange(
                anyString(),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(muni.documentos.model.dto.TerritorioDTO.class)
        );

        when(jjvvRepository.save(any(DocumentoJuntaVecinal.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoJuntaVecinal result = documentoService.createJuntaVecinalDoc(doc, false);
        assertThat(result.getNombreJuntaVecinal()).isEqualTo("Junta Los Pinos");
    }

    @Test
    void testCreateJuntaVecinalDoc_NoJuntaId_Simple() {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setTitulo("Acta Simple");
        doc.setDescripcion("desc");
        doc.setJuntaVecinosId(null);

        when(jjvvRepository.save(any(DocumentoJuntaVecinal.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        DocumentoJuntaVecinal result = documentoService.createJuntaVecinalDoc(doc, true);
        assertThat(result.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
    }

    @Test
    void testCreateJuntaVecinalDoc_TerritoryFetchFails() {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setTitulo("Acta Error");
        doc.setJuntaVecinosId(2L);

        doThrow(new RuntimeException("Territory not found")).when(restTemplate).exchange(
                anyString(),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(muni.documentos.model.dto.TerritorioDTO.class)
        );

        when(jjvvRepository.save(any(DocumentoJuntaVecinal.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoJuntaVecinal result = documentoService.createJuntaVecinalDoc(doc, false);
        assertThat(result).isNotNull();
    }

    @Test
    void testApproveDocument_Licitacion() {
        DocumentoLicitacion doc = new DocumentoLicitacion();
        doc.setId(2L);
        doc.setTitulo("Licitacion Test");
        doc.setDescripcion("desc");
        doc.setEstado(EstadoDocumento.BORRADOR);

        when(documentoRepository.findById(2L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(2L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
        verify(integrationClient, times(1)).notificarAprobacion(any());
    }

    @Test
    void testApproveDocument_Salvoconducto_WithRut() {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setId(3L);
        doc.setTitulo("Salvo Test");
        doc.setDescripcion("desc");
        doc.setEstado(EstadoDocumento.BORRADOR);
        doc.setUsuarioRut("55555555-5");
        doc.setUsuarioNombreCompleto("Ana García");

        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setEmail("ana@test.cl");
        usuario.setNombres("Ana");
        usuario.setApellidoPaterno("García");

        org.springframework.http.ResponseEntity<UsuarioDTO> userResp =
                new org.springframework.http.ResponseEntity<>(usuario, org.springframework.http.HttpStatus.OK);
        doReturn(userResp).when(restTemplate).exchange(
                contains("/rut/"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(documentoRepository.findById(3L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(3L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
    }

    @Test
    void testApproveDocument_Salvoconducto_NoRut_NoNombreCompleto() {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setId(4L);
        doc.setTitulo("Salvo SinRut");
        doc.setDescripcion("desc");
        doc.setEstado(EstadoDocumento.BORRADOR);
        doc.setUsuarioRut(null);
        doc.setUsuarioNombreCompleto(null);

        when(documentoRepository.findById(4L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(4L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
    }

    @Test
    void testApproveDocument_Residencia_WithRut() {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(5L);
        doc.setTitulo("Residencia Test");
        doc.setDescripcion("desc");
        doc.setEstado(EstadoDocumento.BORRADOR);
        doc.setUsuarioRut("66666666-6");
        doc.setUsuarioNombreCompleto("Luis Martínez");

        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setEmail("luis@test.cl");
        usuario.setNombres("Luis");
        usuario.setApellidoPaterno("Martínez");

        org.springframework.http.ResponseEntity<UsuarioDTO> userResp =
                new org.springframework.http.ResponseEntity<>(usuario, org.springframework.http.HttpStatus.OK);
        doReturn(userResp).when(restTemplate).exchange(
                contains("/rut/"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(documentoRepository.findById(5L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(5L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
    }

    @Test
    void testApproveDocument_Residencia_NoRut() {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setId(6L);
        doc.setTitulo("Residencia SinRut");
        doc.setDescripcion("desc");
        doc.setEstado(EstadoDocumento.BORRADOR);
        doc.setUsuarioRut(null);

        when(documentoRepository.findById(6L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(6L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
    }

    @Test
    void testApproveDocument_JuntaVecinal_WithRutMinistroDeFe() {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setId(7L);
        doc.setTitulo("Acta Junta");
        doc.setDescripcion("desc");
        doc.setEstado(EstadoDocumento.BORRADOR);
        doc.setRutMinistroDeFe("77777777-7");

        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setEmail("ministro@test.cl");
        usuario.setNombres("Roberto");
        usuario.setApellidoPaterno("Fuentes");

        org.springframework.http.ResponseEntity<UsuarioDTO> userResp =
                new org.springframework.http.ResponseEntity<>(usuario, org.springframework.http.HttpStatus.OK);
        doReturn(userResp).when(restTemplate).exchange(
                contains("/rut/"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(documentoRepository.findById(7L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(7L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
    }

    @Test
    void testApproveDocument_JuntaVecinal_NoRutMinistroDeFe() {
        DocumentoJuntaVecinal doc = new DocumentoJuntaVecinal();
        doc.setId(8L);
        doc.setTitulo("Acta Sin Ministro");
        doc.setDescripcion("desc");
        doc.setEstado(EstadoDocumento.BORRADOR);
        doc.setRutMinistroDeFe(null);

        when(documentoRepository.findById(8L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(8L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
    }

    @Test
    void testApproveDocument_Contrato_WithRutContratista() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(9L);
        doc.setTitulo("Contrato Con Contratista");
        doc.setDescripcion("desc");
        doc.setEstado(EstadoDocumento.BORRADOR);
        doc.setRutContratista("88888888-8");

        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setEmail("contratista@test.cl");
        usuario.setNombres("Felipe");
        usuario.setApellidoPaterno("Torres");

        org.springframework.http.ResponseEntity<UsuarioDTO> userResp =
                new org.springframework.http.ResponseEntity<>(usuario, org.springframework.http.HttpStatus.OK);
        doReturn(userResp).when(restTemplate).exchange(
                contains("/rut/"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(documentoRepository.findById(9L)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));
        when(pdfService.generateDocumentPdf(any())).thenReturn(new byte[]{});

        Documento approved = documentoService.approveDocument(9L);
        assertThat(approved.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
    }

    @Test
    void testSyncWithBlockchain_AprobadoState() {
        DocumentoContrato doc = new DocumentoContrato();
        doc.setId(10L);
        doc.setEstado(EstadoDocumento.APROBADO);
        doc.setHashSha256("aprobadohash");

        when(documentoRepository.findById(10L)).thenReturn(Optional.of(doc));
        doReturn(Map.of("status", "success", "transactionHash", "0xaprobadotx"))
                .when(integrationClient).registrarEnBlockchain(eq("DOC-10"), eq("aprobadohash"));

        documentoService.syncWithBlockchain(10L);

        assertThat(doc.getEstadoBlockchain()).isEqualTo(EstadoBlockchain.CONFIRMADO);
        assertThat(doc.getBlockchainTxHash()).isEqualTo("0xaprobadotx");
    }

    @Test
    void testCreateSalvoconductoDoc_FetchFails_FetchFallbackAlsoFails() {
        DocumentoSalvoconducto doc = new DocumentoSalvoconducto();
        doc.setTitulo("Salvo doble fallo");
        doc.setUsuarioId(5L);

        doThrow(new RuntimeException("No /me")).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        doThrow(new RuntimeException("Fallback also fails")).when(restTemplate).exchange(
                contains("/5"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(salvoconductoRepository.save(any(DocumentoSalvoconducto.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoSalvoconducto result = documentoService.createSalvoconductoDoc(doc, false);
        assertThat(result).isNotNull();
    }

    @Test
    void testCreateResidenciaDoc_FetchFails_FallbackAlsoFails() {
        DocumentoResidencia doc = new DocumentoResidencia();
        doc.setTitulo("Residencia doble fallo");
        doc.setUsuarioId(7L);

        doThrow(new RuntimeException("No /me")).when(restTemplate).exchange(
                contains("/me"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        doThrow(new RuntimeException("Fallback also fails")).when(restTemplate).exchange(
                contains("/7"),
                eq(org.springframework.http.HttpMethod.GET),
                any(org.springframework.http.HttpEntity.class),
                eq(UsuarioDTO.class)
        );

        when(residenciaRepository.save(any(DocumentoResidencia.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoResidencia result = documentoService.createResidenciaDoc(doc, false);
        assertThat(result).isNotNull();
    }
}
