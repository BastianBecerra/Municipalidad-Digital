package muni.Validacion.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ValidacionController validacionController;

    private RestTemplate mockRestTemplate;

    private final String validHash = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f61234";

    @BeforeEach
    void setUp() {
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        ReflectionTestUtils.setField(validacionController, "restTemplate", mockRestTemplate);
    }

    @Test
    void testValidarDocumento_InvalidFormat_NullOrWrongLength() throws Exception {
        // Test with too short hash
        mockMvc.perform(get("/api/validacion/public/validar/abc123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("INVALID_FORMAT"));

        // Test with non-hex characters
        String nonHexHash = "z1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f61234";
        mockMvc.perform(get("/api/validacion/public/validar/" + nonHexHash))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("INVALID_FORMAT"));
    }

    @Test
    void testValidarDocumento_DocumentNotFound_NullResponse() throws Exception {
        when(mockRestTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.motivo").value("El documento no se encuentra registrado en el sistema municipal."));
    }

    @Test
    void testValidarDocumento_DocumentNotFound_Exception() throws Exception {
        when(mockRestTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.motivo").value("El documento no está registrado en la base de datos municipal o ha sido alterado."));
    }

    @Test
    void testValidarDocumento_DocumentService_GenericException() throws Exception {
        when(mockRestTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection timed out"));

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("SERVICE_ERROR"))
                .andExpect(jsonPath("$.motivo").value("Error de comunicación con el servicio de documentos: Connection timed out"));
    }

    @Test
    void testValidarDocumento_InvalidDocumentId() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", null); // Invalid ID
        mockDoc.put("hashSha256", "abc");

        when(mockRestTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockDoc);

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("INVALID_DOCUMENT_ID"))
                .andExpect(jsonPath("$.motivo").value("El documento recuperado no posee un identificador válido."));
    }

    @Test
    void testValidarDocumento_BlockchainNotFound_Null() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", 123);
        mockDoc.put("hashSha256", "somehash");

        // Document call returns doc
        when(mockRestTemplate.getForObject(Mockito.contains("/public/hash/"), eq(Map.class))).thenReturn(mockDoc);
        // Blockchain call returns null
        when(mockRestTemplate.getForObject(Mockito.contains("/consultar/"), eq(Map.class))).thenReturn(null);

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("NOT_REGISTERED_ON_BLOCKCHAIN"));
    }

    @Test
    void testValidarDocumento_BlockchainNotFound_Exception() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", 123);
        mockDoc.put("hashSha256", "somehash");

        // Document call returns doc
        when(mockRestTemplate.getForObject(Mockito.contains("/public/hash/"), eq(Map.class))).thenReturn(mockDoc);
        // Blockchain call throws 404
        when(mockRestTemplate.getForObject(Mockito.contains("/consultar/"), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("NOT_REGISTERED_ON_BLOCKCHAIN"));
    }

    @Test
    void testValidarDocumento_BlockchainService_GenericException() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", 123);
        mockDoc.put("hashSha256", "somehash");

        // Document call returns doc
        when(mockRestTemplate.getForObject(Mockito.contains("/public/hash/"), eq(Map.class))).thenReturn(mockDoc);
        // Blockchain call throws exception
        when(mockRestTemplate.getForObject(Mockito.contains("/consultar/"), eq(Map.class)))
                .thenThrow(new RuntimeException("Blockchain connection failed"));

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("SERVICE_ERROR"))
                .andExpect(jsonPath("$.motivo").value("Error de comunicación con el servicio Blockchain: Blockchain connection failed"));
    }

    @Test
    void testValidarDocumento_MissingHashes_BlockchainHashNull() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", 123);
        mockDoc.put("hashSha256", "somehash");

        Map<String, Object> mockBlockchain = new HashMap<>();
        mockBlockchain.put("hash", null); // Null hash in blockchain

        when(mockRestTemplate.getForObject(Mockito.contains("/public/hash/"), eq(Map.class))).thenReturn(mockDoc);
        when(mockRestTemplate.getForObject(Mockito.contains("/consultar/"), eq(Map.class))).thenReturn(mockBlockchain);

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("MISSING_HASHES"));
    }

    @Test
    void testValidarDocumento_MissingHashes_DocHashNull() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", 123);
        mockDoc.put("hashSha256", null); // Null hash in DB

        Map<String, Object> mockBlockchain = new HashMap<>();
        mockBlockchain.put("hash", "bchash");

        when(mockRestTemplate.getForObject(Mockito.contains("/public/hash/"), eq(Map.class))).thenReturn(mockDoc);
        when(mockRestTemplate.getForObject(Mockito.contains("/consultar/"), eq(Map.class))).thenReturn(mockBlockchain);

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("MISSING_HASHES"));
    }

    @Test
    void testValidarDocumento_Verified_With0xPrefix() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", 123);
        mockDoc.put("hashSha256", "0xabcdef"); // Has prefix

        Map<String, Object> mockBlockchain = new HashMap<>();
        mockBlockchain.put("hash", "0xabcdef"); // Has prefix

        when(mockRestTemplate.getForObject(Mockito.contains("/public/hash/"), eq(Map.class))).thenReturn(mockDoc);
        when(mockRestTemplate.getForObject(Mockito.contains("/consultar/"), eq(Map.class))).thenReturn(mockBlockchain);

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void testValidarDocumento_Verified_MixedPrefixes() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", 123);
        mockDoc.put("hashSha256", "0xabcdef"); // Has prefix

        Map<String, Object> mockBlockchain = new HashMap<>();
        mockBlockchain.put("hash", "abcdef"); // No prefix

        when(mockRestTemplate.getForObject(Mockito.contains("/public/hash/"), eq(Map.class))).thenReturn(mockDoc);
        when(mockRestTemplate.getForObject(Mockito.contains("/consultar/"), eq(Map.class))).thenReturn(mockBlockchain);

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void testValidarDocumento_HashMismatch() throws Exception {
        Map<String, Object> mockDoc = new HashMap<>();
        mockDoc.put("id", 123);
        mockDoc.put("hashSha256", "abcdef");

        Map<String, Object> mockBlockchain = new HashMap<>();
        mockBlockchain.put("hash", "different");

        when(mockRestTemplate.getForObject(Mockito.contains("/public/hash/"), eq(Map.class))).thenReturn(mockDoc);
        when(mockRestTemplate.getForObject(Mockito.contains("/consultar/"), eq(Map.class))).thenReturn(mockBlockchain);

        mockMvc.perform(get("/api/validacion/public/validar/" + validHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("HASH_MISMATCH"));
    }

    @Test
    void testValidarDocumento_HashNullDirectCall() {
        org.springframework.http.ResponseEntity<?> response = validacionController.validarDocumento(null);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
