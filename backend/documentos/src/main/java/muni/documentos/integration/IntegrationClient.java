package muni.documentos.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationClient {

    private final RestTemplate restTemplate;

    @Value("${muni.blockchain.url:http://localhost:8087/api/blockchain}")
    private String blockchainApiUrl;

    @Value("${muni.notificacion.url:http://localhost:8090/api/notificaciones/public}")
    private String notificacionApiUrl;

    @CircuitBreaker(name = "blockchainCB", fallbackMethod = "fallbackRegistrarBlockchain")
    public Map<?, ?> registrarEnBlockchain(String documentId, String content) {
        log.info("Enviando hash a Blockchain para {}: {}", documentId, content);
        Map<String, String> request = Map.of(
                "documentId", documentId,
                "content", content
        );
        return restTemplate.postForObject(
                blockchainApiUrl + "/registrar",
                request,
                Map.class
        );
    }

    public Map<?, ?> fallbackRegistrarBlockchain(String documentId, String content, Throwable ex) {
        log.warn("⚠️ Circuit Breaker ABIERTO o error al registrar en Blockchain para {}. Detalle: {}", documentId, ex.getMessage());
        return Map.of(
                "status", "error",
                "message", "Servicio de Blockchain no disponible temporalmente. Fallback activado."
        );
    }

    @CircuitBreaker(name = "notificacionCB", fallbackMethod = "fallbackNotificarAprobacion")
    public void notificarAprobacion(Map<String, String> notifRequest) {
        log.info("Enviando solicitud de notificación para {}", notifRequest.get("documentId"));
        restTemplate.postForObject(
                notificacionApiUrl + "/documento-aprobado",
                notifRequest,
                String.class
        );
    }

    public void fallbackNotificarAprobacion(Map<String, String> notifRequest, Throwable ex) {
        log.warn("⚠️ Circuit Breaker ABIERTO o error al notificar aprobación para {}. Detalle: {}", 
                notifRequest.get("documentId"), ex.getMessage());
        // Fallback: Silenciar el error para que la aprobación principal del documento continúe sin caerse
    }
}
