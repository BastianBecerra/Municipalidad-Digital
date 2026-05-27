package muni.notificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoAprobadoRequest {
    private String email;
    private String nombreCompleto;
    private String documentId;
    private String tipoDocumento;
    private String titulo;
    private String hashBlockchain;
}
