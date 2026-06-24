package muni.documentos.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IntegrationClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private IntegrationClient integrationClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(integrationClient, "blockchainApiUrl", "http://localhost:8087/api/blockchain");
        ReflectionTestUtils.setField(integrationClient, "notificacionApiUrl", "http://localhost:8090/api/notificaciones/public");
    }

    @Test
    void testFallbackRegistrarBlockchain_ReturnsFallbackMap() {
        RuntimeException ex = new RuntimeException("Circuit breaker open");
        Map<?, ?> result = integrationClient.fallbackRegistrarBlockchain("DOC-1", "hash1", ex);

        assertThat(result.get("status")).isEqualTo("error");
        assertThat(result.get("message").toString()).contains("Fallback activado");
    }

    @Test
    void testFallbackNotificarAprobacion_DoesNotThrow() {
        RuntimeException ex = new RuntimeException("Circuit breaker open");
        Map<String, String> request = Map.of("documentId", "DOC-99", "email", "test@test.cl");

        // Should not throw, just log silently
        integrationClient.fallbackNotificarAprobacion(request, ex);
    }
}
