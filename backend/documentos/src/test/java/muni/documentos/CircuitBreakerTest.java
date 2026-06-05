package muni.documentos;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import muni.documentos.integration.IntegrationClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
class CircuitBreakerTest {

    @Autowired
    private IntegrationClient integrationClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testBlockchainCircuitBreakerTransitionsToOpen() {
        // 1. Obtener la instancia del disyuntor
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("blockchainCB");
        
        // Resetear el estado del disyuntor antes del test
        circuitBreaker.reset();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // 2. Configurar el mock de RestTemplate para simular una falla de red (Connection refused)
        Mockito.when(restTemplate.postForObject(any(String.class), any(), eq(Map.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        // 3. Realizar 5 llamadas consecutivas (tamaño de la ventana deslizante)
        for (int i = 0; i < 5; i++) {
            Map<?, ?> result = integrationClient.registrarEnBlockchain("doc-" + i, "hash-" + i);
            // Verificar que el método fallback maneja el error y retorna el mapa de contingencia
            assertThat(result.get("status")).isEqualTo("error");
            assertThat(result.get("message").toString()).contains("Fallback activado");
        }

        // 4. Verificar que el disyuntor cambió su estado a OPEN (Abierto)
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // 5. Realizar una sexta llamada y verificar que se ejecuta el fallback de inmediato
        // sin llamar a RestTemplate (el disyuntor la cortocircuita)
        Map<?, ?> shortCircuitedResult = integrationClient.registrarEnBlockchain("doc-6", "hash-6");
        assertThat(shortCircuitedResult.get("status")).isEqualTo("error");
        
        // Verificar que RestTemplate sólo fue invocado 5 veces, confirmando el corto circuito en la sexta llamada
        Mockito.verify(restTemplate, Mockito.times(5)).postForObject(any(String.class), any(), eq(Map.class));
    }

    @Test
    void testNotificacionCircuitBreakerTransitionsToOpen() {
        // 1. Obtener la instancia del disyuntor
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("notificacionCB");
        
        // Resetear el estado del disyuntor antes del test
        circuitBreaker.reset();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // 2. Configurar el mock de RestTemplate para simular una falla de red (Connection refused)
        Mockito.when(restTemplate.postForObject(any(String.class), any(), eq(String.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        // 3. Realizar 5 llamadas consecutivas (tamaño de la ventana deslizante)
        Map<String, String> request = Map.of("documentId", "DOC-100", "email", "test@muni.cl");
        for (int i = 0; i < 5; i++) {
            // Debería ejecutar el fallback de forma silenciosa sin arrojar excepciones
            integrationClient.notificarAprobacion(request);
        }

        // 4. Verificar que el disyuntor cambió su estado a OPEN (Abierto)
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // 5. Realizar una sexta llamada y verificar que se ejecuta el fallback de inmediato
        // sin llamar a RestTemplate (el disyuntor la cortocircuita)
        integrationClient.notificarAprobacion(request);
        
        // Verificar que RestTemplate sólo fue invocado 5 veces, confirmando el corto circuito en la sexta llamada
        Mockito.verify(restTemplate, Mockito.times(5)).postForObject(any(String.class), any(), eq(String.class));
    }
}
