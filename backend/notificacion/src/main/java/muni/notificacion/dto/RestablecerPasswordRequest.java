package muni.notificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestablecerPasswordRequest {
    private String email;
    private String nombreCompleto;
    private String token;
    private String urlRestablecer;
}
