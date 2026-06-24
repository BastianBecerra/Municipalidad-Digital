package muni.blockchain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni.blockchain.dto.DocumentResponse;
import muni.blockchain.service.BlockchainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlockchainController.class)
class BlockchainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlockchainService blockchainService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegistrar_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("documentId", "doc123");
        request.put("content", "hello");

        when(blockchainService.registrarDocumento("doc123", "hello")).thenReturn("0xtxhash");

        mockMvc.perform(post("/api/blockchain/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.transactionHash").value("0xtxhash"))
                .andExpect(jsonPath("$.message").value("Documento registrado exitosamente en la blockchain"));
    }

    @Test
    void testRegistrar_Failure() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("documentId", "doc123");
        request.put("content", "hello");

        when(blockchainService.registrarDocumento("doc123", "hello")).thenThrow(new RuntimeException("Simulated Node Error"));

        mockMvc.perform(post("/api/blockchain/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Simulated Node Error"));
    }

    @Test
    void testConsultar_Success() throws Exception {
        DocumentResponse response = DocumentResponse.builder()
                .documentId("doc123")
                .hash("0xhash")
                .transactionHash("0xtx")
                .timestamp("123456")
                .registeredBy("0xaddr")
                .verified(true)
                .build();

        when(blockchainService.consultarDocumento("doc123")).thenReturn(response);

        mockMvc.perform(get("/api/blockchain/consultar/doc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("doc123"))
                .andExpect(jsonPath("$.hash").value("0xhash"))
                .andExpect(jsonPath("$.registeredBy").value("0xaddr"));
    }

    @Test
    void testConsultar_NotFound() throws Exception {
        when(blockchainService.consultarDocumento("doc123")).thenThrow(new RuntimeException("Not Found"));

        mockMvc.perform(get("/api/blockchain/consultar/doc123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Documento no encontrado o error en consulta: Not Found"));
    }
}
